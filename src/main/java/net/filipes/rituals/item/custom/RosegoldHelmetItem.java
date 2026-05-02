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

public class RosegoldHelmetItem extends Item implements RitualsTooltipStyle {
    public RosegoldHelmetItem(ArmorMaterial material, Properties settings) {
        super(settings
                .humanoidArmor(material, ArmorType.HELMET)
                .durability(ArmorType.HELMET.getDurability(2)));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, EquipmentSlot slot) {
        if (slot != EquipmentSlot.HEAD) return;
        if (!(entity instanceof LivingEntity living)) return;

        living.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 1, true, false, false));
    }

    @Override public int getNameColor() { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorTop() { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF69B4; }
    @Override public int getTooltipBackgroundColor() { return 0xFF1A0010; }
}