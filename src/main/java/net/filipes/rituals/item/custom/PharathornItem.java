package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class PharathornItem extends Item implements RitualsTooltipStyle {

    public PharathornItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings
                .attributes(
                        ItemAttributeModifiers.builder()
                                .add(
                                        Attributes.ATTACK_DAMAGE,
                                        new AttributeModifier(
                                                Item.BASE_ATTACK_DAMAGE_ID,
                                                attackDamage + material.attackDamageBonus(),
                                                AttributeModifier.Operation.ADD_VALUE
                                        ),
                                        EquipmentSlotGroup.MAINHAND
                                )
                                .add(
                                        Attributes.ATTACK_SPEED,
                                        new AttributeModifier(
                                                Item.BASE_ATTACK_SPEED_ID,
                                                attackSpeed,
                                                AttributeModifier.Operation.ADD_VALUE
                                        ),
                                        EquipmentSlotGroup.MAINHAND
                                )
                                .add(
                                        Attributes.ENTITY_INTERACTION_RANGE,
                                        new AttributeModifier(
                                                Identifier.fromNamespaceAndPath("rituals", "pharathorn_reach"),
                                                1.5,
                                                AttributeModifier.Operation.ADD_VALUE
                                        ),
                                        EquipmentSlotGroup.MAINHAND
                                )
                                .build()
                )
        );
    }

    @Override
    public Component getName(ItemStack stack) {
        int stage = ModDataComponents.getStage(stack);
        MutableComponent nameComponent = Component.literal("")
                .append(Component.translatable(getDescriptionId())
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(getNameColor())).withItalic(false)));
        if (stage > 1) {
            nameComponent
                    .append(Component.literal(" [").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFAA00))))
                    .append(Component.literal("★".repeat(stage - 1)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFCC7700))))
                    .append(Component.literal("]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFAA00))));
        }
        return nameComponent;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        int kills = ModDataComponents.getKillCount(stack);
        builder.accept(
                Component.translatable("tooltip.rituals.pharathorn.kills", kills)
                        .withStyle(s -> s.withColor(TextColor.fromRgb(0xFFAA00)).withItalic(false))
        );
    }

    @Override public int getNameColor()                { return 0xFFFFAA00; }
    @Override public int getTooltipBorderColorTop()    { return 0xFFFFAA00; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF664400; }
    @Override public int getTooltipBackgroundColor()   { return 0xFF550000; }
}