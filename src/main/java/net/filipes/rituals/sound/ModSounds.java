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
    public static final Identifier POLARTIY_CHANGE_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_change");

    public static final SoundEvent POLARITY_CHANGE =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    POLARTIY_CHANGE_ID,
                    SoundEvent.createVariableRangeEvent(POLARTIY_CHANGE_ID)
            );

    public static final Identifier GROUND_STAB_ID =
            Identifier.fromNamespaceAndPath("rituals", "ground_stab");

    public static final SoundEvent GROUND_STAB =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    GROUND_STAB_ID,
                    SoundEvent.createVariableRangeEvent(GROUND_STAB_ID)
            );
    public static final Identifier TIME_WARP_ID =
            Identifier.fromNamespaceAndPath("rituals", "time_warp");

    public static final SoundEvent TIME_WARP =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    TIME_WARP_ID,
                    SoundEvent.createVariableRangeEvent(TIME_WARP_ID)
            );

    public static final Identifier SLOW_SOUND_ID =
            Identifier.fromNamespaceAndPath("rituals", "slow_sound");

    public static final SoundEvent SLOW_SOUND =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    SLOW_SOUND_ID,
                    SoundEvent.createVariableRangeEvent(SLOW_SOUND_ID)
            );

    public static final Identifier SHIELD_PLACE_ID =
            Identifier.fromNamespaceAndPath("rituals", "shield_place");

    public static final SoundEvent SHIELD_PLACE =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    SHIELD_PLACE_ID,
                    SoundEvent.createVariableRangeEvent(SHIELD_PLACE_ID)
            );

    public static final Identifier LIGHTNING_CHAIN_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_chain");

    public static final SoundEvent LIGHTNING_CHAIN =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_CHAIN_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_CHAIN_ID)
            );
    public static final Identifier LIGHTNING_CHARGE_ID =
            Identifier.fromNamespaceAndPath("rituals", "lightning_charge");

    public static final SoundEvent LIGHTNING_CHARGE =
            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    LIGHTNING_CHARGE_ID,
                    SoundEvent.createVariableRangeEvent(LIGHTNING_CHARGE_ID)
            );

    public static void initialize() {

    }
}