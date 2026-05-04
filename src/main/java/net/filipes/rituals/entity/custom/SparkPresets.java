package net.filipes.rituals.entity.custom;

import java.util.HashMap;
import java.util.Map;

public final class SparkPresets {

    private static final Map<String, SparkPreset> REGISTRY = new HashMap<>();

    public static final SparkPreset DEFAULT = SparkPreset.builder()
            .build();

    public static final SparkPreset BLUE = SparkPreset.builder()
            .color(60, 140, 255)
            .trailAmount(1)
            .trailGapChance(0f)
            .burstCount(10)
            .build();

    public static final SparkPreset RED_TRIPLE = SparkPreset.builder()
            .color(255, 40, 30)
            .trailAmount(3)
            .trailSpacing(0.07f)
            .burstCount(15)
            .build();

    public static final SparkPreset GREEN_THIN = SparkPreset.builder()
            .color(50, 220, 80)
            .trailWidth(0.06f)
            .trailAmount(1)
            .trailGapChance(0f)
            .gravity(0.03)
            .build();

    static {
        register("default",    DEFAULT);
        register("blue",       BLUE);
        register("red_triple", RED_TRIPLE);
        register("green_thin", GREEN_THIN);
    }

    public static void register(String name, SparkPreset preset) {
        REGISTRY.put(name, preset);
    }

    public static SparkPreset get(String name) {
        return REGISTRY.getOrDefault(name, DEFAULT);
    }

    private SparkPresets() {}
}