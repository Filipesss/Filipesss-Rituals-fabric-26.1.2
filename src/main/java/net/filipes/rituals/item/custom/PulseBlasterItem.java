package net.filipes.rituals.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PulseBlasterItem extends Item {

    public static final int MAX_AMMO        = 8;
    private static final int COOLDOWN_TICKS = 4;
    private static final int CHARGE_TICKS   = 3;

    // Server-side ammo cache — avoids touching the stack mid-fire
    private static final Map<UUID, Integer> activeAmmo = new HashMap<>();

    public PulseBlasterItem(Settings settings) {
        super(settings);
    }

    // ── NBT helpers (persistence only) ───────────────────────────────────────

    public static int getAmmo(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return 0;
        return data.copyNbt().getInt("Ammo").orElse(0);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound   nbt     = existing != null ? existing.copyNbt() : new NbtCompound();
        nbt.putInt("Ammo", ammo);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    // ── Packet helper ─────────────────────────────────────────────────────────

    /** Sends the live ammo count to the owning client. */
    private static void syncAmmo(PlayerEntity player, int ammo) {
        if (player instanceof ServerPlayerEntity sp) {
            ServerPlayNetworking.send(sp, new PulseBlasterAmmoPayload(ammo));
        }
    }

    // ── Reload helper ─────────────────────────────────────────────────────────

    private static boolean tryReload(PlayerEntity player) {
        if (player.isCreative()) return true;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(Items.REDSTONE)) {
                s.decrement(1);
                return true;
            }
        }
        return false;
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    @Override
    public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72000; }

    // ── use() — seed the cache, tell the client its starting ammo ─────────────

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            int current = getAmmo(user.getStackInHand(hand));
            activeAmmo.put(user.getUuid(), current);
            syncAmmo(user, current);          // HUD switches to live mode immediately
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    // ── usageTick — fire using cached ammo ────────────────────────────────────

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        if (world.isClient()) return;

        int ticksHeld       = getMaxUseTime(stack, user) - remainingUseTicks;
        if (ticksHeld < CHARGE_TICKS) return;

        int ticksSinceCharged = ticksHeld - CHARGE_TICKS;
        if (ticksSinceCharged % COOLDOWN_TICKS != 0) return;

        UUID id   = player.getUuid();
        int  ammo = activeAmmo.getOrDefault(id, 0);

        if (ammo <= 0) {
            if (tryReload(player)) {
                ammo = MAX_AMMO;
                world.playSound(null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ITEM_LODESTONE_COMPASS_LOCK,
                        SoundCategory.PLAYERS, 0.6f, 1.2f);
            } else {
                world.playSound(null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_DISPENSER_FAIL,
                        SoundCategory.PLAYERS, 0.5f, 1.0f);

                // Flush remaining ammo (0) to NBT, tell client we're done
                setAmmo(stack, 0);
                activeAmmo.remove(id);
                syncAmmo(player, -1);         // HUD reverts to NBT (will show 0)
                player.stopUsingItem();
                return;
            }
        }

        // Spawn beam
        world.spawnEntity(new PulseBlasterBeamEntity(world, user));
        world.playSound(null,
                user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.PLAYERS, 0.5f, 1.4f);

        int newAmmo = ammo - 1;
        activeAmmo.put(id, newAmmo);
        syncAmmo(player, newAmmo);            // HUD updates every shot
    }

    // ── onStoppedUsing — flush cache to NBT, tell client to revert ────────────

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient()) {
            UUID id = user.getUuid();
            if (activeAmmo.containsKey(id)) {
                int finalAmmo = activeAmmo.remove(id);
                setAmmo(stack, finalAmmo);

                if (user instanceof PlayerEntity player) {
                    syncAmmo(player, -1);     // Tell client: done, read NBT from now on
                }
            }
        }
        return false;
    }
}