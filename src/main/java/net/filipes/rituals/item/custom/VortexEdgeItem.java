package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class VortexEdgeItem extends Item implements RitualsTooltipStyle {

    public VortexEdgeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            double xVel = (target.getRandom().nextDouble() - 0.5) * 0.6; // -0.25 to 0.25
            double yVel = target.getRandom().nextDouble() * 0.25;  // 0.1 to 0.35 (slight upward pop)
            double zVel = (target.getRandom().nextDouble() - 0.5) * 0.6
                    ; // -0.25 to 0.25
            target.push(xVel, yVel, zVel);
            target.hurtMarked = true;
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getNameColor() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xFF550000;
    }
}