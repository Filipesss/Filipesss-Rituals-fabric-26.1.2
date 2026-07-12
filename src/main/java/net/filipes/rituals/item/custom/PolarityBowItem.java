package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.entity.custom.PolarityArrowBlueEntity;
import net.filipes.rituals.entity.custom.PolarityArrowRedEntity;
import net.filipes.rituals.entity.custom.ReversePolarityArrowEntity;
import net.filipes.rituals.network.ReversePolarityChargePacket;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PolarityBowItem extends ProjectileWeaponItem implements RitualsTooltipStyle, RitualsEnchantable {

    private static final Map<UUID, Integer> PLAYER_COMBOS = new HashMap<>();


    public static final int COMBO_THRESHOLD = 3;
    private static final double COMBO_BASE_BONUS = 1.0;
    private static final double COMBO_BONUS_STEP = 0.5;

    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.combine(
            EnchantmentPolicy.layered()
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.POWER)
                    .build(),
            EnchantmentPolicy.restricted(Enchantments.FLAME)
    );

    public PolarityBowItem(Properties settings) {
        super(settings);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), angle, velocity, inaccuracy);
        shooter.level().addFreshEntity(projectile);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack arrowStack = user.getProjectile(user.getItemInHand(hand));
        if (arrowStack.isEmpty() && !user.getAbilities().instabuild) {
            return InteractionResult.FAIL;
        }
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) return false;

        ItemStack arrowStack = player.getProjectile(stack);
        boolean isCreative = player.getAbilities().instabuild;

        if (arrowStack.isEmpty() && !isCreative) return false;

        int usedTicks = getUseDuration(stack, user) - remainingUseTicks;
        float pull = getPullProgress(usedTicks);

        if (pull < 0.1f) return false;

        if (!world.isClientSide()) {
            boolean isCharged = ReversePolarityChargePacket.consumeCharge(player.getUUID());

            int stage = ModDataComponents.getStage(stack);
            boolean comboCapable = stage >= 3;

            double chargedExtraDamage = comboCapable
                    ? getComboBonusDamage(getCombo(player.getUUID()))
                    : 0.0;

            AbstractArrow arrow;
            if (isCharged) {
                arrow = new ReversePolarityArrowEntity(world, player, arrowStack, stack);
                arrow.setBaseDamage(ReversePolarityArrowEntity.BASE_DAMAGE + chargedExtraDamage);

                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0f, pull * 3.0f * ReversePolarityArrowEntity.SPEED_MULTIPLIER, 1.0f);
            } else {
                boolean isRed = isRedPolarity(stack);
                arrow = isRed
                        ? new PolarityArrowRedEntity(world, player, arrowStack, stack)
                        : new PolarityArrowBlueEntity(world, player, arrowStack, stack);

                double totalBaseDmg = isRed ? PolarityArrowRedEntity.BASE_DAMAGE : PolarityArrowBlueEntity.BASE_DAMAGE;
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(
                        world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER),
                        stack
                );

                if (powerLevel > 0) {
                    totalBaseDmg += (double) powerLevel * 0.5 + 0.5;
                }
                if (arrow instanceof PolarityArrowBlueEntity blueArrow) {
                    blueArrow.setTrackedBaseDamage(totalBaseDmg);
                } else if (arrow instanceof PolarityArrowRedEntity redArrow) {
                    redArrow.setTrackedBaseDamage(totalBaseDmg);
                }

                float speedMult = isRed
                        ? PolarityArrowRedEntity.SPEED_MULTIPLIER
                        : PolarityArrowBlueEntity.SPEED_MULTIPLIER;
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0f, pull * 3.0f * speedMult, 1.0f);
            }
            if (world instanceof ServerLevel serverLevel) {
                EnchantmentHelper.onProjectileSpawned(
                        serverLevel,
                        stack,
                        arrow,
                        (consumed) -> {}
                );
            }

            arrow.setCritArrow(pull >= 1.0f);
            world.addFreshEntity(arrow);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                    1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);

            if (!isCreative) arrowStack.shrink(1);
        }

        return true;
    }

    @Override
    public void inventoryTick(final ItemStack stack, final ServerLevel level, final Entity owner, final @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (!(owner instanceof Player player)) return;

        boolean isActiveMainHand = player.getMainHandItem() == stack;
        if (!isActiveMainHand && getCombo(player.getUUID()) > 0) {
            resetCombo(player);
        }
    }

    public static int getCombo(UUID uuid) {
        return PLAYER_COMBOS.getOrDefault(uuid, 0);
    }

    public static double getComboBonusDamage(int combo) {
        return combo >= COMBO_THRESHOLD
                ? COMBO_BASE_BONUS + (combo - COMBO_THRESHOLD) * COMBO_BONUS_STEP
                : 0.0;
    }

    public static void incrementCombo(Player player) {
        if (player.level().isClientSide()) return;
        UUID uuid = player.getUUID();
        int nextCombo = getCombo(uuid) + 1;
        PLAYER_COMBOS.put(uuid, nextCombo);

        if (nextCombo >= COMBO_THRESHOLD) {
            int depth = nextCombo - COMBO_THRESHOLD;
            float pitch = 1.0f + Math.min(depth * 0.15f, 1.0f);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, pitch);
        }
    }

    public static void resetCombo(Player player) {
        if (player.level().isClientSide()) return;
        UUID uuid = player.getUUID();
        int previous = PLAYER_COMBOS.getOrDefault(uuid, 0);
        if (previous > 0) {
            PLAYER_COMBOS.put(uuid, 0);

            if (previous >= COMBO_THRESHOLD) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3f, 1.6f);
            }
        }
    }

    public static boolean isRedPolarity(ItemStack stack) {
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return data != null && !data.flags().isEmpty() && data.flags().get(0);
    }

    public static float getPullProgress(int usedTicks) {
        float progress = usedTicks / 20.0f;
        progress = (progress * progress + progress * 2.0f) / 3.0f;
        return Math.min(progress, 1.0f);
    }

    @Override public int getNameColor()               { return 0xFF3856ff; }
    @Override public int getTooltipBorderColorTop()   { return 0xFF4577ff; }
    @Override public int getTooltipBorderColorBottom(){ return 0xFFff3838; }
    @Override public int getTooltipBackgroundColor()  { return 0xE5181533; }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }
}