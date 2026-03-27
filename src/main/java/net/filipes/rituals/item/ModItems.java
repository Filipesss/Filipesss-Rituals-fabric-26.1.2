package net.filipes.rituals.item;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.item.custom.LightningRapierItem;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.class_3902;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.text.Text;
import java.util.List;
import net.minecraft.text.TextColor;
import java.util.function.Function;

public class ModItems {

    public static final Item HANDLE = registerItem("handle",
            settings -> new Item(settings),
            new Item.Settings().component(DataComponentTypes.LORE,
                    new LoreComponent(List.of(Text.translatable("tooltip.rituals.handle").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));
    public static final Item ROSEGOLD_INGOT = registerItem("rosegold_ingot",
            settings -> new Item(settings),
            new Item.Settings().component(DataComponentTypes.LORE,
                    new LoreComponent(List.of(Text.translatable("tooltip.rituals.rosegold").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));
    public static final Item RAW_ROSEGOLD = registerItem("raw_rosegold",
            settings -> new Item(settings),
            new Item.Settings().component(DataComponentTypes.LORE,
                    new LoreComponent(List.of(Text.translatable("tooltip.rituals.raw_rosegold").styled(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));

    public static final Item EM_PICK = registerItem("em_pick",
            settings -> new Item(settings),
            new Item.Settings().pickaxe(ModToolMaterials.ROSEGOLD, 1.0F, -2.8F).fireproof());

    public static final Item ROSEGOLD_PICKAXE = registerItem("rosegold_pickaxe",
            settings -> new RosegoldPickaxeItem(ModToolMaterials.ROSEGOLD, 1.0F, -2.8F, settings),
            new Item.Settings().component(DataComponentTypes.UNBREAKABLE, class_3902.field_17274).fireproof().component(DataComponentTypes.LORE,
                    new LoreComponent(List.of(Text.translatable("tooltip.rituals.handle").styled(style -> style.withColor(TextColor.fromRgb(0xFFB6C1)).withItalic(false))))));

    public static final Item LIGHTNING_RAPIER = registerItem("lightning_rapier",
            settings -> new LightningRapierItem(ModToolMaterials.ROSEGOLD, 1.5F, -1.8F, settings),
            new Item.Settings()
                    .maxCount(1)
                    .component(DataComponentTypes.UNBREAKABLE, class_3902.field_17274)
                    .component(DataComponentTypes.LORE,
                            new LoreComponent(List.of(
                                    Text.translatable("tooltip.rituals.lightning_rapier")
                                            .styled(style -> style.withColor(TextColor.fromRgb(0x9B6DFF)).withItalic(false))
                            ))));

    // add this import at top near your other imports


    // Add this constant to ModItems (place near the other public static final Item declarations)
    public static final Item PULSE_BLASTER = registerItem("pulse_blaster",
            settings -> new PulseBlasterItem(settings),
            new Item.Settings()
                    .maxCount(1) // single-handed, non-stackable
                    .fireproof() // optional: keep same as your other special items if desired
                    .component(DataComponentTypes.UNBREAKABLE, class_3902.field_17274)
                    .component(DataComponentTypes.LORE,
                            new LoreComponent(List.of(
                                    Text.translatable("tooltip.rituals.pulse_blaster")
                                            .styled(style -> style.withColor(TextColor.fromRgb(0xFF2626)).withItalic(false))
                            ))));



    /**
     * Creates the registry key on the provided Settings, then constructs the Item via the supplied creator.
     * This guarantees the Item is constructed with a Settings that contains the registry key (prevents NPE).
     */
    private static Item registerItem(String name, Function<Item.Settings, Item> creator, Item.Settings settings) {
        Identifier id = Identifier.of(Rituals.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        settings = settings.registryKey(key);          // apply registry key to settings BEFORE construction
        Item item = creator.apply(settings);          // construct the item with the prepared settings
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void registerModItems() {
        Rituals.LOGGER.info("Registering ModItems");
    }
}