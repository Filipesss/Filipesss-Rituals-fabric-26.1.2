package net.filipes.rituals.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {

    public static final CreativeModeTab RITUALS_ITEMS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "rituals_items"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.RITUAL_PEDESTAL))
                    .title(Component.translatable("itemgroup.rituals.rituals_items"))
                    .displayItems((parameters, output) -> {

                        output.accept(ModBlocks.RITUAL_PEDESTAL);
                        output.accept(ModBlocks.RAW_ROSEGOLD_BLOCK);
                        output.accept(ModBlocks.ROSEGOLD_BLOCK);
                        output.accept(ModBlocks.AMETHYST_HOURGLASS);
                        output.accept(ModItems.ROSEGOLD_INGOT);
                        output.accept(ModItems.RAW_ROSEGOLD);
                    })
                    .build());

    public static final CreativeModeTab RITUALS_SPECIAL_ITEMS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "rituals_special_items"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ROSEGOLD_PICKAXE))
                    .title(Component.translatable("itemgroup.rituals.rituals_items"))
                    .displayItems((parameters, output) -> {

                        output.accept(ModItems.ROSEGOLD_PICKAXE);
                        output.accept(ModItems.BLIGHTSPEAR);
                        output.accept(ModItems.LUNAR_BLADE);
                        output.accept(ModItems.SOLAR_BLADE);
                        output.accept(ModItems.VORTEX_EDGE);
                        output.accept(ModItems.SHADOWGUARD);
                        output.accept(ModItems.LIGHTNING_RAPIER);
                        output.accept(ModItems.PULSE_BLASTER);
                        output.accept(ModItems.ROSEGOLD_BOOTS);
                        output.accept(ModItems.ROSEGOLD_LEGGINGS);
                        output.accept(ModItems.ROSEGOLD_CHESTPLATE);
                        output.accept(ModItems.ROSEGOLD_HELMET);

                    })
                    .build());


    public static void registerItemGroups() {
        Rituals.LOGGER.info("Registering creative tabs for " + Rituals.MOD_ID);
    }
}