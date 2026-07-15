package net.filipes.rituals.item.custom;

import net.filipes.rituals.blocks.custom.AmethystHourglassBlock;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AmethystHourglassBlockItem extends BlockItem implements RitualsTooltipStyle {

    private final AmethystHourglassBlock block;

    public AmethystHourglassBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
        this.block = (AmethystHourglassBlock) block;
    }

    @Override
    public int getNameColor() {
        return block.getNameColor();
    }

    @Override
    public int getTooltipBorderColorTop() {
        return block.getTooltipBorderColorTop();
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return block.getTooltipBorderColorBottom();
    }

    @Override
    public int getTooltipBackgroundColor() {
        return block.getTooltipBackgroundColor();
    }
}