package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.world.entity.EntityType;

public final class SparkPreset {

    public final int    maxLifetime;
    public final int    trailLength;
    public final int    windowSize;
    public final int    trailR, trailG, trailB;
    public final float  trailWidth;
    public final int    trailAlpha;
    public final float  trailJitter;
    public final double gravity;
    public final float  trailSpacing;
    public final int    trailAmount;
    public final float  trailGapChance;
    public final float  trailRotation;

    public final int    burstCount;
    public final float  burstWidth;
    public final double burstSpeedMin;
    public final double burstSpeedMax;
    public final int    burstLifetime;
    public final int    burstTrailLength;
    public final int    burstWindowSize;
    public final float  burstJitter;

    public final EntityType<?> onHitSpawnType;
    public final int           onHitSpawnCount;
    public final double        onHitSpawnYOffset;
    public final float onHitSpawnScale;

    private SparkPreset(Builder b) {
        maxLifetime       = b.maxLifetime;
        trailLength       = b.trailLength;
        windowSize        = b.windowSize;
        trailR            = b.trailR;
        trailG            = b.trailG;
        trailB            = b.trailB;
        trailWidth        = b.trailWidth;
        trailAlpha        = b.trailAlpha;
        trailJitter       = b.trailJitter;
        gravity           = b.gravity;
        trailSpacing      = b.trailSpacing;
        trailAmount       = b.trailAmount;
        trailGapChance    = b.trailGapChance;
        trailRotation     = b.trailRotation;
        burstCount        = b.burstCount;
        burstWidth        = b.burstWidth;
        burstSpeedMin     = b.burstSpeedMin;
        burstSpeedMax     = b.burstSpeedMax;
        burstLifetime     = b.burstLifetime;
        burstTrailLength  = b.burstTrailLength;
        burstWindowSize   = b.burstWindowSize;
        burstJitter       = b.burstJitter;
        onHitSpawnType    = b.onHitSpawnType;
        onHitSpawnCount   = b.onHitSpawnCount;
        onHitSpawnYOffset = b.onHitSpawnYOffset;
        onHitSpawnScale = b.onHitSpawnScale;

    }

    public static Builder builder() { return new Builder(); }

    public Builder toBuilder() {
        return new Builder()
                .maxLifetime(maxLifetime).trailLength(trailLength).windowSize(windowSize)
                .color(trailR, trailG, trailB).trailWidth(trailWidth).trailAlpha(trailAlpha)
                .trailJitter(trailJitter).gravity(gravity).trailSpacing(trailSpacing)
                .trailAmount(trailAmount).trailGapChance(trailGapChance).trailRotation(trailRotation)
                .burstCount(burstCount).burstWidth(burstWidth)
                .burstSpeed(burstSpeedMin, burstSpeedMax).burstLifetime(burstLifetime)
                .burstTrailLength(burstTrailLength).burstWindowSize(burstWindowSize)
                .burstJitter(burstJitter)
                .onHitSpawn(onHitSpawnType, onHitSpawnCount, onHitSpawnYOffset)
                .onHitSpawnScale(onHitSpawnScale);
    }

    public static final class Builder {
        int    maxLifetime      = 500;
        int    trailLength      = 40;
        int    windowSize       = 6;
        int    trailR           = 255, trailG = 160, trailB = 30;
        float  trailWidth       = 0.10f;
        int    trailAlpha       = 255;
        float  trailJitter      = 0f;
        double gravity          = 0.06;
        float  trailSpacing     = 0.05f;
        int    trailAmount      = 1;
        float  trailGapChance   = 0f;
        float  trailRotation    = 0f;
        int    burstCount       = 12;
        float  burstWidth       = 0.045f;
        double burstSpeedMin    = 0.15;
        double burstSpeedMax    = 0.45;
        int    burstLifetime    = 55;
        int    burstTrailLength = 22;
        int    burstWindowSize  = 5;
        float  burstJitter      = 0f;
        EntityType<?> onHitSpawnType    = ModEntities.LIGHTNING_TRAIL;
        int           onHitSpawnCount   = 1;
        double        onHitSpawnYOffset = 0.0;
        float onHitSpawnScale = 10.0f;


        public Builder maxLifetime(int v)                        { maxLifetime = v;                             return this; }
        public Builder trailLength(int v)                        { trailLength = v;                             return this; }
        public Builder windowSize(int v)                         { windowSize = v;                              return this; }
        public Builder color(int r, int g, int b)                { trailR = r; trailG = g; trailB = b;          return this; }
        public Builder trailWidth(float v)                       { trailWidth = v;                              return this; }
        public Builder trailAlpha(int v)                         { trailAlpha = v;                              return this; }
        public Builder trailJitter(float v)                      { trailJitter = v;                             return this; }
        public Builder gravity(double v)                         { gravity = v;                                 return this; }
        public Builder trailSpacing(float v)                     { trailSpacing = v;                            return this; }
        public Builder trailAmount(int v)                        { trailAmount = v;                             return this; }
        public Builder trailGapChance(float v)                   { trailGapChance = v;                          return this; }
        public Builder trailRotation(float v)                    { trailRotation = v;                           return this; }
        public Builder burstCount(int v)                         { burstCount = v;                              return this; }
        public Builder burstWidth(float v)                       { burstWidth = v;                              return this; }
        public Builder burstSpeed(double min, double max)        { burstSpeedMin = min; burstSpeedMax = max;    return this; }
        public Builder burstLifetime(int v)                      { burstLifetime = v;                           return this; }
        public Builder burstTrailLength(int v)                   { burstTrailLength = v;                        return this; }
        public Builder burstWindowSize(int v)                    { burstWindowSize = v;                         return this; }
        public Builder burstJitter(float v)                      { burstJitter = v;                             return this; }
        public Builder onHitSpawn(EntityType<?> t, int c, double y) { onHitSpawnType = t; onHitSpawnCount = c; onHitSpawnYOffset = y; return this; }
        public Builder onHitSpawnScale(float v) { onHitSpawnScale = v; return this; }

        public SparkPreset build() { return new SparkPreset(this); }
    }
}