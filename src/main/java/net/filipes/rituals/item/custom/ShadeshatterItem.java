package net.filipes.rituals.item.custom;

import net.filipes.rituals.RitualsClient;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class ShadeshatterItem extends Item implements RitualsTooltipStyle {

    public static final int SHATTER_COOLDOWN_TICKS = 50;
    public static final int MODEL_FRAME_COUNT = 8;

    public ShadeshatterItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            int stage = ModDataComponents.getStage(stack);
            if (stage < 2) return;
            player.getCooldowns().addCooldown(stack, cooldownForStage(stage));
        }
        super.hurtEnemy(stack, target, attacker);
    }

    private int cooldownForStage(int stage) {
        return switch (stage) {
            case 0 -> SHATTER_COOLDOWN_TICKS;
            case 1 -> 44;
            case 2 -> 38;
            case 3 -> 32;
            case 4 -> 26;
            default -> 20;
        };
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        if (level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!player.getCooldowns().isOnCooldown(stack)
                    && !RitualsClient.isShadeshatterAbilityAnimationPlaying()) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                        .send(new net.filipes.rituals.network.ShadeshatterSpellPacket());
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getNameColor() { return 0xBB55FF; }

    @Override
    public int getTooltipBorderColorTop() { return 0xAA33FF; }

    @Override
    public int getTooltipBorderColorBottom() { return 0x220033; }

    @Override
    public int getTooltipBackgroundColor() { return 0xFF150025; }
}