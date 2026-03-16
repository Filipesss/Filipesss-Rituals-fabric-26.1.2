package net.filipes.rituals.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup RITUALS_ITEMS = Registry.register(Registries.ITEM_GROUP, Identifier.of(Rituals.MOD_ID, "rituals_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.HANDLE))
                    .displayName(Text.translatable("itemgroup.rituals.rituals_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.HANDLE);
                        entries.add(ModBlocks.BLOCK_TEST);
                        entries.add(ModBlocks.RITUAL_PEDESTAL);
                        entries.add(ModBlocks.RAW_ROSEGOLD_BLOCK);
                        entries.add(ModBlocks.ROSEGOLD_BLOCK);
                        entries.add(ModItems.ROSEGOLD_INGOT);
                        entries.add(ModItems.ROSEGOLD_PICKAXE);
                        entries.add(ModItems.RAW_ROSEGOLD);

                    })
                    .build());

    public static void registerItemGroups() {
        Rituals.LOGGER.info("Registering item groups");
    }

}
