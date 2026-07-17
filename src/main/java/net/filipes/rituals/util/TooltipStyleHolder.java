package net.filipes.rituals.util;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TooltipStyleHolder {

    @Nullable
    public static RitualsTooltipStyle currentStyle = null;

    public static void set(@Nullable ItemStack stack) {
        if (stack == null) {
            currentStyle = null;
            return;
        }

        if (stack.getItem() instanceof RitualsTooltipStyle style) {
            currentStyle = style;
        } else {
            currentStyle = RitualsTooltipRegistry.get(stack.getItem());
        }
    }

    public static void clear() {
        currentStyle = null;
    }
}