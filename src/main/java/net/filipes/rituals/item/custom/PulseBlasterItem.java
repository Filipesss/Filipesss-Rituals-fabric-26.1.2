package net.filipes.rituals.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PulseBlasterItem extends Item {

    public static final int MAX_AMMO        = 8;
    private static final int COOLDOWN_TICKS = 4;
    private static final int CHARGE_TICKS   = 3;

    private static final Map<UUID, Integer> activeAmmo = new HashMap<>();

    public PulseBlasterItem(Properties settings) {
        super(settings);
    }

    public static int getAmmo(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        return data.copyTag().getInt("Ammo").orElse(0);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = existing != null ? existing.copyTag() : new CompoundTag();
        nbt.putInt("Ammo", ammo);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    // PlayerEntity → Player
    private static void syncAmmo(Player player, int ammo) {
        if (player instanceof ServerPlayer sp) {
            ServerPlayNetworking.send(sp, new PulseBlasterAmmoPayload(ammo));
        }
    }

    private static boolean tryReload(Player player) {
        if (player.isCreative()) return true;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.REDSTONE)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.BOW; }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            int current = getAmmo(user.getItemInHand(hand));
            activeAmmo.put(user.getUUID(), current);
            syncAmmo(user, current);
        }
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) return;
        if (world.isClientSide()) return;

        int ticksHeld = getUseDuration(stack, user) - remainingUseTicks;
        if (ticksHeld < CHARGE_TICKS) return;

        int ticksSinceCharged = ticksHeld - CHARGE_TICKS;
        if (ticksSinceCharged % COOLDOWN_TICKS != 0) return;

        UUID id   = player.getUUID();
        int  ammo = activeAmmo.getOrDefault(id, 0);

        if (ammo <= 0) {
            if (tryReload(player)) {
                ammo = MAX_AMMO;
                world.playSound(null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.LODESTONE_COMPASS_LOCK,
                        SoundSource.PLAYERS, 0.6f, 1.2f);
            } else {
                world.playSound(null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.DISPENSER_FAIL,
                        SoundSource.PLAYERS, 0.5f, 1.0f);

                setAmmo(stack, 0);
                activeAmmo.remove(id);
                syncAmmo(player, -1);
                player.stopUsingItem();
                return;
            }
        }

        world.addFreshEntity(new PulseBlasterBeamEntity(world, user));
        world.playSound(null,
                user.getX(), user.getY(), user.getZ(),
                ModSounds.PULSE_BLASTER_SHOT,
                SoundSource.PLAYERS, 0.5f, 1.0f);
        int newAmmo = ammo - 1;
        activeAmmo.put(id, newAmmo);
        syncAmmo(player, newAmmo);

    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClientSide()) {
            UUID id = user.getUUID();
            if (activeAmmo.containsKey(id)) {
                int finalAmmo = activeAmmo.remove(id);
                setAmmo(stack, finalAmmo);
                if (user instanceof Player player) {
                    syncAmmo(player, -1);
                }
            }
        }
        return false;
    }
}