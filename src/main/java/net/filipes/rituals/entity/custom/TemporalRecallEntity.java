package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.particle.ModParticles;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TemporalRecallEntity extends Entity {

    private static final float FADE_IN  = 5f;
    private static final float HOLD     = 190f;
    private static final float FADE_OUT = 5f;
    public  static final int   TOTAL_LIFETIME = 200;

    private static final double BASE_ORBIT_RADIUS = 1.2;
    private static final int NUM_ORBIT_SPARKS = 3;

    private int recallTicks = 0;
    private double currentOrbitAngle = 0.0;
    private boolean initializedSpawnVisuals = false;

    private final UUID[] orbitSparkUUIDs = new UUID[NUM_ORBIT_SPARKS];

    private static final EntityDataAccessor<Boolean> DATA_RECALLING =
            SynchedEntityData.defineId(TemporalRecallEntity.class, EntityDataSerializers.BOOLEAN);

    public TemporalRecallEntity(EntityType<? extends TemporalRecallEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public TemporalRecallEntity(EntityType<? extends TemporalRecallEntity> type, Level level,
                                LivingEntity owner, Vec3 spawnPos) {
        this(type, level);
        setPos(spawnPos.x, spawnPos.y, spawnPos.z);
    }

    public void setRecalling(boolean recalling) { entityData.set(DATA_RECALLING, recalling); }
    public boolean isRecalling()               { return entityData.get(DATA_RECALLING); }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {

            // 1. Initial Arrival Visuals
            if (!initializedSpawnVisuals) {
                spawnArrivalVisuals(serverLevel);
                for (int i = 0; i < NUM_ORBIT_SPARKS; i++) {
                    spawnOrbitSparkInstance(serverLevel, i);
                }
                initializedSpawnVisuals = true;
            }

            boolean recalling = isRecalling();
            double progress = 0.0;
            double angularVelocity = Math.PI / 40.0;

            if (!recalling) {
                this.currentOrbitAngle += angularVelocity;

                if (tickCount % 4 == 0) {
                    spawnAmbientMotes(serverLevel);
                }
            } else {
                if (this.recallTicks < 80) {
                    this.recallTicks++;
                }
                progress = (double) this.recallTicks / 80.0;

                double speedMultiplier = 1.0 + Math.pow(progress, 3.5) * 24.0;
                angularVelocity *= speedMultiplier;
                this.currentOrbitAngle += angularVelocity;

                if (tickCount % 3 == 0) {
                    spawnRecallingVacuumParticles(serverLevel, progress);
                }
            }

            tickPersistentSparks(serverLevel, angularVelocity, progress, recalling);
        }

        if (tickCount >= TOTAL_LIFETIME + 20) {
            discard();
        }
    }

    private void spawnArrivalVisuals(ServerLevel level) {
        double px = getX();
        double py = getY() + 0.5;
        double pz = getZ();

        level.sendParticles(ModParticles.MOON, px, py, pz, 8, 0.3, 0.4, 0.3, 0.05);
        level.sendParticles(ModParticles.TEMPORAL_HOURGLASS, px, py, pz, 6, 0.2, 0.3, 0.2, 0.05);
        level.sendParticles(new DustParticleOptions(0x6257ff, 1.5f), px, py, pz, 14, 0.3, 0.5, 0.3, 0.1);

        int startSparksCount = 8 + random.nextInt(3);

        for (int i = 0; i < startSparksCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double horizontalSpeed = 0.25 + random.nextDouble() * 0.08;

            double vx = Math.cos(angle) * horizontalSpeed;
            double vy = 0.22 + random.nextDouble() * 0.12;
            double vz = Math.sin(angle) * horizontalSpeed;

            SparkEntity startSpark = new SparkEntity(ModEntities.SPARK, level, px, getY() + 0.1, pz);
            startSpark.applyPreset(SparkPresets.TEMPORAL_RECALL_RISE);

            startSpark.setNoGravity(false);
            startSpark.maxLifetime = 25 + random.nextInt(12);

            Vec3 motion = new Vec3(vx, vy, vz);
            startSpark.forcedVelocity = motion;
            startSpark.setDeltaMovement(motion);

            level.addFreshEntity(startSpark);
        }
    }

    private void spawnOrbitSparkInstance(ServerLevel level, int index) {
        double offsetAngle = index * (2.0 * Math.PI / NUM_ORBIT_SPARKS);
        double sx = getX() + Math.cos(offsetAngle) * BASE_ORBIT_RADIUS;
        double sy = getY() + 0.6;
        double sz = getZ() + Math.sin(offsetAngle) * BASE_ORBIT_RADIUS;

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, sx, sy, sz);
        spark.applyPreset(SparkPresets.TEMPORAL_RECALL_ORBIT);

        spark.maxLifetime = TOTAL_LIFETIME - this.tickCount;

        level.addFreshEntity(spark);
        this.orbitSparkUUIDs[index] = spark.getUUID();
    }

    private void tickPersistentSparks(ServerLevel level, double angularVelocity, double progress, boolean recalling) {
        double radius = BASE_ORBIT_RADIUS;

        if (recalling && progress > 0.80) {
            double collapseFactor = (1.0 - progress) / 0.20;
            radius *= Math.max(0.0, collapseFactor);
        }

        for (int i = 0; i < NUM_ORBIT_SPARKS; i++) {
            UUID sparkUUID = this.orbitSparkUUIDs[i];
            Entity entity = sparkUUID != null ? level.getEntity(sparkUUID) : null;

            if (!(entity instanceof SparkEntity spark) || !spark.isAlive()) {
                if (tickCount < TOTAL_LIFETIME - 5) {
                    spawnOrbitSparkInstance(level, i);
                }
                continue;
            }

            double offsetAngle = this.currentOrbitAngle + (i * (2.0 * Math.PI / NUM_ORBIT_SPARKS));

            double targetX = getX() + Math.cos(offsetAngle) * radius;
            double targetY = getY() + 0.6 + (!recalling ? Math.sin((tickCount + i * 10) * 0.1) * 0.1 : 0.0);
            double targetZ = getZ() + Math.sin(offsetAngle) * radius;

            double vx = -Math.sin(offsetAngle) * radius * angularVelocity;
            double vy = !recalling ? Math.cos((tickCount + i * 10) * 0.1) * 0.01 : 0.0;
            double vz = Math.cos(offsetAngle) * radius * angularVelocity;

            if (recalling && progress > 0.80) {
                Vec3 toCore = new Vec3(getX() - spark.getX(), (getY() + 0.8) - spark.getY(), getZ() - spark.getZ());
                vx = toCore.x * 0.4;
                vy = toCore.y * 0.4;
                vz = toCore.z * 0.4;
            }

            spark.setPos(targetX, targetY, targetZ);
            Vec3 motionVec = new Vec3(vx, vy, vz);
            spark.forcedVelocity = motionVec;
            spark.setDeltaMovement(motionVec);
        }
    }

    private void spawnAmbientMotes(ServerLevel level) {
        double angle = (tickCount * 0.15) + (random.nextDouble() * 0.2);
        double radius = 0.25 + random.nextDouble() * 0.35;

        double rx = getX() + Math.cos(angle) * radius;
        double ry = getY() + 0.1 + (random.nextDouble() * 0.3);
        double rz = getZ() + Math.sin(angle) * radius;

        double dx = -Math.sin(angle) * 0.015;
        double dy = 0.03 + random.nextDouble() * 0.03;
        double dz = Math.cos(angle) * 0.015;

        if (random.nextBoolean()) {
            level.sendParticles(new DustParticleOptions(0x6257ff, 0.9f), rx, ry, rz, 0, dx, dy, dz, 1.0);
        } else {
            level.sendParticles(ModParticles.TEMPORAL_HOURGLASS, rx, ry, rz, 0, dx, dy, dz, 1.0);
        }

        if (random.nextInt(3) == 0) {
            level.sendParticles(ModParticles.MOON, rx, ry, rz, 0, dx * 0.5, dy * 1.2, dz * 0.5, 1.0);
        }
    }

    private void spawnRecallingVacuumParticles(ServerLevel level, double progress) {
        int count = progress > 0.80 ? 1 : 2;

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = BASE_ORBIT_RADIUS * (1.0 - (progress * 0.4));

            if (progress > 0.80) {
                radius = Math.max(0.1, BASE_ORBIT_RADIUS * (1.0 - progress));
            }

            double px = getX() + Math.cos(angle) * radius;
            double py = getY() + random.nextDouble() * 1.8;
            double pz = getZ() + Math.sin(angle) * radius;

            double dx, dy, dz;
            if (progress > 0.80) {
                Vec3 implode = new Vec3(getX() - px, (getY() + 0.7) - py, getZ() - pz).normalize().scale(0.4);
                dx = implode.x; dy = implode.y; dz = implode.z;
            } else {
                double speed = 0.1 + progress * 0.3;
                dx = -Math.sin(angle) * speed;
                dy = 0.03 + progress * 0.1;
                dz = Math.cos(angle) * speed;
            }

            level.sendParticles(new DustParticleOptions(0x00E5FF, 1.1f), px, py, pz, 0, dx, dy, dz, 1.0);
            level.sendParticles(ModParticles.MOON, px, py, pz, 0, dx, dy, dz, 1.0);
        }
    }

    public static void spawnBurstSparks(ServerLevel level, Vec3 pos) {
        var random = level.getRandom();

        for (int i = 0; i < 8; i++) {
            Vec3 velocity = randomSphere(1.5 + random.nextDouble() * 1.5);
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x, pos.y + 1.0, pos.z);
            spark.applyPreset(SparkPresets.TEMPORAL_RECALL_BURST);

            spark.maxLifetime = 4 + random.nextInt(5);

            spark.forcedVelocity = velocity;
            spark.setDeltaMovement(velocity);
            level.addFreshEntity(spark);
        }

        for (int i = 0; i < 4; i++) {
            Vec3 velocity = randomSphere(2.0 + random.nextDouble() * 1.5);
            SparkEntity spark = new SparkEntity(ModEntities.BURST_SPARK, level, pos.x, pos.y + 1.0, pos.z);
            spark.applyPreset(SparkPresets.TEMPORAL_RECALL_BURST);

            spark.maxLifetime = 3 + random.nextInt(4);

            spark.forcedVelocity = velocity;
            spark.setDeltaMovement(velocity);
            level.addFreshEntity(spark);
        }

        level.sendParticles(ModParticles.TEMPORAL_HOURGLASS, pos.x, pos.y + 1.0, pos.z, 8, 0.2, 0.2, 0.2, 0.4);
        level.sendParticles(ModParticles.MOON, pos.x, pos.y + 1.0, pos.z, 8, 0.2, 0.2, 0.2, 0.3);
        level.sendParticles(new DustParticleOptions(0x00E5FF, 1.6f), pos.x, pos.y + 1.0, pos.z, 12, 0.2, 0.2, 0.2, 0.6);
    }

    private static Vec3 randomSphere(double speed) {
        double theta = Math.random() * Math.PI * 2.0;
        double phi   = Math.acos(2.0 * Math.random() - 1.0);
        return new Vec3(
                Math.sin(phi) * Math.cos(theta) * speed,
                Math.cos(phi) * speed,
                Math.sin(phi) * Math.sin(theta) * speed
        );
    }

    public float getAlpha(float pt) {
        float age = tickCount - 1 + pt;
        if      (age < FADE_IN)                return age / FADE_IN;
        else if (age < FADE_IN + HOLD)         return 1f;
        else {
            float f = (age - FADE_IN - HOLD) / FADE_OUT;
            return Math.max(0f, 1f - f);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RECALLING, false);
    }

    @Override public boolean shouldBeSaved()                                    { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)              {}
    @Override protected void addAdditionalSaveData(ValueOutput out)             {}
    @Override public PushReaction getPistonPushReaction()                       { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                       { return false; }
    @Override public boolean isPushable()                                       { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                { return d < (256.0 * 256.0); }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                           { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                        { return false; }
}