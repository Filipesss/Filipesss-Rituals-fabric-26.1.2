package net.filipes.rituals.blocks.custom;

import net.filipes.rituals.screen.AmethystHourglassScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystHourglassBlock extends Block {

    private static final Component TITLE = Component.translatable("block.rituals.amethyst_hourglass");

    public AmethystHourglassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inventory, p) -> new AmethystHourglassScreenHandler(syncId, inventory),
                    TITLE
            ));
        }
        return InteractionResult.SUCCESS;
    }
}