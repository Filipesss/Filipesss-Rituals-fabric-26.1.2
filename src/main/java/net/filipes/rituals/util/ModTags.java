package net.filipes.rituals.util;

import net.filipes.rituals.Rituals;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> NEEDS_ROSEGOLD_TOOL = createTag("needs_rosegold_tool");
        public static final TagKey<Block> INCORRECT_FOR_ROSEGOLD_TOOL = createTag("incorrect_for_rosegold_tool");

        private static TagKey<Block> createTag(String name) {
            // TagKey.create and Identifier.fromNamespaceAndPath are the Mojang Mapping standards
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> ROSEGOLD_REPAIR = createTag("rosegold_repair");

        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name));
        }
    }
}