package net.filipes.rituals.effect;

import net.filipes.rituals.Rituals;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class ModStatusEffects {

    public static final Holder<MobEffect> STUN = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "stun"),
            new StunEffect()
    );

    public static final Holder<MobEffect> SUNBLESSED = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "sunblessed"),
            new SunblessedEffect()
    );

    public static final Holder<MobEffect> MOONSHINE = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "moonshine"),
            new MoonshineEffect()
    );

    public static final Holder<MobEffect> CONDUCTIVITY = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "conductivity"),
            new ConductivityEffect()
    );

    public static void registerModStatusEffects() {
        Rituals.LOGGER.info("Registering Mod Mob Effects for " + Rituals.MOD_ID);
    }
}