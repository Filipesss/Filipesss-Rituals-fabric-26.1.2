package net.filipes.rituals.upgrade;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KillUpgradeRegistry {

    private static final Map<UpgradeKey, KillUpgradeRecipe> RECIPES = new HashMap<>();


    public static void registerAll() {
        register(ModItems.SHADOWGUARD, 1, new KillUpgradeRecipe(2, 2));
        register(ModItems.SHADOWGUARD, 2, new KillUpgradeRecipe(3, 3));
        register(ModItems.SHADOWGUARD, 3, new KillUpgradeRecipe(4, 5));
        register(ModItems.SHADOWGUARD, 4, new KillUpgradeRecipe(5, 6));
        register(ModItems.SHADOWGUARD, 5, new KillUpgradeRecipe(6, 8));
        register(ModItems.SHADOWGUARD, 6, new KillUpgradeRecipe(7, 10));


        register(ModItems.PHARATHORN, 1, new KillUpgradeRecipe(2, 2));
        register(ModItems.PHARATHORN, 2, new KillUpgradeRecipe(3, 4));
        register(ModItems.PHARATHORN, 3, new KillUpgradeRecipe(4, 6));

    }

    public static void register(Item item, int fromStage, KillUpgradeRecipe recipe) {
        RECIPES.put(new UpgradeKey(item, fromStage), recipe);
    }

    public static Optional<KillUpgradeRecipe> getRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        int stage = ModDataComponents.getStage(stack);
        return Optional.ofNullable(RECIPES.get(new UpgradeKey(stack.getItem(), stage)));
    }

    public static boolean isKillUpgradeable(Item item) {
        return RECIPES.keySet().stream().anyMatch(k -> k.item() == item);
    }

    private record UpgradeKey(Item item, int stage) {}
}