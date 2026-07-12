package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class TemporalGlassreaverItem extends AxeItem implements RitualsTooltipStyle {

    public TemporalGlassreaverItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getNameColor() {
        return 0xFFbb8cf5;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0xFF73f05d;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0xFFdb4646;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xE509331a;
    }

}
