package net.filipes.rituals.pedestal;

import net.filipes.rituals.blocks.ModBlocks;
import net.filipes.rituals.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PedestalTypes {

    private static final Map<String, PedestalType> REGISTRY_INTERNAL = new LinkedHashMap<>();
    public  static final Map<String, PedestalType> REGISTRY =
            Collections.unmodifiableMap(REGISTRY_INTERNAL);

    public static final PedestalType ROSEGOLD_PICKAXE = register(new PedestalType(
            "rosegold_pickaxe_pedestal",
            Map.ofEntries(
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 3),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 6),
                    Map.entry(Items.NETHERITE_INGOT, 3),
                    Map.entry(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.AMETHYST_SHARD, 24),
                    Map.entry(Items.GOLD_BLOCK, 16)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.ROSEGOLD_PICKAXE);
                Registry<Enchantment> enchReg = access.lookupOrThrow(Registries.ENCHANTMENT);
                ItemEnchantments.Mutable ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                ench.set(enchReg.getOrThrow(Enchantments.EFFICIENCY), 5);
                stack.set(DataComponents.ENCHANTMENTS, ench.toImmutable());
                return stack;
            }
    ));
    public static final PedestalType ROSEGOLD_HELMET = register(new PedestalType(
            "rosegold_helmet_pedestal",
            Map.ofEntries(
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 5),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 10),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.AMETHYST_SHARD, 24),
                    Map.entry(Items.GOLD_BLOCK, 20)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.ROSEGOLD_HELMET);
                return stack;
            }
    ));
    public static final PedestalType ROSEGOLD_CHESTPLATE = register(new PedestalType(
            "rosegold_chestplate_pedestal",
            Map.ofEntries(
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 7),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 12),
                    Map.entry(Items.NETHERITE_INGOT, 5),
                    Map.entry(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 3),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.AMETHYST_SHARD, 24),
                    Map.entry(Items.GOLD_BLOCK, 24)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.ROSEGOLD_CHESTPLATE);
                return stack;
            }
    ));
    public static final PedestalType ROSEGOLD_LEGGINGS = register(new PedestalType(
            "rosegold_leggings_pedestal",
            Map.ofEntries(
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 6),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 10),
                    Map.entry(Items.NETHERITE_INGOT, 5),
                    Map.entry(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 3),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.AMETHYST_SHARD, 24),
                    Map.entry(Items.GOLD_BLOCK, 24)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.ROSEGOLD_LEGGINGS);
                return stack;
            }
    ));
    public static final PedestalType ROSEGOLD_BOOTS = register(new PedestalType(
            "rosegold_boots_pedestal",
            Map.ofEntries(
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 4),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 8),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.AMETHYST_SHARD, 24),
                    Map.entry(Items.GOLD_BLOCK, 20)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.ROSEGOLD_BOOTS);
                return stack;
            }
    ));
    public static final PedestalType LIGHTNING_RAPIER = register(new PedestalType(
            "lightning_rapier_pedestal",
            Map.ofEntries(
                    Map.entry(Items.PLAYER_HEAD, 1),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 32),
                    Map.entry(Items.LIGHTNING_ROD.waxed().unaffected(), 64),
                    Map.entry(Items.QUARTZ_BLOCK, 32),
                    Map.entry(Items.GOLD_BLOCK, 16),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.SKELETON_SKULL, 1),
                    Map.entry(Items.LIGHTNING_ROD.waxed().oxidized(), 16)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.LIGHTNING_RAPIER);
                return stack;
            }
    ));
    public static final PedestalType SOLAR_BLADE = register(new PedestalType(
            "solar_blade_pedestal",
            Map.ofEntries(
                    Map.entry(Items.NETHER_STAR, 1),
                    Map.entry(Items.BLAZE_ROD, 32),
                    Map.entry(Items.SUNFLOWER, 64),
                    Map.entry(Items.GOLD_BLOCK, 32),
                    Map.entry(Items.OCHRE_FROGLIGHT, 8),
                    Map.entry(Items.GLOWSTONE, 32),
                    Map.entry(Items.PLAYER_HEAD, 1),
                    Map.entry(Items.NETHERITE_INGOT, 2)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.SOLAR_BLADE);
                return stack;
            }
    ));
    public static final PedestalType LUNAR_BLADE = register(new PedestalType(
            "lunar_blade_pedestal",
            Map.ofEntries(
                    Map.entry(Items.ECHO_SHARD, 4),
                    Map.entry(Items.NETHER_STAR, 1),
                    Map.entry(Items.AMETHYST_SHARD, 32),
                    Map.entry(Items.AMETHYST_BLOCK, 64),
                    Map.entry(Items.DIAMOND_BLOCK, 12),
                    Map.entry(Items.PEARLESCENT_FROGLIGHT, 8),
                    Map.entry(Items.PLAYER_HEAD, 1),
                    Map.entry(Items.NETHERITE_INGOT, 2)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.LUNAR_BLADE);
                return stack;
            }
    ));
    public static final PedestalType VORTEX_EDGE = register(new PedestalType(
            "vortex_edge_pedestal",
            Map.ofEntries(
                    Map.entry(Items.ECHO_SHARD, 6),
                    Map.entry(Items.SCULK_CATALYST, 24),
                    Map.entry(Items.SCULK_SHRIEKER, 32),
                    Map.entry(ModItems.ROSEGOLD_INGOT, 4),
                    Map.entry(Items.NETHERITE_INGOT, 6),
                    Map.entry(Items.ENDER_EYE, 16),
                    Map.entry(Items.END_CRYSTAL, 4),
                    Map.entry(Items.PLAYER_HEAD, 2)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.VORTEX_EDGE);
                return stack;
            }
    ));
    public static final PedestalType SHADOWGUARD = register(new PedestalType(
            "shadowguard_pedestal",
            Map.ofEntries(
                    Map.entry(Items.HEAVY_CORE, 3),
                    Map.entry(Items.BREEZE_ROD, 16),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.PHANTOM_MEMBRANE, 3),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(Items.DIAMOND_BLOCK, 6),
                    Map.entry(Items.IRON_BLOCK, 16),
                    Map.entry(Items.GHAST_TEAR, 4),
                    Map.entry(Items.OMINOUS_TRIAL_KEY, 12)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.SHADOWGUARD);
                return stack;
            }
    ));
    public static final PedestalType DEPTHSTRIKE = register(new PedestalType(
            "depthstrike_pedestal",
            Map.ofEntries(
                    Map.entry(Items.TRIDENT, 1),
                    Map.entry(Items.CONDUIT, 1),
                    Map.entry(Items.NAUTILUS_SHELL, 4),
                    Map.entry(Items.SPONGE, 16),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(Items.PRISMARINE_BRICKS, 64),
                    Map.entry(Items.PRISMARINE_SHARD, 32),
                    Map.entry(Items.HEART_OF_THE_SEA, 2),
                    Map.entry(Items.NETHERITE_INGOT, 3)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.DEPTHSTRIKE);
                return stack;
            }
    ));
    public static final PedestalType BLIGHTSPEAR = register(new PedestalType(
            "blightspear_pedestal",
            Map.ofEntries(
                    Map.entry(Items.CINNABAR, 64),
                    Map.entry(Items.SULFUR, 64),
                    Map.entry(Items.POTENT_SULFUR, 16),
                    Map.entry(Items.COBWEB, 48),
                    Map.entry(Items.NETHERITE_SPEAR, 1),
                    Map.entry(Items.PLAYER_HEAD, 1),
                    Map.entry(Items.CREAKING_HEART, 8),
                    Map.entry(Items.GOLD_BLOCK, 24),
                    Map.entry(Items.ROTTEN_FLESH, 32)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.BLIGHTSPEAR);
                return stack;
            }
    ));
    public static final PedestalType TEMPORAL_GLASSREAVER = register(new PedestalType(
            "temporal_glassreaver_pedestal",
            Map.ofEntries(
                    Map.entry(Items.GLASS, 64),
                    Map.entry(Items.CLOCK, 24),
                    Map.entry(Items.AMETHYST_SHARD, 64),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.IRON_BLOCK, 16),
                    Map.entry(Items.ECHO_SHARD, 6),
                    Map.entry(Items.CRYING_OBSIDIAN, 16)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.TEMPORAL_GLASSREAVER);
                return stack;
            }
    ));
    public static final PedestalType CINDERBOLT = register(new PedestalType(
            "cinderbolt_pedestal",
            Map.ofEntries(
                    Map.entry(Items.CROSSBOW, 1),
                    Map.entry(Items.BLAZE_ROD, 32),
                    Map.entry(Items.PLAYER_HEAD, 1),
                    Map.entry(Items.FIRE_CHARGE, 16),
                    Map.entry(Items.CAMPFIRE, 8),
                    Map.entry(Items.NETHER_WART, 24),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.RESIN_BLOCK, 16)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.CINDERBOLT);
                return stack;
            }
    ));
    public static final PedestalType PULSE_BLASTER = register(new PedestalType(
            "pulse_blaster_pedestal",
            Map.ofEntries(
                    Map.entry(Items.REDSTONE_BLOCK, 48),
                    Map.entry(Items.PISTON, 24),
                    Map.entry(Items.STICKY_PISTON, 12),
                    Map.entry(Items.DISPENSER, 32),
                    Map.entry(Items.IRON_BLOCK, 48),
                    Map.entry(Items.NETHERITE_INGOT, 3),
                    Map.entry(Items.DIAMOND_BLOCK, 12),
                    Map.entry(Items.COPPER_BLOCK.waxed().unaffected(), 64),
                    Map.entry(Items.HEAVY_CORE, 2),
                    Map.entry(Items.BLAZE_POWDER, 24),
                    Map.entry(Items.OBSERVER, 32),
                    Map.entry(Items.SLIME_BLOCK, 20)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.PULSE_BLASTER);
                return stack;
            }
    ));
    public static final PedestalType PHARATHORN = register(new PedestalType(
            "pharathorn_pedestal",
            Map.ofEntries(
                    Map.entry(Items.ANCIENT_DEBRIS, 24),
                    Map.entry(Items.DIAMOND_BLOCK, 16),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(Items.TURTLE_HELMET, 1),
                    Map.entry(Items.TERRACOTTA, 64),
                    Map.entry(Items.GOLD_BLOCK, 24),
                    Map.entry(Items.OBSIDIAN, 16),
                    Map.entry(Items.HEART_OF_THE_SEA, 2)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.PHARATHORN);
                return stack;
            }
    ));
    public static final PedestalType POLARITY_BOW = register(new PedestalType(
            "polarity_bow_pedestal",
            Map.ofEntries(
                    Map.entry(Items.REDSTONE_BLOCK, 32),
                    Map.entry(Items.LAPIS_BLOCK, 32),
                    Map.entry(Items.NETHERITE_INGOT, 4),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(Items.IRON_BLOCK, 48),
                    Map.entry(Items.LODESTONE, 16),
                    Map.entry(Items.LIGHTNING_ROD.waxed().unaffected(), 24),
                    Map.entry(Items.COMPASS, 4)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.POLARITY_BOW);
                return stack;
            }
    ));
    public static final PedestalType SHADESHATTER = register(new PedestalType(
            "shadeshatter_pedestal",
            Map.ofEntries(
                    Map.entry(Items.NETHERITE_BLOCK, 1),
                    Map.entry(Items.DIAMOND_BLOCK, 12),
                    Map.entry(Items.AMETHYST_SHARD, 48),
                    Map.entry(Items.AMETHYST_BLOCK, 64),
                    Map.entry(Items.CALIBRATED_SCULK_SENSOR, 16),
                    Map.entry(Items.ECHO_SHARD, 8),
                    Map.entry(Items.RECOVERY_COMPASS, 1),
                    Map.entry(Items.PLAYER_HEAD, 2),
                    Map.entry(ModBlocks.item(ModBlocks.ROSEGOLD_BLOCK), 2)
            ),
            access -> {
                ItemStack stack = new ItemStack(ModItems.SHADESHATTER);
                return stack;
            }
    ));



    private static PedestalType register(PedestalType type) {
        REGISTRY_INTERNAL.put(type.id(), type);
        return type;
    }

    public static @Nullable PedestalType byId(String id) {
        return REGISTRY_INTERNAL.get(id);
    }
}