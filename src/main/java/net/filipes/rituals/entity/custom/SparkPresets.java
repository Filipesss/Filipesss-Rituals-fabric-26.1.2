package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SparkPresets {

    private static final Map<String, SparkPreset> REGISTRY = new HashMap<>();

    public static final SparkPreset DEFAULT = SparkPreset.builder()
            .build();

    public static final SparkPreset BLUE = SparkPreset.builder()
            .color(60, 140, 255)
            .burstCount(10)
            .build();
    public static final  SparkPreset LIGHTNING_TRIPLE = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(3)
            .trailAlpha(85)
            .trailJitter(0.1f)
            .burstCount(10)
            .addSpawn(ModEntities.LIGHTNING_TRAIL, 1, 2.0, 2.0f, 0)
            .addSpawn(ModEntities.LIGHTNING_SPARK, 5, 0.5, 1.0f, 5)
            .trailRotation(0.45f)
            .trailGapChance(0.2f)
            .build();
    public static final  SparkPreset LIGHTNING_TRIPLE_RED = SparkPreset.builder()
            .color(255, 107, 107)
            .trailAmount(3)
            .trailAlpha(95)
            .trailJitter(0.1f)
            .burstCount(10)
            .addSpawn(ModEntities.LIGHTNING_TRAIL, 1, 2.0, 2.0f, 0)
            .addSpawn(ModEntities.LIGHTNING_SPARK, 5, 0.5, 1.0f, 5)
            .trailRotation(0.5f)
            .trailGapChance(0.2f)
            .build();
    public static final  SparkPreset LIGHTNING_TRIPLE_RED_BIG = SparkPreset.builder()
            .color(255, 107, 107)
            .trailAmount(4)
            .trailAlpha(95)
            .trailJitter(0.2f)
            .trailSpacing(0.05f)
            .burstCount(15)
            .addSpawn(ModEntities.LIGHTNING_TRAIL, 1, 3.0, 3.0f, 0)
            .addSpawn(ModEntities.LIGHTNING_SPARK, 7, 0.5, 1.0f, 5)
            .trailRotation(0.65f)
            .trailGapChance(0.19f)
            .build();

    public static final  SparkPreset DEPTHSTRIKE_TRAIL = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(5)
            .trailAlpha(85)
            .trailSpacing(0.12f)
            .trailJitter(0.3f)
            .burstCount(15)
            .burstWidth(0.095f)
            .addSpawn(ModEntities.LIGHTNING_EXPLOSION, 1, 1.0, 2.0f, 0)
            .trailRotation(0.65f)
            .trailGapChance(0.3f)
            .build();

    public static final  SparkPreset DEPTHSTRIKE_TRAIL_GROUND_SHOCK = SparkPreset.builder()
            .color(255, 107, 107)
            .trailAmount(5)
            .trailSpacing(0.12f)
            .trailAlpha(95)
            .trailJitter(0.3f)
            .burstCount(15)
            .burstWidth(0.095f)
            .addSpawn(ModEntities.LIGHTNING_EXPLOSION, 1, 1.0, 2.0f, 0)
            .trailRotation(0.65f)
            .trailGapChance(0.3f)
            .build();
    public static final  SparkPreset LIGHTNING_STRIKE_IMPACT = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(1)
            .gravity(0.035)
            .maxLifetime(15)
            .windowSize(3)
            .trailSpacing(0f)
            .trailAlpha(95)
            .trailJitter(0.05f)
            .burstCount(0)
            .trailRotation(0.45f)
            .trailGapChance(0.1f)
            .build();
    public static final  SparkPreset DEPTHSTRIKE_CHARGED_BALL = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(1)
            .gravity(0.001)
            .trailLength(65)
            .maxLifetime(10)
            .windowSize(3)
            .trailSpacing(0f)
            .trailAlpha(95)
            .trailJitter(0.05f)
            .burstCount(0)
            .trailRotation(0.05f)
            .trailGapChance(0f)
            .build();
    public static final  SparkPreset DEPTHSTRIKE_CHARGED_BALL_TRAIL = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(4)
            .trailLength(85)
            .windowSize(7)
            .trailRotation(0.45f)
            .maxLifetime(800)
            .trailAlpha(85)
            .trailJitter(0.25f)
            .burstCount(10)
            .addSpawn(ModEntities.LIGHTNING_TRAIL, 1, 2.0, 2.0f, 0)
            .addSpawn(ModEntities.LIGHTNING_SPARK, 5, 0.5, 1.0f, 5)
            .trailRotation(0.45f)
            .trailGapChance(0.2f)
            .build();
    public static final  SparkPreset DEPTHSTRIKE_CHARGED_BALL_IMPACT = SparkPreset.builder()
            .color(133, 255, 212)
            .trailAmount(1)
            .gravity(0.06)
            .windowSize(4)
            .trailAlpha(95)
            .trailJitter(0.05f)
            .burstCount(0)
            .trailRotation(0.05f)
            .build();




    static {
        register("default",    DEFAULT);
        register("blue",       BLUE);
        register("lightning_triple", LIGHTNING_TRIPLE);
        register("depthstrike_trail", DEPTHSTRIKE_TRAIL);
        register("lightning_triple_red", LIGHTNING_TRIPLE_RED);
        register("lightning_triple_red_big", LIGHTNING_TRIPLE_RED_BIG);
        register("depthstrike_trail_ground_shock", DEPTHSTRIKE_TRAIL_GROUND_SHOCK);
        register("lightning_strike_impact", LIGHTNING_STRIKE_IMPACT);
        register("depthstrike_charged_ball", DEPTHSTRIKE_CHARGED_BALL);
        register("depthstrike_charged_ball_trail", DEPTHSTRIKE_CHARGED_BALL_TRAIL);
        register("depthstrike_charged_ball_impact", DEPTHSTRIKE_CHARGED_BALL_IMPACT);

    }
    public static Optional<String> nameOf(SparkPreset preset) {
        return REGISTRY.entrySet().stream()
                .filter(e -> e.getValue() == preset)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public static void register(String name, SparkPreset preset) {
        REGISTRY.put(name, preset);
    }

    public static SparkPreset get(String name) {
    return REGISTRY.getOrDefault(name, DEFAULT);
    }

    private SparkPresets() {}
}