package net.filipes.rituals.item.custom;

import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;

public class LunarBladeItem extends Item implements RitualsTooltipStyle, RitualsEnchantable {

    public LunarBladeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }
    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.combine(
            EnchantmentPolicy.restricted(Enchantments.SHARPNESS)
    );

    @Override
    public int getNameColor() {
        return 0xFFaedcf5;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0xFFbddff2;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0xFF52819c;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xe51b2830;
    }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }

}
