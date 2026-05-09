package net.filipes.rituals.upgrade;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UpgradeRecipeRegistry {

    private static final Map<UpgradeKey, UpgradeRecipe> RECIPES = new HashMap<>();

    /** Call this once during mod initialization to register all upgrade recipes. */
    public static void registerAll() {
        register(ModItems.ROSEGOLD_PICKAXE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.DIAMOND, 4),
                new IngredientRequirement(Items.AMETHYST_SHARD, 8)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.DIAMOND, 8),
                new IngredientRequirement(Items.AMETHYST_SHARD, 16),
                new IngredientRequirement(Items.NETHERITE_INGOT, 2)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 4),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 8),
                new IngredientRequirement(Items.AMETHYST_SHARD, 64),
                new IngredientRequirement(Items.ECHO_SHARD, 4)
        )));
        // Stage 1 → 2: Unlock chain lightning  (copper wiring, gold for conductivity)
        register(ModItems.LIGHTNING_RAPIER, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.GOLD_INGOT, 8),
                new IngredientRequirement(Items.IRON_CHAIN, 4),
                new IngredientRequirement(Items.COPPER_INGOT, 12)
        )));

        // Stage 2 → 3: Unlock supercharge system  (amethyst resonance + lightning rod)
        register(ModItems.LIGHTNING_RAPIER, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.AMETHYST_SHARD, 16),
                new IngredientRequirement(Items.LIGHTNING_ROD, 2),
                new IngredientRequirement(Items.GOLD_INGOT, 12)
        )));

        // Stage 3 → 4: Unlock instant charge ability  (netherite focus + echo)
        register(ModItems.LIGHTNING_RAPIER, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 3),
                new IngredientRequirement(Items.ECHO_SHARD, 2),
                new IngredientRequirement(Items.AMETHYST_SHARD, 24),
                new IngredientRequirement(Items.LIGHTNING_ROD, 1)
        )));

        // Stage 4 → 5: Unlock dash with charge bonus  (wind charge + nether)
        register(ModItems.LIGHTNING_RAPIER, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 6),
                new IngredientRequirement(Items.WIND_CHARGE, 8),
                new IngredientRequirement(Items.ECHO_SHARD, 4)
        )));

        // Stage 5 → 6: Unlock stun on hit  (heavy core, the rarest upgrade)
        register(ModItems.LIGHTNING_RAPIER, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.HEAVY_CORE, 1),
                new IngredientRequirement(Items.NETHERITE_INGOT, 8),
                new IngredientRequirement(Items.ECHO_SHARD, 6),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32)
        )));

    }

    public static void register(Item item, int fromStage, UpgradeRecipe recipe) {
        RECIPES.put(new UpgradeKey(item, fromStage), recipe);
    }

    public static Optional<UpgradeRecipe> getRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        int stage = ModDataComponents.getStage(stack);
        return Optional.ofNullable(RECIPES.get(new UpgradeKey(stack.getItem(), stage)));
    }

    private record UpgradeKey(Item item, int stage) {}
}
