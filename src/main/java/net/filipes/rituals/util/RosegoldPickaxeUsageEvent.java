package net.filipes.rituals.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem.MiningMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RosegoldPickaxeUsageEvent {

    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    private static final Set<BlockPos> AOE_BLOCKS = new HashSet<>();

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(RosegoldPickaxeUsageEvent::onBeforeBreak);
        PlayerBlockBreakEvents.AFTER.register(RosegoldPickaxeUsageEvent::onAfterBreak);
    }

    private static boolean onBeforeBreak(Level level, Player player, BlockPos pos,
                                         BlockState blockState, @Nullable BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer serverPlayer)) return true;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof RosegoldPickaxeItem)) return true;
        if (HARVESTED_BLOCKS.contains(pos)) return true;

        MiningMode mode = RosegoldPickaxeItem.getMiningMode(tool);
        if (mode == MiningMode.NONE) return true;

        for (BlockPos position : RosegoldPickaxeItem.getBlocksToDestroy(mode, pos, serverPlayer)) {
            BlockState targetState = level.getBlockState(position);
            if (targetState.isAir()) continue;
            if (targetState.getDestroySpeed(level, position) < 0.0F) continue;

            HARVESTED_BLOCKS.add(position);
            AOE_BLOCKS.add(position);
            serverPlayer.gameMode.destroyBlock(position);
            HARVESTED_BLOCKS.remove(position);
        }

        return true;
    }

    private static void onAfterBreak(Level level, Player player, BlockPos pos,
                                     BlockState state, @Nullable BlockEntity blockEntity) {

        AOE_BLOCKS.remove(pos);

        if (!(level instanceof ServerLevel serverLevel)) return;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof RosegoldPickaxeItem)) return;
        if (!RosegoldPickaxeItem.hasDoubleDrops(tool)) return;

        LootParams.Builder params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

        List<ItemStack> drops = state.getDrops(params);
        for (ItemStack drop : drops) {
            Block.popResource(serverLevel, pos, drop);
        }
    }
}