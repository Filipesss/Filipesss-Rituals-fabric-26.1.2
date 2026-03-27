package net.filipes.rituals.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private ModSounds() {}

    public static final Identifier PULSE_BLASTER_SHOT_ID =
            Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_shot");

    public static final SoundEvent PULSE_BLASTER_SHOT =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    PULSE_BLASTER_SHOT_ID,
                    SoundEvent.createVariableRangeEvent(PULSE_BLASTER_SHOT_ID)
            );

    public static final Identifier LIGHTNING_RAPIER_ATTACK1_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_attack1");

    public static final SoundEvent LIGHTNING_RAPIER_ATTACK1 =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_RAPIER_ATTACK1_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_RAPIER_ATTACK1_ID)
            );

    public static final Identifier LIGHTNING_RAPIER_ATTACK2_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_attack2");

    public static final SoundEvent LIGHTNING_RAPIER_ATTACK2 =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_RAPIER_ATTACK2_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_RAPIER_ATTACK2_ID)
            );

    public static void initialize() {
        // intentionally empty; touching the class loads the static fields
    }
}