package net.filipes.rituals.tooltip;


import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.world.item.Item;

import java.util.*;

public class TooltipRegistry {

    private static final Map<Item, List<TooltipLine>> REGISTRY = new LinkedHashMap<>();

    public static void register(Item item, TooltipLine... lines) {
        REGISTRY.computeIfAbsent(item, k -> new ArrayList<>())
                .addAll(Arrays.asList(lines));
    }

    public static void register(Item item, TooltipLine line) {
        REGISTRY.computeIfAbsent(item, k -> new ArrayList<>()).add(line);
    }


    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            List<TooltipLine> tooltips = REGISTRY.get(stack.getItem());
            if (tooltips == null || tooltips.isEmpty()) return;


            int insertAt = 1;
            for (TooltipLine line : tooltips) {
                lines.add(insertAt++, line.toComponent());
            }
        });
    }
}