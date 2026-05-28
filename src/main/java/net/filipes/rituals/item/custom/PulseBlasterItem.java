package net.filipes.rituals.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PulseBlasterItem extends Item {

    public static final int MAX_AMMO_BASE   = 8;
    public static final int MAX_AMMO_STAGE3 = 12;

    private static final int COOLDOWN_BASE  = 4;
    private static final int COOLDOWN_MIN   = 3;
    private static final int CHARGE_TICKS   = 3;
    private static final int RAMP_INTERVAL  = 40;

    public static final long OVERCHARGE_DURATION_MS = 5_000L;

    private static final Map<UUID, Integer> activeAmmo   = new HashMap<>();
    private static final Map<UUID, Integer> lastShotTick = new HashMap<>();
    public  static final Map<UUID, Long>    overchargeExpiry = new HashMap<>();

    public PulseBlasterItem(Properties p) { super(p); }


    public static int getMaxAmmo(int stage) {
        return stage >= 3 ? MAX_AMMO_STAGE3 : MAX_AMMO_BASE;
    }

    public static boolean isOvercharged(UUID id) {
        Long exp = overchargeExpiry.get(id);
        return exp != null && System.currentTimeMillis() < exp;
    }

    public static float getDamageMultiplier(UUID id) {
        return isOvercharged(id) ? 1.6f : 1.0f;
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

    public static void syncAmmo(Player player, int ammo) {
        if (player instanceof ServerPlayer sp)
            ServerPlayNetworking.send(sp, new PulseBlasterAmmoPayload(ammo));
    }

    @Override public ItemUseAnimation getUseAnimation(ItemStack s)           { return ItemUseAnimation.BOW; }
    @Override public int             getUseDuration(ItemStack s, LivingEntity u) { return 72000; }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            UUID id = user.getUUID();
            int current = getAmmo(user.getItemInHand(hand));
            activeAmmo.put(id, current);
            lastShotTick.remove(id);
            syncAmmo(user, current);
        }
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player) || world.isClientSide()) return;

        int ticksHeld = getUseDuration(stack, user) - remainingUseTicks;
        if (ticksHeld < CHARGE_TICKS) return;

        UUID    id    = player.getUUID();
        int     stage = ModDataComponents.getStage(stack);
        boolean over  = isOvercharged(id);

        int cooldown = COOLDOWN_BASE;
        if (stage >= 2) {
            int steps = (ticksHeld - CHARGE_TICKS) / RAMP_INTERVAL;
            cooldown = Math.max(COOLDOWN_MIN, COOLDOWN_BASE - steps);
        }
        if (over) cooldown = Math.max(1, cooldown - 1);

        Integer lastShot = lastShotTick.get(id);
        int ticksSinceCharged = ticksHeld - CHARGE_TICKS;
        boolean shouldFire = (lastShot == null)
                ? ticksSinceCharged == 0
                : (ticksHeld - lastShot) >= cooldown;
        if (!shouldFire) return;

        int maxAmmo = getMaxAmmo(stage);
        int ammo = activeAmmo.getOrDefault(id, 0);
        if (ammo <= 0) {
            if (tryReload(player)) {
                ammo = maxAmmo;
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.6f, 1.2f);
            } else {
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 1.0f);
                setAmmo(stack, 0);
                activeAmmo.remove(id);
                lastShotTick.remove(id);
                syncAmmo(player, -1);
                player.stopUsingItem();
                return;
            }
        }

        world.addFreshEntity(new PulseBlasterBeamEntity(world, user));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSounds.PULSE_BLASTER_SHOT, SoundSource.PLAYERS, 0.2f, 1.0f);

        int newAmmo = ammo - 1;
        activeAmmo.put(id, newAmmo);
        lastShotTick.put(id, ticksHeld);
        syncAmmo(player, newAmmo);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClientSide()) {
            UUID id = user.getUUID();
            Integer remaining = activeAmmo.remove(id);
            if (remaining != null) {
                setAmmo(stack, remaining);
                if (user instanceof Player p) syncAmmo(p, remaining);
            }
            lastShotTick.remove(id);
        }
        return false;
    }
    public static int getLiveAmmo(UUID id, ItemStack stack) {
        return activeAmmo.containsKey(id) ? activeAmmo.get(id) : getAmmo(stack);
    }
    public static void clearActiveAmmo(UUID id) {
        activeAmmo.remove(id);
    }

    private static boolean tryReload(Player player) {
        if (player.isCreative()) return true;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.REDSTONE)) { s.shrink(1); return true; }
        }
        return false;
    }
}