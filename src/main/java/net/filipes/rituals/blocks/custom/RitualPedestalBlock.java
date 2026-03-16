package net.filipes.rituals.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RitualPedestalBlock extends BlockWithEntity {

    public RitualPedestalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        return (w, pos, s, be) -> {
            if (be instanceof RitualPedestalBlockEntity pedestal) {
                pedestal.tick(w);
            }
        };
    }
    private ItemStack mergeIntoPlayerInventory(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        net.minecraft.entity.player.PlayerInventory inv = player.getInventory();
        ItemStack toGive = stack.copy();

        for (int i = 0; i < inv.size() && !toGive.isEmpty(); i++) {
            ItemStack slot = inv.getStack(i);
            if (!slot.isEmpty() && ItemStack.areItemsEqual(slot, toGive)) {
                int slotMax = Math.min(slot.getMaxCount(), slot.getItem().getMaxCount());
                int space = slotMax - slot.getCount();
                if (space > 0) {
                    int put = Math.min(space, toGive.getCount());
                    slot.increment(put);
                    toGive.decrement(put);
                    inv.setStack(i, slot);
                }
            }
        }

        for (int i = 0; i < inv.size() && !toGive.isEmpty(); i++) {
            if (inv.getStack(i).isEmpty()) {
                int put = Math.min(toGive.getCount(), toGive.getMaxCount());
                inv.setStack(i, toGive.split(put));
            }
        }

        return toGive;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof RitualPedestalBlockEntity pedestal)) {
            return ActionResult.PASS;
        }

        ItemStack heldStack = player.getMainHandStack();

        if (world.isClient()) {
            return heldStack.isEmpty() ? ActionResult.PASS : ActionResult.SUCCESS;
        }

        if (heldStack.isEmpty()) {
            if (player.isSneaking()) {
                ItemStack removed = pedestal.removeFirstNonEmpty();
                if (!removed.isEmpty()) {

                    ItemStack leftover = mergeIntoPlayerInventory(player, removed);
                    if (!leftover.isEmpty()) {

                        player.getInventory().offerOrDrop(leftover);
                    }
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP,
                            net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        }

        if (player.isSneaking()) {
            boolean movedAny = false;

            ItemStack prototype = heldStack.copy();

            if (!player.getMainHandStack().isEmpty() && ItemStack.areItemsEqual(player.getMainHandStack(), prototype)) {
                ItemStack mainCopy = player.getMainHandStack().copy();
                ItemStack leftover = pedestal.insertStack(mainCopy);
                player.setStackInHand(Hand.MAIN_HAND, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                movedAny |= leftover.getCount() < player.getMainHandStack().getCount() || leftover.isEmpty();
            }

            if (!player.getOffHandStack().isEmpty() && ItemStack.areItemsEqual(player.getOffHandStack(), prototype)) {
                ItemStack offCopy = player.getOffHandStack().copy();
                ItemStack leftover = pedestal.insertStack(offCopy);
                player.setStackInHand(Hand.OFF_HAND, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                movedAny |= leftover.getCount() < player.getOffHandStack().getCount() || leftover.isEmpty();
            }

            net.minecraft.entity.player.PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack slot = inv.getStack(i);
                if (!slot.isEmpty() && ItemStack.areItemsEqual(slot, prototype)) {
                    ItemStack leftover = pedestal.insertStack(slot.copy());
                    inv.setStack(i, leftover.isEmpty() ? ItemStack.EMPTY : leftover);
                    movedAny |= leftover.getCount() < slot.getCount() || leftover.isEmpty();
                }
            }

            if (movedAny) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        ItemStack toInsert = heldStack.copyWithCount(1);
        ItemStack leftoverSingle = pedestal.insertStack(toInsert);
        if (leftoverSingle.isEmpty()) {
            heldStack.decrement(1);
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}