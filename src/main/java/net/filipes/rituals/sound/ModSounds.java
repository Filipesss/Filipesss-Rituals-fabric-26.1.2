package net.filipes.rituals.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    private ModSounds() {}

    public static final Identifier PULSE_BLASTER_SHOT_ID =
            Identifier.of("rituals", "pulse_blaster_shot");

    public static final SoundEvent PULSE_BLASTER_SHOT =
            Registry.register(
                    Registries.SOUND_EVENT,
                    PULSE_BLASTER_SHOT_ID,
                    SoundEvent.of(PULSE_BLASTER_SHOT_ID)
            );

    public static void initialize() {
        // intentionally empty; touching the class loads the static fields
    }
}