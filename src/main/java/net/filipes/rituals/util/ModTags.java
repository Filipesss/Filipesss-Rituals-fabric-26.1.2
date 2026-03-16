package net.filipes.rituals.util;

import net.filipes.rituals.Rituals;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> NEEDS_ROSEGOLD_TOOL = createTag("needs_emerald_tool");
        public static final TagKey<Block> INCORRECT_FOR_ROSEGOLD_TOOL = createTag("incorrect_for_emerald_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Rituals.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> ROSEGOLD_REPAIR = createTag("rosegold_repair");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Rituals.MOD_ID, name));
        }
    }


}
