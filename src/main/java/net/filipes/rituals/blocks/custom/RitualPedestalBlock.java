package net.filipes.rituals.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RitualPedestalBlock extends BaseEntityBlock {

    public RitualPedestalBlock(Properties properties) {
        super(properties);
    }

    private static final MapCodec<RitualPedestalBlock> CODEC =
            simpleCodec(RitualPedestalBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (w, pos, s, be) -> {
            if (be instanceof RitualPedestalBlockEntity pedestal) {
                pedestal.tick(w);
            }
        };
    }

    private ItemStack mergeIntoPlayerInventory(Player player, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        Inventory inv = player.getInventory();
        ItemStack toGive = stack.copy();

        for (int i = 0; i < inv.getContainerSize() && !toGive.isEmpty(); i++) {
            ItemStack slot = inv.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItem(slot, toGive)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int put = Math.min(space, toGive.getCount());
                    slot.grow(put);
                    toGive.shrink(put);
                    inv.setItem(i, slot);
                }
            }
        }

        for (int i = 0; i < inv.getContainerSize() && !toGive.isEmpty(); i++) {
            if (inv.getItem(i).isEmpty()) {
                int put = Math.min(toGive.getCount(), toGive.getMaxStackSize());
                inv.setItem(i, toGive.split(put));
            }
        }

        return toGive;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RitualPedestalBlockEntity pedestal))
        {
            return InteractionResult.PASS;
        }
        if (pedestal.isFulfilled()) return InteractionResult.PASS;

        ItemStack heldStack = player.getMainHandItem();

        if (level.isClientSide()) {
            return heldStack.isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }

        if (heldStack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ItemStack removed = pedestal.removeFirstNonEmpty();
                if (!removed.isEmpty()) {
                    ItemStack leftover = mergeIntoPlayerInventory(player, removed);
                    if (!leftover.isEmpty()) {
                        if (!player.getInventory().add(leftover)) {
                            player.drop(leftover, false);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            boolean movedAny = false;
            ItemStack prototype = heldStack.copy();

            ItemStack mainItem = player.getMainHandItem();
            if (!mainItem.isEmpty() && ItemStack.isSameItem(mainItem, prototype)) {
                ItemStack mainCopy = mainItem.copy();
                ItemStack leftover = pedestal.insertStack(mainCopy);
                player.setItemInHand(InteractionHand.MAIN_HAND, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                movedAny |= leftover.getCount() < mainCopy.getCount() || leftover.isEmpty();
            }

            ItemStack offItem = player.getOffhandItem();
            if (!offItem.isEmpty() && ItemStack.isSameItem(offItem, prototype)) {
                ItemStack offCopy = offItem.copy();
                ItemStack leftover = pedestal.insertStack(offCopy);
                player.setItemInHand(InteractionHand.OFF_HAND, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                movedAny |= leftover.getCount() < offCopy.getCount() || leftover.isEmpty();
            }

            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack slot = inv.getItem(i);
                if (!slot.isEmpty() && ItemStack.isSameItem(slot, prototype)) {
                    ItemStack slotCopy = slot.copy();
                    ItemStack leftover = pedestal.insertStack(slotCopy);
                    inv.setItem(i, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                    movedAny |= leftover.getCount() < slotCopy.getCount() || leftover.isEmpty();
                }
            }

            if (movedAny) {
                level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        ItemStack toInsert = heldStack.copyWithCount(1);
        ItemStack leftoverSingle = pedestal.insertStack(toInsert);
        if (leftoverSingle.isEmpty()) {
            heldStack.shrink(1);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}