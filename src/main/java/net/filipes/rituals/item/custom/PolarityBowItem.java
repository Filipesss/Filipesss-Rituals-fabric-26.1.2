package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.BowItem;

public class PolarityBowItem extends BowItem implements RitualsTooltipStyle {
    public PolarityBowItem(Properties settings) {
        super(settings);
    }


    @Override
    public int getNameColor() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0;
    }
}