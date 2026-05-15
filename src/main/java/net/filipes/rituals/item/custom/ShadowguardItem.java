package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ShadowguardItem extends MaceItem implements RitualsTooltipStyle {

    private static final Map<UUID, Long> invisibleUntil = new HashMap<>();

    public ShadowguardItem(Properties settings) {
        super(settings);
    }

    public static void markInvisible(UUID uuid) {
        invisibleUntil.put(uuid, System.currentTimeMillis() + 3000);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);


        if (attacker.level().isClientSide()) return;

        int stage = ModDataComponents.getStage(stack);

        if (stage >= 2) {
            float roll = attacker.level().getRandom().nextFloat();

            if (roll < 0.50f) {

                attacker.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));

                invisibleUntil.put(attacker.getUUID(), System.currentTimeMillis() + 3000);

                if (attacker instanceof ServerPlayer serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new ShadowguardInvisiblePacket());
                }
            }
        }
    }

    public static void tickInvisibility() {
        long now = System.currentTimeMillis();
        invisibleUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    public static boolean isInvisibleFromShadowguard(UUID uuid) {
        Long expiry = invisibleUntil.get(uuid);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    @Override
    public Component getName(ItemStack stack) {
        int stage = ModDataComponents.getStage(stack);
        MutableComponent nameComponent = Component.literal("")
                .append(Component.translatable(getDescriptionId())
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(getNameColor())).withItalic(false)));
        if (stage > 1) {
            nameComponent
                    .append(Component.literal(" [").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF9B6DFF))))
                    .append(Component.literal("★".repeat(stage - 1)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF7744CC))))
                    .append(Component.literal("]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF9B6DFF))));
        }
        return nameComponent;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        int kills = ModDataComponents.getKillCount(stack);
        builder.accept(
                Component.translatable("tooltip.rituals.shadowguard.kills", kills)
                        .withStyle(s -> s.withColor(TextColor.fromRgb(0x9B6DFF)).withItalic(false))
        );
    }

    @Override public int getNameColor()                { return 0xFF9B6DFF; }
    @Override public int getTooltipBorderColorTop()    { return 0xFF9B6DFF; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF330066; }
    @Override public int getTooltipBackgroundColor()   { return 0xFF550000; }
}