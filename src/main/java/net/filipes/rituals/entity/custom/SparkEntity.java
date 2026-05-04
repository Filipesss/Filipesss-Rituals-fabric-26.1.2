package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;

public class SparkEntity extends ThrowableProjectile {

    // ── Trail ──────────────────────────────────────────────────────────────
    public int    maxLifetime;
    public int    trailLength;
    public int    windowSize;
    public int    trailR, trailG, trailB;
    public float  trailWidth;
    public int    trailAlpha;
    public float  trailJitter;
    public double gravity;
    public float  trailSpacing;
    public int    trailAmount;
    public float  trailGapChance;
    public float  trailRotation;
    public int    landingCount = 0;

    // ── Burst ──────────────────────────────────────────────────────────────
    public int    burstCount;
    public float  burstWidth;
    public double burstSpeedMin;
    public double burstSpeedMax;
    public int    burstLifetime;
    public int    burstTrailLength;
    public int    burstWindowSize;
    public float  burstJitter;

    // ── On-hit spawn ───────────────────────────────────────────────────────
    public EntityType<?> onHitSpawnType;
    public int           onHitSpawnCount;
    public double        onHitSpawnYOffset;
    public float onHitSpawnScale;

    // ── Internal ───────────────────────────────────────────────────────────
    public final ArrayDeque<Vec3> trailPositions = new ArrayDeque<>();
    public int trailWindowOffset = 0;

    /**
     * If non-null, used as the launch velocity on the first server tick
     * instead of a random launch. Set this before adding the entity to the world.
     */
    public Vec3 forcedVelocity = null;

    protected boolean launched = false;

    // ──────────────────────────────────────────────────────────────────────

    public SparkEntity(EntityType<? extends SparkEntity> type, Level level) {
        super(type, level);
        applyPreset(SparkPresets.DEFAULT);
    }

    public SparkEntity(EntityType<? extends SparkEntity> type, Level level,
                       double x, double y, double z) {
        this(type, level);
        setPos(x, y, z);
    }

    /** Bulk-applies all values from a preset. Safe to call at any time before spawning. */
    public void applyPreset(SparkPreset p) {
        maxLifetime       = p.maxLifetime;
        trailLength       = p.trailLength;
        windowSize        = p.windowSize;
        trailR            = p.trailR;
        trailG            = p.trailG;
        trailB            = p.trailB;
        trailWidth        = p.trailWidth;
        trailAlpha        = p.trailAlpha;
        trailJitter       = p.trailJitter;
        gravity           = p.gravity;
        trailSpacing      = p.trailSpacing;
        trailAmount       = p.trailAmount;
        trailGapChance    = p.trailGapChance;
        trailRotation     = p.trailRotation;
        burstCount        = p.burstCount;
        burstWidth        = p.burstWidth;
        burstSpeedMin     = p.burstSpeedMin;
        burstSpeedMax     = p.burstSpeedMax;
        burstLifetime     = p.burstLifetime;
        burstTrailLength  = p.burstTrailLength;
        burstWindowSize   = p.burstWindowSize;
        burstJitter       = p.burstJitter;
        onHitSpawnType    = p.onHitSpawnType;
        onHitSpawnCount   = p.onHitSpawnCount;
        onHitSpawnYOffset = p.onHitSpawnYOffset;
        onHitSpawnScale = p.onHitSpawnScale;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {}

    @Override
    public void tick() {
        if (!level().isClientSide() && !launched) {
            launched = true;
            if (forcedVelocity != null) {
                setDeltaMovement(forcedVelocity);
            } else {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double speed = 0.25 + random.nextDouble() * 0.35;
                double vy    = 0.15 + random.nextDouble() * 0.25;
                setDeltaMovement(Math.cos(angle) * speed, vy, Math.sin(angle) * speed);
            }
        }

        if (level().isClientSide()) {
            trailPositions.addLast(new Vec3(getX(), getY(), getZ()));
            while (trailPositions.size() > trailLength) trailPositions.removeFirst();
            trailWindowOffset = trailPositions.size() - 1;
        }

        super.tick();

        if (!level().isClientSide() && tickCount >= maxLifetime) discard();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) {
            if (hit.getType() == HitResult.Type.BLOCK) {
                spawnBurst(hit.getLocation());
                spawnOnHitEntities(hit.getLocation());

            }
            discard();
        }
    }

    private void spawnBurst(Vec3 pos) {
        for (int i = 0; i < burstCount; i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double elev  = (random.nextDouble() * 0.7 - 0.15) * Math.PI;
            double speed = burstSpeedMin + random.nextDouble() * (burstSpeedMax - burstSpeedMin);
            double cosE  = Math.cos(elev);
            Vec3 vel = new Vec3(cosE * Math.cos(angle) * speed,
                    Math.sin(elev)          * speed,
                    cosE * Math.sin(angle)  * speed);

            BurstSparkEntity burst = new BurstSparkEntity(ModEntities.BURST_SPARK, level());
            burst.setPos(pos.x, pos.y, pos.z);
            burst.forcedVelocity = vel;
            burst.trailR         = trailR;
            burst.trailG         = trailG;
            burst.trailB         = trailB;
            burst.trailAlpha     = trailAlpha;
            burst.trailWidth     = burstWidth;
            burst.maxLifetime    = burstLifetime;
            burst.trailLength    = burstTrailLength;
            burst.windowSize     = burstWindowSize;
            burst.trailJitter    = burstJitter;
            level().addFreshEntity(burst);
        }
    }

    private void spawnOnHitEntities(Vec3 pos) {
        if (onHitSpawnType == null) return;
        for (int i = 0; i < onHitSpawnCount; i++) {
            Entity e = onHitSpawnType.create((ServerLevel) level(), EntitySpawnReason.TRIGGERED);
            if (e == null) continue;
            e.setPos(pos.x, pos.y + onHitSpawnYOffset, pos.z);
            if (onHitSpawnScale != 1.0f && e instanceof LivingEntity living) {
                var attr = living.getAttribute(Attributes.SCALE);
                if (attr != null) attr.setBaseValue(onHitSpawnScale);
            }
            level().addFreshEntity(e);
        }
    }

    @Override protected double  getDefaultGravity()                                  { return gravity; }
    @Override public    boolean shouldBeSaved()                                      { return false; }
    @Override protected void    readAdditionalSaveData(ValueInput in)                {}
    @Override protected void    addAdditionalSaveData(ValueOutput out)               {}
    @Override public    boolean hurtServer(ServerLevel l, DamageSource s, float a)  { return false; }
    @Override public    boolean canCollideWith(Entity e)                             { return false; }
    @Override public    boolean shouldRenderAtSqrDistance(double d)                 { return d < 128 * 128; }
}