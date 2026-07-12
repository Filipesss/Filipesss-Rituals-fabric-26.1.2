package net.filipes.rituals.client.tooltip;

import net.minecraft.world.item.Item;

/**
 * Tracks the vertical scroll offset for oversized item tooltips.
 *
 * recomputeBounds() is called every frame from ScrollableTooltipPositioner with the
 * REAL tooltip height and screen height that vanilla already computed - no guessing
 * needed. If content fits, offset is forced to 0 (locked/unmoveable). If it doesn't,
 * offset is clamped so the tooltip can be nudged up/down but never scrolled entirely
 * off screen.
 */
public class TooltipScrollHandler {

    private static final float PIXELS_PER_SCROLL_NOTCH = 12f;

    private static float offset = 0f;
    private static boolean scrollable = false;
    private static float maxOffset = 0f;

    private static Item lastItem = null;
    private static int lastStage = -1;

    /** Called from TooltipRegistry when the hovered item/stage changes, to reset scroll position. */
    public static void updateContext(Item item, int stage) {
        if (item != lastItem || stage != lastStage) {
            offset = 0f;
            lastItem = item;
            lastStage = stage;
        }
    }

    /** Fixed top/bottom margins used when pinning an oversized tooltip's position. Tune to taste. */
    public static final int TOP_MARGIN = 4;
    public static final int BOTTOM_MARGIN = 4;

    /**
     * Called every frame from ScrollableTooltipPositioner with the tooltip's real height and the
     * screen height. Deliberately does NOT depend on vanilla's cursor-tracked base Y - that value
     * changes with mouse position every frame, which made the clamp (and the scroll offset itself)
     * unstable. TOP_MARGIN/BOTTOM_MARGIN are a fixed, frame-stable stand-in for vanilla's own
     * padding; adjust them if the tooltip sits slightly off from the true screen edges.
     */
    public static void recomputeBounds(int contentHeight, int screenHeight) {
        scrollable = contentHeight > screenHeight;

        if (!scrollable) {
            offset = 0f;
            maxOffset = 0f;
            return;
        }

        maxOffset = Math.max(0f, contentHeight - screenHeight + TOP_MARGIN + BOTTOM_MARGIN);
        offset = Math.max(0f, Math.min(offset, maxOffset));
    }

    public static boolean isScrollable() {
        return scrollable;
    }

    /** How many pixels to shift the tooltip UP by. 0 when locked. */
    public static int getOffset() {
        return Math.round(offset);
    }

    /**
     * @param scrollY vanilla scroll delta (positive = wheel up).
     * @return true if the scroll was consumed (an oversized tooltip is active).
     */
    public static boolean handleScroll(double scrollY) {
        if (!scrollable) return false;

        offset -= (float) scrollY * PIXELS_PER_SCROLL_NOTCH;
        offset = Math.max(0f, Math.min(offset, maxOffset));

        return true;
    }
}