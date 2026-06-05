package net.filipes.rituals.item.custom;

import net.filipes.rituals.item.ModToolMaterials;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BlightspearItem extends Item implements RitualsTooltipStyle {

    public BlightspearItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.spear(
                material,
                1.15f,
                1.2f,
                0.4f,
                2.5f,
                9.0f,
                5.5f,
                5.1f,
                8.75f,
                4.6f
        ));
    }

    @Override
    public void inventoryTick(final ItemStack itemStack, final ServerLevel level, final Entity owner, final @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        if (!level.isClientSide() && owner instanceof LivingEntity living) {

            if (living.getMainHandItem() == itemStack || living.getOffhandItem() == itemStack) {

                if (living.hasEffect(MobEffects.SLOWNESS)) {
                    living.removeEffect(MobEffects.SLOWNESS);
                }
            }
        }
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
        return 0;
    }
}