package net.filipes.rituals.item.custom;

import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;

public class SolarBladeItem extends Item implements RitualsTooltipStyle, RitualsEnchantable {

    public SolarBladeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }
    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.combine(
            EnchantmentPolicy.restricted(Enchantments.SHARPNESS)
    );

    @Override
    public int getNameColor() {
        return 0xFFfce17e;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0xFFffe896;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0xFF918351;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xe536290a;
    }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }

}
