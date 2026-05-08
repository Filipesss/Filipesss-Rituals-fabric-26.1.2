package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.Scalable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import java.util.ArrayList;
import java.util.List;

public class SparkEntity extends ThrowableProjectile {

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

    public int    burstCount;
    public float  burstWidth;
    public double burstSpeedMin;
    public double burstSpeedMax;
    public int    burstLifetime;
    public int    burstTrailLength;
    public int    burstWindowSize;
    public float  burstJitter;
    private boolean hasHit = false;

    public List<SpawnEntry> onHitSpawns = new ArrayList<>();

    private record PendingSpawn(EntityType<?> type, Vec3 pos, double yOffset, float scale, int spawnAtTick) {}
    private final List<PendingSpawn> pendingSpawns = new ArrayList<>();

    public final ArrayDeque<Vec3> trailPositions = new ArrayDeque<>();
    public int trailWindowOffset = 0;

    public Vec3 forcedVelocity = null;
    protected boolean launched = false;

    private static final EntityDataAccessor<String>  PRESET_NAME =
            SynchedEntityData.defineId(SparkEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TRAIL_COLOR =
            SynchedEntityData.defineId(SparkEntity.class, EntityDataSerializers.INT);

    public SparkEntity(EntityType<? extends SparkEntity> type, Level level) {
        super(type, level);
        applyPreset(SparkPresets.DEFAULT);
    }

    public SparkEntity(EntityType<? extends SparkEntity> type, Level level,
                       double x, double y, double z) {
        this(type, level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(PRESET_NAME, "default");
        entityData.define(TRAIL_COLOR, (255 << 16) | (160 << 8) | 30);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(PRESET_NAME)) {
            applyPresetSilently(SparkPresets.get(entityData.get(PRESET_NAME)));
        }
        if (key.equals(TRAIL_COLOR)) {
            int packed = entityData.get(TRAIL_COLOR);
            trailR = (packed >> 16) & 0xFF;
            trailG = (packed >> 8)  & 0xFF;
            trailB =  packed        & 0xFF;
        }
    }

    public void applyPreset(SparkPreset p) {
        SparkPresets.nameOf(p).ifPresent(name -> entityData.set(PRESET_NAME, name));
        applyPresetSilently(p);
    }

    private void applyPresetSilently(SparkPreset p) {
        maxLifetime      = p.maxLifetime;
        trailLength      = p.trailLength;
        windowSize       = p.windowSize;
        trailR           = p.trailR;
        trailG           = p.trailG;
        trailB           = p.trailB;
        trailWidth       = p.trailWidth;
        trailAlpha       = p.trailAlpha;
        trailJitter      = p.trailJitter;
        gravity          = p.gravity;
        trailSpacing     = p.trailSpacing;
        trailAmount      = p.trailAmount;
        trailGapChance   = p.trailGapChance;
        trailRotation    = p.trailRotation;
        burstCount       = p.burstCount;
        burstWidth       = p.burstWidth;
        burstSpeedMin    = p.burstSpeedMin;
        burstSpeedMax    = p.burstSpeedMax;
        burstLifetime    = p.burstLifetime;
        burstTrailLength = p.burstTrailLength;
        burstWindowSize  = p.burstWindowSize;
        burstJitter      = p.burstJitter;
        onHitSpawns      = new ArrayList<>(p.onHitSpawns);
    }

    public void setTrailColor(int r, int g, int b) {
        trailR = r;
        trailG = g;
        trailB = b;
        entityData.set(TRAIL_COLOR, (r << 16) | (g << 8) | b);
    }

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

        if (!level().isClientSide()) {
            pendingSpawns.removeIf(p -> {
                if (tickCount < p.spawnAtTick()) return false;
                Entity e = p.type().create((ServerLevel) level(), EntitySpawnReason.TRIGGERED);
                if (e != null) {
                    e.setPos(p.pos().x, p.pos().y + p.yOffset(), p.pos().z);
                    if (p.scale() != 1.0f) {
                        if (e instanceof LivingEntity living) {
                            var attr = living.getAttribute(Attributes.SCALE);
                            if (attr != null) attr.setBaseValue(p.scale());
                        } else if (e instanceof Scalable scalable) {
                            scalable.setEntityScale(p.scale());
                        }
                    }
                    level().addFreshEntity(e);
                }
                return true;
            });
        }

        if (!level().isClientSide()) {
            boolean allDone  = hasHit && pendingSpawns.isEmpty();
            boolean timedOut = !hasHit && tickCount >= maxLifetime;
            if (allDone || timedOut) discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (!level().isClientSide() && !hasHit) {
            hasHit = true;
            setDeltaMovement(Vec3.ZERO);
            if (hit.getType() == HitResult.Type.BLOCK) {
                spawnBurst(hit.getLocation());
                spawnOnHitEntities(hit.getLocation());
            }
        }
    }

    private void spawnBurst(Vec3 pos) {
        for (int i = 0; i < burstCount; i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double elev  = (random.nextDouble() * 0.7 - 0.15) * Math.PI;
            double speed = burstSpeedMin + random.nextDouble() * (burstSpeedMax - burstSpeedMin);
            double cosE  = Math.cos(elev);
            Vec3 vel = new Vec3(cosE * Math.cos(angle) * speed,
                    Math.sin(elev)         * speed,
                    cosE * Math.sin(angle) * speed);

            BurstSparkEntity burst = new BurstSparkEntity(ModEntities.BURST_SPARK, level());
            burst.setPos(pos.x, pos.y, pos.z);
            burst.forcedVelocity = vel;
            burst.setTrailColor(trailR, trailG, trailB);
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
        for (SpawnEntry entry : onHitSpawns) {
            for (int i = 0; i < entry.count(); i++) {
                pendingSpawns.add(new PendingSpawn(
                        entry.type(), pos,
                        entry.yOffset(), entry.scale(),
                        tickCount + entry.delayTicks()
                ));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Misc overrides
    // -------------------------------------------------------------------------

    @Override protected double  getDefaultGravity()                                 { return gravity; }
    @Override public    boolean shouldBeSaved()                                     { return false; }
    @Override protected void    readAdditionalSaveData(ValueInput in)               {}
    @Override protected void    addAdditionalSaveData(ValueOutput out)              {}
    @Override public    boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public    boolean canCollideWith(Entity e)                            { return false; }
    @Override public    boolean shouldRenderAtSqrDistance(double d)                { return d < 128 * 128; }
}