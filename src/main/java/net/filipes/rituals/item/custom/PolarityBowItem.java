package net.filipes.rituals.item.custom;

import net.filipes.rituals.entity.custom.PolarityArrowBlueEntity;
import net.filipes.rituals.entity.custom.PolarityArrowRedEntity;
import net.filipes.rituals.entity.custom.ReversePolarityArrowEntity;
import net.filipes.rituals.network.ReversePolarityChargePacket;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

public class PolarityBowItem extends Item implements RitualsTooltipStyle {

    public PolarityBowItem(Properties settings) {
        super(settings);
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

        // REPLACE the entire server-side block in releaseUsing():

        if (!world.isClientSide()) {
            boolean isCharged = ReversePolarityChargePacket.consumeCharge(player.getUUID());

            AbstractArrow arrow;
            if (isCharged) {
                arrow = new ReversePolarityArrowEntity(world, player, stack);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0f, pull * 3.0f * ReversePolarityArrowEntity.SPEED_MULTIPLIER, 1.0f);
            } else {
                boolean isRed = isRedPolarity(stack);
                arrow = isRed
                        ? new PolarityArrowRedEntity(world, player, stack)
                        : new PolarityArrowBlueEntity(world, player, stack);
                float speedMult = isRed
                        ? PolarityArrowRedEntity.SPEED_MULTIPLIER
                        : PolarityArrowBlueEntity.SPEED_MULTIPLIER;
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0f, pull * 3.0f * speedMult, 1.0f);
            }

            arrow.setCritArrow(pull >= 1.0f);
            world.addFreshEntity(arrow);  // single add (was duplicated before)

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                    1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);

            if (!isCreative) arrowStack.shrink(1);
        }

        return true;
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

    // --- tooltip interface ---

    @Override public int getNameColor()               { return 0; }
    @Override public int getTooltipBorderColorTop()   { return 0; }
    @Override public int getTooltipBorderColorBottom(){ return 0; }
    @Override public int getTooltipBackgroundColor()  { return 0; }
}