package net.filipes.rituals.entity;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<PulseBlasterBeamEntity> PULSE_BLASTER_BEAM = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Rituals.MOD_ID, "pulse_blaster_beam"),
            EntityType.Builder.<PulseBlasterBeamEntity>create(PulseBlasterBeamEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f)   // hitbox size: small bolt
                    .maxTrackingRange(64)
                    .trackingTickInterval(1)     // update position every tick for smooth movement
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Rituals.MOD_ID, "pulse_blaster_beam")))
    );

    public static void registerModEntities() {
        Rituals.LOGGER.info("Registering Mod Entities for " + Rituals.MOD_ID);
    }
}