package net.filipes.rituals.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.item.ModToolMaterials;
import net.filipes.rituals.network.TemporalMuteClearPacket;
import net.filipes.rituals.util.MuteTracker;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BlightspearItem extends Item implements RitualsTooltipStyle, RitualsEnchantable {

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
    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.combine(
            EnchantmentPolicy.layered()
                    .stage(4, Integer.MAX_VALUE).allow(Enchantments.LUNGE)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.SHARPNESS)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.SMITE)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.BANE_OF_ARTHROPODS)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.KNOCKBACK)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.FIRE_ASPECT)
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.LOOTING)
                    .build()
    );

    @Override
    public void inventoryTick(final ItemStack itemStack, final ServerLevel level, final Entity owner, final @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        if (!level.isClientSide() && owner instanceof LivingEntity living) {

            if (living.getMainHandItem() == itemStack || living.getOffhandItem() == itemStack) {

                if (living.hasEffect(MobEffects.SLOWNESS)) {
                    living.removeEffect(MobEffects.SLOWNESS);
                }

                if (living.hasEffect(ModStatusEffects.STUN)) {
                    living.removeEffect(ModStatusEffects.STUN);
                }

                if (MuteTracker.isMuted(living.getUUID())) {
                    MuteTracker.clear(living.getUUID());
                    if (living instanceof ServerPlayer sp) {
                        ServerPlayNetworking.send(sp, new TemporalMuteClearPacket());
                    }
                }
            }
        }
    }

    @Override
    public int getNameColor() {
        return 0xFFe02b2b;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0xFFdb4035;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0xFFdbc035;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xE53d170e;
    }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }
}