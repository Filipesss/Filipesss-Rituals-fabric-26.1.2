package net.filipes.rituals.util;

public interface RitualsTooltipStyle {
    /** ARGB tint of the item name text. */
    int getNameColor();

    /** ARGB tint of the tooltip BORDER texture (TOP). */
    int getTooltipBorderColorTop();

    /** ARGB tint of the tooltip BORDER texture (BOTTOM). */
    int getTooltipBorderColorBottom();

    /** ARGB tint of the tooltip BACKGROUND texture. */
    int getTooltipBackgroundColor();
}