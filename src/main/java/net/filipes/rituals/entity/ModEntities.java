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
    public static final EntityType<SpiralStabEntity> SPIRAL_STAB = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "spiral_stab"),
            EntityType.Builder.<SpiralStabEntity>of(
                            (type, level) -> new SpiralStabEntity((EntityType<? extends SpiralStabEntity>) type, level),
                            MobCategory.MISC
                    ).sized(0.25f, 0.25f).clientTrackingRange(64).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "death_laser")))
    );
    public static final EntityType<DashStabEntity> DASH_STAB = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "dash_stab"),
            EntityType.Builder.<DashStabEntity>of(
                            (type, level) -> new DashStabEntity((EntityType<? extends DashStabEntity>) type, level),
                            MobCategory.MISC
                    ).sized(0.1f, 0.1f).clientTrackingRange(10).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "dash_stab")))
    );
    public static final EntityType<TeleportTrailEntity> LIGHTNING_RAPIER_TELEPORT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_rapier_teleport"),
            EntityType.Builder.<TeleportTrailEntity>of(
                            (type, level) -> new TeleportTrailEntity((EntityType<? extends TeleportTrailEntity>) type, level),
                            MobCategory.MISC
                    ).sized(0.1f, 0.1f).clientTrackingRange(10).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_rapier_teleport")))
    );

    public static final EntityType<CinderboltBeamEntity> CINDERBOLT_BEAM = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "cinderbolt_beam"),
            EntityType.Builder.<CinderboltBeamEntity>of(
                            (type, level) -> new CinderboltBeamEntity((EntityType<? extends CinderboltBeamEntity>) type, level),
                            MobCategory.MISC
                    ).sized(0.25f, 0.25f).clientTrackingRange(64).updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "cinderbolt_beam")))
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
    public static final EntityType<PolarityArrowEntity> POLARITY_ARROW =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("rituals", "polarity_arrow"),
                    EntityType.Builder.<PolarityArrowEntity>of(
                            PolarityArrowEntity::new,
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "polarity_arrow"))
            ));
    public static final EntityType<CinderArrowEntity> CINDER_ARROW =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("rituals", "cinder_arrow"),
                    EntityType.Builder.<CinderArrowEntity>of(
                            CinderArrowEntity::new,
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "cinder_arrow"))
                    ));

    public static final EntityType<LightningStrikeEntity> LIGHTNING_STRIKE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_strike"),
            EntityType.Builder.<LightningStrikeEntity>of(
                            (type, level) -> new LightningStrikeEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_strike")))
    );
    public static final EntityType<LightningTrailEntity> LIGHTNING_TRAIL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_trail"),
            EntityType.Builder.<LightningTrailEntity>of(
                            (type, level) -> new LightningTrailEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(1f, 2f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_trail")))
    );
    public static final EntityType<SparkEntity> SPARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "spark"),
            EntityType.Builder.<SparkEntity>of(
                            (type, level) -> new SparkEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "spark")))
    );
    public static final EntityType<BurstSparkEntity> BURST_SPARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "burst_spark"),
            EntityType.Builder.<BurstSparkEntity>of(
                            (type, level) -> new BurstSparkEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "burst_spark")))
    );




    public static void registerModEntities() {
        Rituals.LOGGER.info("Registering Mod Entities for " + Rituals.MOD_ID);
    }
}