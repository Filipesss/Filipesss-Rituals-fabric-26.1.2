package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShadeshatterMorphHandler {

    private record MorphEntry(
            ServerPlayer player,
            int slot,
            ItemStack original,
            UUID morphId,
            long expiryTick,
            @Nullable ShadeshatterPowerup powerup
    ) {}

    private static final Map<UUID, MorphEntry> ACTIVE = new HashMap<>();

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                onPlayerDisconnect(handler.player));
    }

    public static void beginMorph(ServerPlayer player, int slot, ItemStack original,
                                  UUID morphId, long durationTicks,
                                  @Nullable ShadeshatterPowerup powerup) {
        long expiry = player.level().getGameTime() + durationTicks;
        ACTIVE.put(player.getUUID(),
                new MorphEntry(player, slot, original, morphId, expiry, powerup));

        if (powerup != null) {
            ShadeshatterPowerupTracker.applyPowerup(player, powerup);
        }
    }

    public static boolean isMorphed(UUID uuid) {
        return ACTIVE.containsKey(uuid);
    }

    public static @Nullable ItemStack getOriginalIfMorphed(UUID playerUUID) {
        MorphEntry m = ACTIVE.get(playerUUID);
        return m != null ? m.original() : null;
    }

    public static void updateOriginal(UUID playerUUID, ItemStack newOriginal) {
        MorphEntry old = ACTIVE.get(playerUUID);
        if (old == null) return;
        ACTIVE.put(playerUUID, new MorphEntry(
                old.player(), old.slot(), newOriginal, old.morphId(),
                old.expiryTick(), old.powerup()));
    }


    public static void tick(MinecraftServer server) {
        ACTIVE.entrySet().removeIf(entry -> {
            MorphEntry m = entry.getValue();
            ServerPlayer player = m.player();

            if (player.isRemoved()) return true;

            if (player.level().getGameTime() >= m.expiryTick()) {
                restoreMorph(player, m, player.level());
                return true;
            }

            return false;
        });
    }

    private static void onPlayerDisconnect(ServerPlayer player) {
        MorphEntry m = ACTIVE.remove(player.getUUID());
        if (m == null) return;
        restoreMorph(player, m, player.level());
    }

    private static void restoreMorph(ServerPlayer player, MorphEntry m, ServerLevel level) {

        if (m.powerup() != null) {
            ShadeshatterPowerupTracker.removePowerup(player);
        }
        if (m.powerup() == ShadeshatterPowerup.MORPH_RECHARGE) {
            ShadeshatterMorphPacket.applyRecharge(player.getUUID());
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (hasMorphId(player.getInventory().getItem(i), m.morphId())) {
                if (i >= 36 && i <= 39) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.addItem(m.original().copy());
                } else {
                    player.getInventory().setItem(i, m.original().copy());
                }
                player.inventoryMenu.broadcastChanges();
                return;
            }
        }

        if (player.containerMenu != player.inventoryMenu) {
            for (Slot slot : player.containerMenu.slots) {
                if (hasMorphId(slot.getItem(), m.morphId())) {
                    slot.set(m.original().copy());
                    player.containerMenu.broadcastChanges();
                    return;
                }
            }
        }

        BlockPos playerPos = player.blockPosition();
        for (BlockPos bp : BlockPos.betweenClosed(
                playerPos.offset(-16, -4, -16),
                playerPos.offset( 16,  4,  16))) {
            BlockEntity be = level.getBlockEntity(bp);
            if (be instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (hasMorphId(container.getItem(i), m.morphId())) {
                        container.setItem(i, m.original().copy());
                        be.setChanged();
                        return;
                    }
                }
            }
        }

        var thrownDepthstrikes = level.getEntities(
                ModEntities.THROWN_DEPTHSTRIKE,
                player.getBoundingBox().inflate(128),
                e -> player.equals(e.getOwner())
        );
        if (!thrownDepthstrikes.isEmpty()) {
            thrownDepthstrikes.forEach(e -> e.discard());
            player.getInventory().setItem(m.slot(), m.original().copy());
            player.inventoryMenu.broadcastChanges();
            return;
        }

        List<ItemEntity> found = level.getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(64),
                ie -> hasMorphId(ie.getItem(), m.morphId())
        );
        if (!found.isEmpty()) {
            found.get(0).setItem(m.original().copy());
            return;
        }

        player.getInventory().setItem(m.slot(), m.original().copy());
        player.inventoryMenu.broadcastChanges();
    }

    public static boolean hasMorphId(ItemStack stack, UUID morphId) {
        if (stack.isEmpty()) return false;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return false;
        CompoundTag tag = cd.copyTag();
        if (!tag.contains("MorphIdM") || !tag.contains("MorphIdL")) return false;
        long msb = tag.getLongOr("MorphIdM", 0L);
        long lsb = tag.getLongOr("MorphIdL", 0L);
        return morphId.getMostSignificantBits() == msb && morphId.getLeastSignificantBits() == lsb;
    }
}