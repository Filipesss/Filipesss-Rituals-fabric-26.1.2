package net.filipes.rituals.util;

public interface RitualsTooltipStyle {
    /** ARGB tint of the item name text. */
    int getNameColor();

    /** ARGB tint of the tooltip BORDER texture. */
    int getTooltipBorderColor();

    /** ARGB tint of the tooltip BACKGROUND texture. */
    int getTooltipBackgroundColor();
}