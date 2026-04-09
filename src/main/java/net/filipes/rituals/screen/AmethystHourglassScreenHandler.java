package net.filipes.rituals.screen;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.upgrade.UpgradeRecipe;
import net.filipes.rituals.upgrade.UpgradeRecipeRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class AmethystHourglassScreenHandler extends AbstractContainerMenu {

    public static final int BUTTON_UPGRADE = 0;

    private final Container inventory;

    public AmethystHourglassScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(2));
    }

    public AmethystHourglassScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
        super(ModMenuTypes.AMETHYST_HOURGLASS, syncId);
        checkContainerSize(inventory, 2);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.player);

        // Slot 0 – input weapon
        this.addSlot(new Slot(inventory, 0, 21, 53));
        // Slot 1 – output weapon
        this.addSlot(new Slot(inventory, 1, 139, 53));

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 130 + row * 18));
        // Hotbar
        for (int col = 0; col < 9; col++)
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 188));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_UPGRADE) {
            return tryUpgrade(player);
        }
        return false;
    }

    private boolean tryUpgrade(Player player) {
        ItemStack input = inventory.getItem(0);
        if (input.isEmpty()) return false;

        Optional<UpgradeRecipe> opt = UpgradeRecipeRegistry.getRecipe(input);
        if (opt.isEmpty()) return false;

        UpgradeRecipe recipe = opt.get();
        if (!recipe.canCraft(player)) return false;

        recipe.consumeIngredients(player);

        ItemStack upgraded = ModDataComponents.withStage(input, recipe.getResultStage());
        inventory.setItem(0, ItemStack.EMPTY);
        inventory.setItem(1, upgraded);

        broadcastChanges();
        return true;
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < 2; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < 2) {
                if (!this.moveItemStackTo(originalStack, 2, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(originalStack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    public Container getInventory() { return inventory; }
}