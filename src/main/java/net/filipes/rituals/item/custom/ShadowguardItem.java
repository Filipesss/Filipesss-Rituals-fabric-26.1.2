package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.network.ShadowguardInvisiblePacket;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ShadowguardItem extends MaceItem implements RitualsTooltipStyle, RitualsEnchantable {

    private static final Map<UUID, Long> invisibleUntil = new HashMap<>();

    public ShadowguardItem(Properties settings) {
        super(settings);
    }
    private static final EnchantmentPolicy POLICY = EnchantmentPolicy.layered()
            .stage(1, 2).deny()
            .stage(3, 5).allow(Enchantments.WIND_BURST)
            .stage(6, Integer.MAX_VALUE).allowAll()
            .build();

    public static void markInvisible(UUID uuid) {
        invisibleUntil.put(uuid, System.currentTimeMillis() + 3000);
    }

    public static void tickInvisibility() {
        long now = System.currentTimeMillis();
        invisibleUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    public static boolean isInvisibleFromShadowguard(UUID uuid) {
        Long expiry = invisibleUntil.get(uuid);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    @Override public int getNameColor()                { return 0xFF9d6dd1; }
    @Override public int getTooltipBorderColorTop()    { return 0xFF9B6DFF; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF330066; }
    @Override public int getTooltipBackgroundColor()   { return 0xe5292033; }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }
}