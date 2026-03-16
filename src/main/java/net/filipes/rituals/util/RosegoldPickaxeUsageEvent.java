package net.filipes.rituals.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class RosegoldPickaxeUsageEvent implements PlayerBlockBreakEvents.Before {

    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity playerEntity, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        ItemStack mainHandItem = playerEntity.getMainHandStack();

        if (mainHandItem.getItem() instanceof RosegoldPickaxeItem rosegoldPickaxe && playerEntity instanceof ServerPlayerEntity serverPlayer) {
            if (HARVESTED_BLOCKS.contains(pos)) {
                return true; // prevent recursion double-break
            }

            for (BlockPos position : RosegoldPickaxeItem.getBlocksToBeDestroyed(1, pos, serverPlayer)) {
                if (pos.equals(position)) {
                    continue;
                }

                BlockState targetState = world.getBlockState(position);
                if (targetState.isAir()) {
                    continue;
                }
                float hardness = targetState.getHardness(world, position);
                if (hardness < 0.0F) {
                    continue;
                }

                HARVESTED_BLOCKS.add(position);
                serverPlayer.interactionManager.tryBreakBlock(position);
                HARVESTED_BLOCKS.remove(position);
            }
        }

        return true;
    }
}