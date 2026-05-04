package net.filipes.rituals.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class BurstSparkEntity extends SparkEntity {

    public BurstSparkEntity(EntityType<? extends SparkEntity> type, Level level) {
        super(type, level);

        trailAmount    = 1;
        trailSpacing   = 0f;
        trailGapChance = 0f;
        trailRotation  = 0f;
        trailWidth     = 0.045f;
        trailJitter    = 0f;
        trailAlpha     = 220;
        maxLifetime    = 55;
        trailLength    = 22;
        windowSize     = 5;
        gravity        = 0.055;
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!level().isClientSide()) discard();
    }

    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
}