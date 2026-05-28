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

    public static final Identifier LASER_ID =
            Identifier.fromNamespaceAndPath("rituals", "laser");

    public static final SoundEvent LASER =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LASER_ID,
                    SoundEvent.createVariableRangeEvent(LASER_ID)
            );

    public static final Identifier CINDER_SHIELD_EQUIP_ID =
            Identifier.fromNamespaceAndPath("rituals", "cinder_shield_equip");

    public static final SoundEvent CINDER_SHIELD_EQUIP =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    CINDER_SHIELD_EQUIP_ID,
                    SoundEvent.createVariableRangeEvent(CINDER_SHIELD_EQUIP_ID)
            );
    public static final Identifier GENERIC_DASH_ID =
            Identifier.fromNamespaceAndPath("rituals", "generic_dash");

    public static final SoundEvent GENERIC_DASH =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    GENERIC_DASH_ID,
                    SoundEvent.createVariableRangeEvent(GENERIC_DASH_ID)
            );
    public static final Identifier ELECTRIC_TRAIL_ID =
            Identifier.fromNamespaceAndPath("rituals", "elec_trail");

    public static final SoundEvent ELECTRIC_TRAIL =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    ELECTRIC_TRAIL_ID,
                    SoundEvent.createVariableRangeEvent(ELECTRIC_TRAIL_ID)
            );
    public static final Identifier LIGHTNING_BOLT_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_bolt");

    public static final SoundEvent LIGHTNING_BOLT =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_BOLT_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_BOLT_ID)
            );
    public static final Identifier LIGHTNING_BOLT_2_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_bolt_2");

    public static final SoundEvent LIGHTNING_BOLT_2 =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_BOLT_2_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_BOLT_2_ID)
            );

    public static final Identifier LIGHTNING_BOLT_3_ID =
            Identifier.fromNamespaceAndPath("rituals", "lstrike1");

    public static final SoundEvent LIGHTNING_BOLT_3 =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_BOLT_3_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_BOLT_3_ID)
            );

    public static void initialize() {

    }
}