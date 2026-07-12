package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class RosegoldBootsItem extends Item implements RitualsTooltipStyle {

    public RosegoldBootsItem(ArmorMaterial material, Properties settings) {
        super(settings
                .humanoidArmor(material, ArmorType.BOOTS)
                .durability(ArmorType.BOOTS.getDurability(2)));
    }

    @Override public int getNameColor()                { return 0xFFFFB6C1; }
    @Override public int getTooltipBorderColorTop()    { return 0xFFFF80AA; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF99004D; }
    @Override public int getTooltipBackgroundColor()   { return 0xE5420d29; }
}