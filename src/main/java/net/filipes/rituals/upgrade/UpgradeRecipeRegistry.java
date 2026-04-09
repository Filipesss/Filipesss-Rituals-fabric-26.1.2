package net.filipes.rituals.upgrade;

import net.filipes.rituals.component.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class UpgradeRecipeRegistry {

    private static final Map<UpgradeKey, UpgradeRecipe> RECIPES = new HashMap<>();

    /** Call this once during mod initialization to register all upgrade recipes. */
    public static void registerAll() {
        // ── Rosegold Pickaxe ──────────────────────────────────────────────────
        register(net.filipes.rituals.item.ModItems.ROSEGOLD_PICKAXE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(net.minecraft.world.item.Items.DIAMOND, 4),
                new IngredientRequirement(net.minecraft.world.item.Items.AMETHYST_SHARD, 8)
        )));
        register(net.filipes.rituals.item.ModItems.ROSEGOLD_PICKAXE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(net.minecraft.world.item.Items.DIAMOND, 8),
                new IngredientRequirement(net.minecraft.world.item.Items.AMETHYST_SHARD, 16),
                new IngredientRequirement(net.minecraft.world.item.Items.NETHERITE_INGOT, 2)
        )));
        register(net.filipes.rituals.item.ModItems.ROSEGOLD_PICKAXE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(net.minecraft.world.item.Items.NETHERITE_INGOT, 4),
                new IngredientRequirement(net.minecraft.world.item.Items.AMETHYST_SHARD, 32)
        )));
        register(net.filipes.rituals.item.ModItems.ROSEGOLD_PICKAXE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(net.minecraft.world.item.Items.NETHERITE_INGOT, 8),
                new IngredientRequirement(net.minecraft.world.item.Items.AMETHYST_SHARD, 64),
                new IngredientRequirement(net.minecraft.world.item.Items.ECHO_SHARD, 4)
        )));

        // ── Add more weapons here the same way ────────────────────────────────
        // register(ModItems.SOME_SWORD, 1, new UpgradeRecipe(2, List.of(...)));
    }

    public static void register(Item item, int fromStage, UpgradeRecipe recipe) {
        RECIPES.put(new UpgradeKey(item, fromStage), recipe);
    }

    public static Optional<UpgradeRecipe> getRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        int stage = ModDataComponents.getStage(stack);
        return Optional.ofNullable(RECIPES.get(new UpgradeKey(stack.getItem(), stage)));
    }

    // ── Internal key ─────────────────────────────────────────────────────────
    private record UpgradeKey(Item item, int stage) {}
}