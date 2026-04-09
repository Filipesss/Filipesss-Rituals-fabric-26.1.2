package net.filipes.rituals.screen;

import net.filipes.rituals.Rituals;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    public static MenuType<AmethystHourglassScreenHandler> AMETHYST_HOURGLASS;

    public static void registerMenuTypes() {
        AMETHYST_HOURGLASS = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "amethyst_hourglass"),
                new MenuType<>(AmethystHourglassScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
        );
        Rituals.LOGGER.info("Registering menu types for " + Rituals.MOD_ID);
    }
}