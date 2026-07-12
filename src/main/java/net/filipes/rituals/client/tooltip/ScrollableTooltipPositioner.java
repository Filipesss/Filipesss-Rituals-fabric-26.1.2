package net.filipes.rituals.client.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class ScrollableTooltipPositioner implements ClientTooltipPositioner {

    private final ClientTooltipPositioner delegate;

    public ScrollableTooltipPositioner(ClientTooltipPositioner delegate) {
        this.delegate = delegate;
    }

    @Override
    public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        TooltipScrollHandler.recomputeBounds(height, screenHeight);

        Vector2ic base = delegate.positionTooltip(screenWidth, screenHeight, x, y, width, height);

        if (!TooltipScrollHandler.isScrollable()) {
            return base;
        }
        int pinnedTopY = TooltipScrollHandler.TOP_MARGIN;
        int y2 = pinnedTopY - TooltipScrollHandler.getOffset();
        return new Vector2i(base.x(), y2);
    }
}