package net.filipes.rituals.entity;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.entity.custom.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<PulseBlasterBeamEntity> PULSE_BLASTER_BEAM = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pulse_blaster_beam"),
            EntityType.Builder.<PulseBlasterBeamEntity>of(
                            (type, level) -> new PulseBlasterBeamEntity(
                                    (EntityType<? extends PulseBlasterBeamEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pulse_blaster_beam")
                    ))
    );
    public static final EntityType<ScreenShakeEntity> SCREEN_SHAKE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "screen_shake"),
            EntityType.Builder.<ScreenShakeEntity>of(
                            (type, level) -> new ScreenShakeEntity(
                                    (EntityType<? extends ScreenShakeEntity>) type,
                                    level
                            ),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "screen_shake")
                    ))
    );
    public static final EntityType<PolarityTornadoBlueEntity> POLARITY_TORNADO_BLUE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "polarity_tornado_blue"),
            EntityType.Builder.<PolarityTornadoBlueEntity>of(
                            (type, level) -> new PolarityTornadoBlueEntity(
                                    (EntityType<? extends PolarityTornadoBlueEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "polarity_tornado_blue")
                    ))
    );
    public static final EntityType<PolarityTornadoRedEntity> POLARITY_TORNADO_RED = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "polarity_tornado_red"),
            EntityType.Builder.<PolarityTornadoRedEntity>of(
                            (type, level) -> new PolarityTornadoRedEntity(
                                    (EntityType<? extends PolarityTornadoRedEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "polarity_tornado_red")
                    ))
    );
    public static final EntityType<ElectricBoltEntity> ELECTRIC_BOLT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "electric_bolt"),
            EntityType.Builder.<ElectricBoltEntity>of(
                            (type, level) -> new ElectricBoltEntity(
                                    (EntityType<? extends ElectricBoltEntity>) type,
                                    level
                            ),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "electric_bolt")
                    ))
    );
    public static final EntityType<DeathLaserEntity> DEATH_LASER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "death_laser"),
            EntityType.Builder.<DeathLaserEntity>of(
                            (type, level) -> new DeathLaserEntity((EntityType<? extends DeathLaserEntity>) type, level),
                            MobCategory.MISC
                    ).sized(0.25f, 0.25f).clientTrackingRange(64).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "death_laser")))
    );
    public static final EntityType<ThrownDepthstrikeEntity> THROWN_DEPTHSTRIKE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "thrown_depthstrike"),
            EntityType.Builder.<ThrownDepthstrikeEntity>of(
                            (type, level) -> new ThrownDepthstrikeEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "thrown_depthstrike")
                    ))
    );




    public static void registerModEntities() {
        Rituals.LOGGER.info("Registering Mod Entities for " + Rituals.MOD_ID);
    }
}