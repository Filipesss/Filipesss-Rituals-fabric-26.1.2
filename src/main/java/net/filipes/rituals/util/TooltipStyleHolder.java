package net.filipes.rituals.util;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Shared state between the tooltip event (sets the style)
 * and the mixin (reads the style while drawing the border).
 */
public class TooltipStyleHolder {

    @Nullable
    public static RitualsTooltipStyle currentStyle = null;

    public static void set(@Nullable ItemStack stack) {
        if (stack != null && stack.getItem() instanceof RitualsTooltipStyle style) {
            currentStyle = style;
        } else {
            currentStyle = null;
        }
    }

    public static void clear() {
        currentStyle = null;
    }
}