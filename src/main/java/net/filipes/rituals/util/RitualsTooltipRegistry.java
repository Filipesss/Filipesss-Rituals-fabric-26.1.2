package net.filipes.rituals.util;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class RitualsTooltipRegistry {

    private static final Map<Item, RitualsTooltipStyle> STYLES = new HashMap<>();

    public static void register(Item item, RitualsTooltipStyle style) {
        STYLES.put(item, style);
    }

    public static RitualsTooltipStyle get(Item item) {
        return STYLES.get(item);
    }
}