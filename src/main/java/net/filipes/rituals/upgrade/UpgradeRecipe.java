package net.filipes.rituals.upgrade;

import net.minecraft.world.entity.player.Player;
import java.util.List;

public class UpgradeRecipe {

    private final int resultStage;
    private final List<IngredientRequirement> ingredients;

    public UpgradeRecipe(int resultStage, List<IngredientRequirement> ingredients) {
        this.resultStage = resultStage;
        this.ingredients = List.copyOf(ingredients);
    }

    public int getResultStage() { return resultStage; }
    public List<IngredientRequirement> getIngredients() { return ingredients; }

    public boolean canCraft(Player player) {
        return ingredients.stream().allMatch(r -> r.isSatisfied(player));
    }

    public void consumeIngredients(Player player) {
        ingredients.forEach(r -> r.consume(player));
    }
}