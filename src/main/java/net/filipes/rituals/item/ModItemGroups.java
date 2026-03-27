package net.filipes.rituals.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {

    // In Mojang Mappings, ItemGroup is CreativeModeTab
    public static final CreativeModeTab RITUALS_ITEMS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "rituals_items"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.HANDLE))
                    // Text.translatable is Component.translatable
                    .title(Component.translatable("itemgroup.rituals.rituals_items"))
                    // .entries is typically .displayItems in Mojang-mapped Fabric API
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.HANDLE);
                        output.accept(ModBlocks.BLOCK_TEST);
                        output.accept(ModBlocks.RITUAL_PEDESTAL);
                        output.accept(ModBlocks.RAW_ROSEGOLD_BLOCK);
                        output.accept(ModBlocks.ROSEGOLD_BLOCK);
                        output.accept(ModItems.ROSEGOLD_INGOT);
                        output.accept(ModItems.ROSEGOLD_PICKAXE);
                        output.accept(ModItems.RAW_ROSEGOLD);
                    })
                    .build());

    public static void registerItemGroups() {
        Rituals.LOGGER.info("Registering item groups for " + Rituals.MOD_ID);
    }
}