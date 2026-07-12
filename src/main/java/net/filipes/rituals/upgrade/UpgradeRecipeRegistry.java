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
        register(ModItems.ROSEGOLD_PICKAXE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(ModItems.ROSEGOLD_INGOT, 6),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32),
                new IngredientRequirement(Items.GOLD_BLOCK, 8)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 1),
                new IngredientRequirement(Items.ANCIENT_DEBRIS, 8),
                new IngredientRequirement(Items.COPPER_BLOCK.waxed().unaffected(), 16)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.AMETHYST_BLOCK, 16),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 4),
                new IngredientRequirement(ModItems.ROSEGOLD_INGOT, 8)
        )));
        register(ModItems.ROSEGOLD_PICKAXE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(ModItems.ROSEGOLD_INGOT, 16),
                new IngredientRequirement(Items.OMINOUS_TRIAL_KEY, 4),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 6),
                new IngredientRequirement(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2)
        )));



        register(ModItems.LIGHTNING_RAPIER, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.COPPER_BLOCK.weathering().unaffected(), 16),
                new IngredientRequirement(Items.QUARTZ_BLOCK, 32),
                new IngredientRequirement(Items.GLOWSTONE, 48),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32)
        )));

        register(ModItems.LIGHTNING_RAPIER, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.GOLD_BLOCK, 12),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 4),
                new IngredientRequirement(Items.BLAZE_ROD, 24),
                new IngredientRequirement(Items.AMETHYST_SHARD, 48)
        )));

        register(ModItems.LIGHTNING_RAPIER, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.BREEZE_ROD, 24),
                new IngredientRequirement(Items.LIGHTNING_ROD.weathering().unaffected(), 16),
                new IngredientRequirement(Items.COPPER_BLOCK.weathering().unaffected(), 32),
                new IngredientRequirement(Items.REDSTONE_BLOCK, 32)
        )));

        register(ModItems.LIGHTNING_RAPIER, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.NETHER_QUARTZ_ORE, 32),
                new IngredientRequirement(Items.BREEZE_ROD, 24),
                new IngredientRequirement(Items.GOLD_BLOCK, 16),
                new IngredientRequirement(Items.COPPER_BULB.weathering().unaffected(), 8)
        )));

        register(ModItems.LIGHTNING_RAPIER, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.NETHER_STAR, 1),
                new IngredientRequirement(Items.LIGHTNING_ROD.waxed().unaffected(), 32),
                new IngredientRequirement(Items.COPPER_BULB.waxed().oxidized(), 8)

        )));



        register(ModItems.PULSE_BLASTER, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.REDSTONE_BLOCK, 16),
                new IngredientRequirement(Items.IRON_BLOCK, 12),
                new IngredientRequirement(Items.PISTON, 16)
        )));

        register(ModItems.PULSE_BLASTER, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.OBSERVER, 32),
                new IngredientRequirement(Items.DISPENSER, 24),
                new IngredientRequirement(Items.COPPER_BLOCK.waxed().unaffected(), 24),
                new IngredientRequirement(Items.REDSTONE, 32)

        )));

        register(ModItems.PULSE_BLASTER, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.STICKY_PISTON, 24),
                new IngredientRequirement(Items.SLIME_BLOCK, 16),
                new IngredientRequirement(Items.REDSTONE_BLOCK, 24)
        )));

        register(ModItems.PULSE_BLASTER, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.HEAVY_CORE, 1),
                new IngredientRequirement(Items.COPPER_BULB.waxed().unaffected(), 16),
                new IngredientRequirement(Items.OBSERVER, 32),
                new IngredientRequirement(Items.IRON_BLOCK, 24)
        )));

        register(ModItems.PULSE_BLASTER, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.SCULK_CATALYST, 24),
                new IngredientRequirement(Items.NETHER_STAR, 1),
                new IngredientRequirement(Items.REDSTONE_BLOCK, 32),
                new IngredientRequirement(Items.COPPER_BULB.weathering().unaffected(), 32),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 8)
        )));


        register(ModItems.CINDERBOLT, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.FIRE_CHARGE, 16),
                new IngredientRequirement(Items.COAL_BLOCK, 32),
                new IngredientRequirement(Items.BLAZE_POWDER, 16),
                new IngredientRequirement(Items.DIAMOND, 20)
        )));

        register(ModItems.CINDERBOLT, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.BLAZE_ROD, 8),
                new IngredientRequirement(Items.MAGMA_CREAM, 16),
                new IngredientRequirement(Items.GLOWSTONE, 32),
                new IngredientRequirement(Items.NETHERITE_INGOT, 2)
        )));

        register(ModItems.CINDERBOLT, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.NETHER_WART, 32),
                new IngredientRequirement(Items.MAGMA_BLOCK, 32),
                new IngredientRequirement(Items.FIRE_CHARGE, 32),
                new IngredientRequirement(Items.HONEY_BLOCK, 12)
        )));

        register(ModItems.CINDERBOLT, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.BLAZE_ROD, 16),
                new IngredientRequirement(Items.GHAST_TEAR, 4),
                new IngredientRequirement(Items.OBSIDIAN, 32)
        )));

        register(ModItems.CINDERBOLT, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.GOLD_BLOCK, 24),
                new IngredientRequirement(Items.GHAST_TEAR, 8),
                new IngredientRequirement(Items.BLAZE_POWDER, 64),
                new IngredientRequirement(Items.CRYING_OBSIDIAN, 24),
                new IngredientRequirement(Items.HONEYCOMB_BLOCK, 12)
        )));

        register(ModItems.CINDERBOLT, 6, new UpgradeRecipe(7, List.of(
                new IngredientRequirement(Items.TOTEM_OF_UNDYING, 1),
                new IngredientRequirement(Items.BLAZE_ROD, 32),
                new IngredientRequirement(Items.GHAST_TEAR, 16),
                new IngredientRequirement(Items.MAGMA_BLOCK, 64),
                new IngredientRequirement(Items.NETHERITE_INGOT, 2)
        )));


        register(ModItems.LUNAR_BLADE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.AMETHYST_SHARD, 32),
                new IngredientRequirement(Items.GLOW_INK_SAC, 16),
                new IngredientRequirement(Items.PRISMARINE_SHARD, 32),
                new IngredientRequirement(Items.HONEYCOMB_BLOCK, 12)
        )));

        register(ModItems.LUNAR_BLADE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.AMETHYST_BLOCK, 16),
                new IngredientRequirement(Items.NAUTILUS_SHELL, 8),
                new IngredientRequirement(Items.ENDER_PEARL, 16)
        )));

        register(ModItems.LUNAR_BLADE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.ECHO_SHARD, 2),
                new IngredientRequirement(Items.PEARLESCENT_FROGLIGHT, 8),
                new IngredientRequirement(Items.QUARTZ_BLOCK, 32),
                new IngredientRequirement(Items.BREEZE_ROD, 16)
        )));

        register(ModItems.LUNAR_BLADE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.HEART_OF_THE_SEA, 1),
                new IngredientRequirement(Items.ECHO_SHARD, 4),
                new IngredientRequirement(Items.CRYING_OBSIDIAN, 16)
        )));

        register(ModItems.LUNAR_BLADE, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.HEAVY_CORE, 1),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 8),
                new IngredientRequirement(Items.SEA_LANTERN, 16),
                new IngredientRequirement(Items.END_CRYSTAL, 4)
        )));



        register(ModItems.SOLAR_BLADE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.GOLD_INGOT, 32),
                new IngredientRequirement(Items.GLOWSTONE_DUST, 64),
                new IngredientRequirement(Items.SUNFLOWER, 32),
                new IngredientRequirement(Items.HONEY_BLOCK, 14)
        )));

        register(ModItems.SOLAR_BLADE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.GOLD_BLOCK, 8),
                new IngredientRequirement(Items.BLAZE_ROD, 16),
                new IngredientRequirement(Items.OCHRE_FROGLIGHT, 8)
        )));

        register(ModItems.SOLAR_BLADE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.BLAZE_ROD, 16),
                new IngredientRequirement(Items.MAGMA_CREAM, 16),
                new IngredientRequirement(Items.GLOWSTONE, 32),
                new IngredientRequirement(Items.DIAMOND, 27)
        )));

        register(ModItems.SOLAR_BLADE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.NETHERITE_INGOT, 3),
                new IngredientRequirement(Items.OCHRE_FROGLIGHT, 16),
                new IngredientRequirement(Items.GOLD_BLOCK, 16),
                new IngredientRequirement(Items.HONEYCOMB_BLOCK, 12)
        )));

        register(ModItems.SOLAR_BLADE, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.BEACON, 1),
                new IngredientRequirement(Items.TRIAL_KEY, 12),
                new IngredientRequirement(Items.IRON_BLOCK, 16),
                new IngredientRequirement(Items.GOLD_BLOCK, 32)
        )));



        register(ModItems.VORTEX_EDGE, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.OBSIDIAN, 32),
                new IngredientRequirement(Items.REDSTONE_BLOCK, 16),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32)
        )));

        register(ModItems.VORTEX_EDGE, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.ENDER_PEARL, 16),
                new IngredientRequirement(Items.CONDUIT, 1),
                new IngredientRequirement(Items.CRYING_OBSIDIAN, 16),
                new IngredientRequirement(Items.SCULK_CATALYST, 16)
        )));

        register(ModItems.VORTEX_EDGE, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.SCULK_SENSOR, 16),
                new IngredientRequirement(Items.SCULK, 64),
                new IngredientRequirement(Items.ENDER_EYE, 16)
        )));

        register(ModItems.VORTEX_EDGE, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.ECHO_SHARD, 2),
                new IngredientRequirement(Items.END_CRYSTAL, 4),
                new IngredientRequirement(Items.CRYING_OBSIDIAN, 32),
                new IngredientRequirement(Items.IRON_BLOCK, 14)
        )));

        register(ModItems.VORTEX_EDGE, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.NETHER_STAR, 1),
                new IngredientRequirement(Items.HEAVY_CORE, 1),
                new IngredientRequirement(Items.END_CRYSTAL, 8),
                new IngredientRequirement(Items.DIAMOND_BLOCK, 4)
        )));



        register(ModItems.BLIGHTSPEAR, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.SPIDER_EYE, 32),
                new IngredientRequirement(Items.COBWEB, 32),
                new IngredientRequirement(Items.FERMENTED_SPIDER_EYE, 8),
                new IngredientRequirement(Items.GOLD_BLOCK, 8)
        )));

        register(ModItems.BLIGHTSPEAR, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.POISONOUS_POTATO, 16),
                new IngredientRequirement(Items.SPIDER_EYE, 64),
                new IngredientRequirement(Items.SLIME_BLOCK, 16),
                new IngredientRequirement(Items.POTENT_SULFUR, 16)
        )));

        register(ModItems.BLIGHTSPEAR, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.NETHER_WART, 32),
                new IngredientRequirement(Items.ROTTEN_FLESH, 64),
                new IngredientRequirement(Items.SPIDER_EYE, 32),
                new IngredientRequirement(Items.GOLD_BLOCK, 12),
                new IngredientRequirement(Items.MANGROVE_ROOTS, 64)
        )));

        register(ModItems.BLIGHTSPEAR, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.WITHER_SKELETON_SKULL, 2),
                new IngredientRequirement(Items.GHAST_TEAR, 4),
                new IngredientRequirement(Items.CREAKING_HEART, 8),
                new IngredientRequirement(Items.CINNABAR_BRICKS, 48)
        )));

        register(ModItems.BLIGHTSPEAR, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.WITHER_ROSE, 16),
                new IngredientRequirement(Items.WITHER_SKELETON_SKULL, 4),
                new IngredientRequirement(Items.CREAKING_HEART, 16),
                new IngredientRequirement(Items.SULFUR_BRICKS, 64),
                new IngredientRequirement(Items.CINNABAR_BRICKS, 48)
        )));


        register(ModItems.TEMPORAL_GLASSREAVER, 1, new UpgradeRecipe(2, List.of(
                new IngredientRequirement(Items.GLASS, 64),
                new IngredientRequirement(Items.CLOCK, 16),
                new IngredientRequirement(Items.AMETHYST_SHARD, 32),
                new IngredientRequirement(Items.EMERALD_BLOCK, 32)
        )));

        register(ModItems.TEMPORAL_GLASSREAVER, 2, new UpgradeRecipe(3, List.of(
                new IngredientRequirement(Items.COPPER_BLOCK.waxed().unaffected(), 16),
                new IngredientRequirement(Items.QUARTZ_BLOCK, 16),
                new IngredientRequirement(Items.AMETHYST_BLOCK, 48)
        )));

        register(ModItems.TEMPORAL_GLASSREAVER, 3, new UpgradeRecipe(4, List.of(
                new IngredientRequirement(Items.BLAZE_ROD, 16),
                new IngredientRequirement(Items.FIRE_CHARGE, 32),
                new IngredientRequirement(Items.AMETHYST_SHARD, 48),
                new IngredientRequirement(Items.BREEZE_ROD, 24),
                new IngredientRequirement(Items.HONEY_BLOCK, 12)
        )));

        register(ModItems.TEMPORAL_GLASSREAVER, 4, new UpgradeRecipe(5, List.of(
                new IngredientRequirement(Items.ECHO_SHARD, 4),
                new IngredientRequirement(Items.END_CRYSTAL, 2),
                new IngredientRequirement(Items.HEART_OF_THE_SEA, 2)
        )));

        register(ModItems.TEMPORAL_GLASSREAVER, 5, new UpgradeRecipe(6, List.of(
                new IngredientRequirement(Items.WITHER_SKELETON_SKULL, 3),
                new IngredientRequirement(Items.GOLD_BLOCK, 32),
                new IngredientRequirement(Items.IRON_BLOCK, 24)
        )));

        register(ModItems.TEMPORAL_GLASSREAVER, 6, new UpgradeRecipe(7, List.of(
                new IngredientRequirement(Items.HEAVY_CORE, 2),
                new IngredientRequirement(Items.NETHER_STAR, 1),
                new IngredientRequirement(Items.NETHERITE_INGOT, 2),
                new IngredientRequirement(Items.DIAMOND, 24)
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


    public static int getMaxStage(Item item) {
        return RECIPES.entrySet().stream()
                .filter(e -> e.getKey().item() == item)
                .mapToInt(e -> e.getValue().getResultStage())
                .max()
                .orElse(0);
    }

    private record UpgradeKey(Item item, int stage) {}
}