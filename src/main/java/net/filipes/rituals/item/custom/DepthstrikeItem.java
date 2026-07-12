package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.effect.ConductivityHelper;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class DepthstrikeItem extends TridentItem implements RitualsTooltipStyle, RitualsEnchantable {

    public DepthstrikeItem(Properties settings) {
        super(settings);
    }
    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.combine(
            EnchantmentPolicy.layered()
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.IMPALING)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.CHANNELING)
                    .build(),
            EnchantmentPolicy.restricted(Enchantments.LOYALTY),
            EnchantmentPolicy.restricted(Enchantments.RIPTIDE)
    );

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);

        if (attacker.level().isClientSide()) return;
        if (target == attacker) return;

        int stage = ModDataComponents.getStage(stack);

        float bonus = ConductivityHelper.getDamageBonus(target);
        if (bonus > 0f) {
            target.invulnerableTime = 0;

            target.hurt(
                    attacker.level().damageSources().indirectMagic(attacker, attacker),
                    bonus
            );
        }

        if (stage >= 2) {
            ConductivityHelper.applyOrStack(target);
        } else {
            ConductivityHelper.applyLevelOne(target);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player) {
            int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
            if (timeHeld < 10) return false;
            if (itemStack.nextDamageWillBreak()) return false;

            player.awardStat(Stats.ITEM_USED.get(this));

            if (level instanceof ServerLevel serverLevel) {
                itemStack.hurtWithoutBreaking(1, player);
                ItemStack thrownItemStack = itemStack.consumeAndReturn(1, player);

                ThrownDepthstrikeEntity projectile = new ThrownDepthstrikeEntity(serverLevel, player, thrownItemStack);
                projectile.setWeaponStage(ModDataComponents.getStage(itemStack));

                float baseVelocity = 2.5F;
                float velocity = projectile.isCharged
                        ? baseVelocity * ThrownDepthstrikeEntity.CHARGED_SPEED_MULTIPLIER
                        : baseVelocity;

                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);

                if (player.hasInfiniteMaterials()) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                serverLevel.addFreshEntity(projectile);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THROW, player.getSoundSource(), 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }


    @Override public int getNameColor()               { return 0xFF00ffc8; }
    @Override public int getTooltipBorderColorTop()   { return 0xFF00ffc8; }
    @Override public int getTooltipBorderColorBottom(){ return 0xFFff4545; }
    @Override public int getTooltipBackgroundColor()  { return 0xe50a403d; }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }
}