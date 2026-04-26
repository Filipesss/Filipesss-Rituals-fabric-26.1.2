package net.filipes.rituals.item.custom;

import net.filipes.rituals.entity.custom.PolarityArrowEntity;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
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
        // Only start drawing if the player has arrows (or is in creative)
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
            PolarityArrowEntity arrow = new PolarityArrowEntity(world, player, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0f, pull * 3.0f, 1.0f);
            arrow.setCritArrow(pull >= 1.0f);

            world.addFreshEntity(arrow);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                    1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pull * 0.5f);

            if (!isCreative) {
                arrowStack.shrink(1);
            }
        }

        return true;
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