package net.filipes.rituals.effect;

import net.filipes.rituals.Rituals;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModStatusEffects {

    public static final RegistryEntry<StatusEffect> STUN = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(Rituals.MOD_ID, "stun"),
            new StunEffect()
    );

    public static void registerModStatusEffects() {
        Rituals.LOGGER.info("Registering ModStatusEffects");
    }
}