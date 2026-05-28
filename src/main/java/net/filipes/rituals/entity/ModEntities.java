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
                            (type, level) -> new DeathLaserEntity(
                                    (EntityType<? extends DeathLaserEntity>) type,
                                    level
                            ),
                            MobCategory.MISC
                    )
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "death_laser")
                    ))
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
    public static final EntityType<LightningSparkEntity> LIGHTNING_SPARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_spark"),
            EntityType.Builder.<LightningSparkEntity>of(
                            (type, level) -> new LightningSparkEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(1f, 2f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_spark")))
    );
    public static final EntityType<LightningExplosionEntity> LIGHTNING_EXPLOSION = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_explosion"),
            EntityType.Builder.<LightningExplosionEntity>of(
                            (type, level) -> new LightningExplosionEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(1f, 2f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lightning_explosion")))
    );
    public static final EntityType<WindGustBigEntity> WIND_GUST_BIG = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "wind_gust_big"),
            EntityType.Builder.<WindGustBigEntity>of(
                            (type, level) -> new WindGustBigEntity(type, level),
                            MobCategory.MISC
                    )
                    .sized(1f, 2f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "wind_gust_big")))
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
    public static final EntityType<DepthstrikeGroundEntity> DEPTHSTRIKE_GROUND = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "depthstrike_ground"),
            EntityType.Builder.<DepthstrikeGroundEntity>of(
                            (type, level) -> new DepthstrikeGroundEntity(
                                    (EntityType<? extends DepthstrikeGroundEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "depthstrike_ground")
                    ))
    );
    public static final EntityType<PharathornGroundSmashEntity> PHARATHORN_GROUND_SMASH = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pharathorn_ground_smash"),
            EntityType.Builder.<PharathornGroundSmashEntity>of(
                            (type, level) -> new PharathornGroundSmashEntity(
                                    (EntityType<? extends PharathornGroundSmashEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pharathorn_ground_smash")
                    ))
    );
    public static final EntityType<DepthstrikeChargedBallEntity> DEPTHSTRIKE_CHARGED_BALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "depthstrike_charged_ball"),
            EntityType.Builder.<DepthstrikeChargedBallEntity>of(
                            (type, level) -> new DepthstrikeChargedBallEntity(
                                    (EntityType<? extends DepthstrikeChargedBallEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "depthstrike_charged_ball")
                    ))
    );
    public static final EntityType<ShadowguardGrappleEntity> SHADOWGUARD_GRAPPLE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "shadowguard_grapple"),
            EntityType.Builder.<ShadowguardGrappleEntity>of(
                            (type, level) -> new ShadowguardGrappleEntity(
                                    (EntityType<? extends ShadowguardGrappleEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "shadowguard_grapple")
                    ))
    );
    public static final EntityType<LifestealMarkEntity> LIFESTEAL_MARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lifesteal_mark"),
            EntityType.Builder.<LifestealMarkEntity>of(
                            (type, level) -> new LifestealMarkEntity(
                                    (EntityType<? extends LifestealMarkEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lifesteal_mark")
                    ))
    );
    public static final EntityType<PharathornMarkEntity> PHARATHORN_MARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pharathorn_mark"),
            EntityType.Builder.<PharathornMarkEntity>of(
                            (type, level) -> new PharathornMarkEntity(
                                    (EntityType<? extends PharathornMarkEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pharathorn_mark")
                    ))
    );
    public static final EntityType<LunarMarkEntity> LUNAR_MARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lunar_mark"),
            EntityType.Builder.<LunarMarkEntity>of(
                            (type, level) -> new LunarMarkEntity(
                                    (EntityType<? extends LunarMarkEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lunar_mark")
                    ))
    );
    public static final EntityType<SolarMarkEntity> SOLAR_MARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "solar_mark"),
            EntityType.Builder.<SolarMarkEntity>of(
                            (type, level) -> new SolarMarkEntity(
                                    (EntityType<? extends SolarMarkEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "solar_mark")
                    ))
    );
    public static final EntityType<CinderboltShieldEntity> CINDERBOLT_SHIELD = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "cinderbolt_shield"),
            EntityType.Builder.<CinderboltShieldEntity>of(
                            (type, level) -> new CinderboltShieldEntity(
                                    (EntityType<? extends CinderboltShieldEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "cinderbolt_shield")
                    ))
    );
    public static final EntityType<LunarFragmentEntity> LUNAR_FRAGMENT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lunar_fragment"),
            EntityType.Builder.<LunarFragmentEntity>of(
                            (type, level) -> new LunarFragmentEntity(
                                    (EntityType<? extends LunarFragmentEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "lunar_fragment")
                    ))
    );
    public static final EntityType<SolarStormcellEntity> SOLAR_STORMCELL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "solar_stormcell"),
            EntityType.Builder.<SolarStormcellEntity>of(
                            (type, level) -> new SolarStormcellEntity(
                                    (EntityType<? extends SolarStormcellEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "solar_stormcell")
                    ))
    );




    public static void registerModEntities() {
        Rituals.LOGGER.info("Registering Mod Entities for " + Rituals.MOD_ID);
    }
}