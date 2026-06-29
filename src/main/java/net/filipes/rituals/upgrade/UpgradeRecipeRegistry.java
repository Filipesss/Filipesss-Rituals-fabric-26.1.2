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

    public static void registerAll() {
        register(ModItems.ROSEGOLD_PICKAXE, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.ROSEGOLD_PICKAXE, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.ROSEGOLD_PICKAXE, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.ROSEGOLD_PICKAXE, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.LIGHTNING_RAPIER, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LIGHTNING_RAPIER, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LIGHTNING_RAPIER, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LIGHTNING_RAPIER, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LIGHTNING_RAPIER, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.PULSE_BLASTER, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.PULSE_BLASTER, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.PULSE_BLASTER, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.PULSE_BLASTER, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.PULSE_BLASTER, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.CINDERBOLT, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.CINDERBOLT, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.CINDERBOLT, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.CINDERBOLT, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.CINDERBOLT, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.CINDERBOLT, 6, new UpgradeRecipe(7, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.LUNAR_BLADE, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LUNAR_BLADE, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LUNAR_BLADE, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LUNAR_BLADE, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.LUNAR_BLADE, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.SOLAR_BLADE, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.SOLAR_BLADE, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.SOLAR_BLADE, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.SOLAR_BLADE, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.SOLAR_BLADE, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.VORTEX_EDGE, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.VORTEX_EDGE, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.VORTEX_EDGE, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.VORTEX_EDGE, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.VORTEX_EDGE, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.BLIGHTSPEAR, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.BLIGHTSPEAR, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.BLIGHTSPEAR, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.BLIGHTSPEAR, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.BLIGHTSPEAR, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));

        register(ModItems.TEMPORAL_GLASSREAVER, 1, new UpgradeRecipe(2, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.TEMPORAL_GLASSREAVER, 2, new UpgradeRecipe(3, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.TEMPORAL_GLASSREAVER, 3, new UpgradeRecipe(4, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.TEMPORAL_GLASSREAVER, 4, new UpgradeRecipe(5, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.TEMPORAL_GLASSREAVER, 5, new UpgradeRecipe(6, List.of(new IngredientRequirement(Items.DIRT, 1))));
        register(ModItems.TEMPORAL_GLASSREAVER, 6, new UpgradeRecipe(7, List.of(new IngredientRequirement(Items.DIRT, 1))));
    }

    public static void register(Item item, int fromStage, UpgradeRecipe recipe) {
        RECIPES.put(new UpgradeKey(item, fromStage), recipe);
    }

    public static Optional<UpgradeRecipe> getRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        int stage = ModDataComponents.getStage(stack);
        return Optional.ofNullable(RECIPES.get(new UpgradeKey(stack.getItem(), stage)));
    }

    /**
     * Returns the highest resultStage registered for this item, or 0 if the item
     * has no ingredient-upgrade recipes.
     */
    public static int getMaxStage(Item item) {
        return RECIPES.entrySet().stream()
                .filter(e -> e.getKey().item() == item)
                .mapToInt(e -> e.getValue().getResultStage())
                .max()
                .orElse(0);
    }

    private record UpgradeKey(Item item, int stage) {}
}