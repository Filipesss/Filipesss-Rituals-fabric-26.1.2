package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.TridentItem;

public class DepthstrikeItem extends TridentItem implements RitualsTooltipStyle {
    public DepthstrikeItem(Properties settings) {
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