package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class RosegoldChestplateItem extends Item implements RitualsTooltipStyle {

    public RosegoldChestplateItem(ArmorMaterial material, Properties settings) {
        super(settings
                .humanoidArmor(material, ArmorType.CHESTPLATE)
                .durability(ArmorType.CHESTPLATE.getDurability(2))
                .attributes(
                        material.createAttributes(ArmorType.CHESTPLATE)
                                .withModifierAdded(
                                        Attributes.MAX_HEALTH,
                                        new AttributeModifier(
                                                Identifier.fromNamespaceAndPath("rituals", "rosegold_chestplate_health"),
                                                10.0,
                                                AttributeModifier.Operation.ADD_VALUE
                                        ),
                                        EquipmentSlotGroup.CHEST
                                )
                )
        );
    }

    @Override public int getNameColor()                { return 0xFFFFB6C1; }
    @Override public int getTooltipBorderColorTop()    { return 0xFFFF80AA; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF99004D; }
    @Override public int getTooltipBackgroundColor()   { return 0xE5420d29; }
}