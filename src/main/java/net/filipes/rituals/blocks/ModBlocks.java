package net.filipes.rituals.blocks;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.custom.RitualPedestalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block BLOCK_TEST = registerBlock("block_test",
            AbstractBlock.Settings.create()
                    .strength(4f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final Block ROSEGOLD_BLOCK = registerBlock("rosegold_block",
            AbstractBlock.Settings.create()
                    .strength(4f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final Block RAW_ROSEGOLD_BLOCK = registerBlock("raw_rosegold_block",
            AbstractBlock.Settings.create()
                    .strength(4f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final RitualPedestalBlock RITUAL_PEDESTAL = registerRitualPedestal("ritual_pedestal",
            AbstractBlock.Settings.create()
                    .hardness(-1f)
                    .resistance(3600000f)
                    .sounds(BlockSoundGroup.STONE)
                    .nonOpaque()
    );

    private static RitualPedestalBlock registerRitualPedestal(String name, AbstractBlock.Settings blockSettings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Rituals.MOD_ID, name));

        RitualPedestalBlock block = new RitualPedestalBlock(blockSettings.registryKey(key));
        Registry.register(Registries.BLOCK, key, block);
        registerBlockItem(name, block);

        return block;
    }


    private static Block registerBlock(String name, AbstractBlock.Settings blockSettings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Rituals.MOD_ID, name));

        Block block = new Block(blockSettings.registryKey(key));
        Registry.register(Registries.BLOCK, key, block);
        registerBlockItem(name, block);

        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Rituals.MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Settings().registryKey(key));
        Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModBlocks() {
        Rituals.LOGGER.info("Registering mod blocks for " + Rituals.MOD_ID);
    }
}