package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.ShadeshatterItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public class ShadeshatterMorphPacket implements CustomPacketPayload {

    public static final Type<ShadeshatterMorphPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_morph"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterMorphPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterMorphPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final long COOLDOWN_MS = 25_000L;
    public static final long MORPH_TICKS = 140L;

    private static final List<Item> MORPH_TARGETS = List.of(
            ModItems.ROSEGOLD_PICKAXE,
            ModItems.ROSEGOLD_HELMET,
            ModItems.ROSEGOLD_CHESTPLATE,
            ModItems.ROSEGOLD_LEGGINGS,
            ModItems.ROSEGOLD_BOOTS,
            ModItems.LIGHTNING_RAPIER,
            ModItems.SOLAR_BLADE,
            ModItems.LUNAR_BLADE,
            ModItems.VORTEX_EDGE,
            ModItems.PULSE_BLASTER,
            ModItems.SHADOWGUARD,
            ModItems.BLIGHTSPEAR,
            ModItems.POLARITY_BOW,
            ModItems.CINDERBOLT,
            ModItems.DEPTHSTRIKE,
            ModItems.PHARATHORN,
            ModItems.TEMPORAL_GLASSREAVER
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void applyRecharge(UUID uuid) {
        SERVER_COOLDOWNS.put(uuid, System.currentTimeMillis() - COOLDOWN_MS / 2);
    }

    public static void handle(ShadeshatterMorphPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ShadeshatterItem)) return;
            if (ShadeshatterMorphHandler.isMorphed(player.getUUID())) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            int shadeshatterStage = ModDataComponents.getStage(stack);

            Item chosen = MORPH_TARGETS.get(player.getRandom().nextInt(MORPH_TARGETS.size()));
            ItemStack morphed = new ItemStack(chosen);

            if (shadeshatterStage >= 5) {
                ModDataComponents.setStageMax(morphed);
            } else if (shadeshatterStage >= 3) {
                ModDataComponents.setStage(morphed, 1 + player.getRandom().nextInt(4));
            } else {
                ModDataComponents.setStage(morphed, 1);
            }

            ShadeshatterPowerup powerup = null;
            if (shadeshatterStage >= 6) {
                ShadeshatterPowerup[] powerups = ShadeshatterPowerup.values();
                powerup = powerups[player.getRandom().nextInt(powerups.length)];
                player.sendOverlayMessage(
                        Component.literal("§5✦ " + powerup.displayName())
                );
            }

            UUID morphId = UUID.randomUUID();
            CompoundTag morphTag = new CompoundTag();
            morphTag.putLong("MorphIdM", morphId.getMostSignificantBits());
            morphTag.putLong("MorphIdL", morphId.getLeastSignificantBits());
            morphed.set(DataComponents.CUSTOM_DATA, CustomData.of(morphTag));

            int slot = player.getInventory().getSelectedSlot();
            ItemStack original = stack.copy();

            player.getInventory().setItem(slot, morphed);
            player.inventoryMenu.broadcastChanges();

            long morphDuration = powerup != null
                    ? (long)(MORPH_TICKS * powerup.durationMultiplier())
                    : MORPH_TICKS;
            ShadeshatterMorphHandler.beginMorph(player, slot, original, morphId, morphDuration, powerup);
        });
    }
}