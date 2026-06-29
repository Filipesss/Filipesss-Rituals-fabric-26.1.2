package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class RosegoldLeggingsItem extends Item implements RitualsTooltipStyle {

    public RosegoldLeggingsItem(ArmorMaterial material, Properties settings) {
        super(settings
                .humanoidArmor(material, ArmorType.LEGGINGS)
                .durability(ArmorType.LEGGINGS.getDurability(2)));
    }
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, EquipmentSlot slot) {
        if (slot != EquipmentSlot.LEGS) return;
        if (!(entity instanceof LivingEntity living)) return;

        living.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, true, false, true));
    }

    @Override public int getNameColor()                { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorTop()    { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF69B4; }
    @Override public int getTooltipBackgroundColor()   { return 0xFF1A0010; }
}