package net.filipes.rituals.tooltip;

import net.filipes.rituals.item.ModItems;
import net.minecraft.world.item.Items;

public class ModTooltips {

    public static void register() {

        TooltipRegistry.register(Items.MACE,
                TooltipLine.translated("tooltip.minecraft.mace", 0xFF0000)
        );

        TooltipRegistry.register(ModItems.ROSEGOLD_BOOTS,
                TooltipLine.builder()
                        .literal("This item makes you ", 0xFFFFFF)
                        .literal("Double Jump", 0xFF0000).bold().underline()
                        .build()
        );

        TooltipRegistry.register(ModItems.LIGHTNING_RAPIER,
                TooltipLine.builder()
                        .literal("Stage 1", 0xFFFFFF).bold().underline()
                        .build(),
                TooltipLine.builder()
                        .literal("Grants ", 0xFFFFFF)
                        .literal("Double Jump", 0xFF0000).bold()
                        .literal(" when worn", 0xFFFFFF)
                        .build(),
                TooltipLine.builder()
                        .literal("Stage 2", 0xFFFFFF).bold().underline()
                        .build(),
                TooltipLine.builder()
                        .literal("Calls down ", 0xFFFFFF)
                        .literal("Lightning", 0xFFFF00).italic()
                        .literal(" on hit", 0xFFFFFF)
                        .build()
        );
    }
}