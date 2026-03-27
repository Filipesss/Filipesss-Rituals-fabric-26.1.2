package net.filipes.rituals.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class RosegoldPickaxeUsageEvent implements PlayerBlockBreakEvents.Before {

    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    @Override
    public boolean beforeBlockBreak(Level level, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        ItemStack mainHandItem = player.getMainHandItem();  // was getMainHandStack()

        if (mainHandItem.getItem() instanceof RosegoldPickaxeItem rosegoldPickaxe && player instanceof ServerPlayer serverPlayer) {
            if (HARVESTED_BLOCKS.contains(pos)) {
                return true; // prevent recursion / double-break
            }

            for (BlockPos position : RosegoldPickaxeItem.getBlocksToBeDestroyed(1, pos, serverPlayer)) {
                if (pos.equals(position)) {
                    continue;
                }

                BlockState targetState = level.getBlockState(position);
                if (targetState.isAir()) {
                    continue;
                }

                float hardness = targetState.getDestroySpeed(level, position); // was getHardness()
                if (hardness < 0.0F) {
                    continue;
                }

                HARVESTED_BLOCKS.add(position);
                serverPlayer.gameMode.destroyBlock(position); // was interactionManager.tryBreakBlock()
                HARVESTED_BLOCKS.remove(position);
            }
        }

        return true;
    }
}