package net.filipes.rituals.item;

import net.filipes.rituals.Rituals;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {

    public static class Items {

        public static final TagKey<Item> PERSISTENT_DROP = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "persistent_drop")
        );
    }
}