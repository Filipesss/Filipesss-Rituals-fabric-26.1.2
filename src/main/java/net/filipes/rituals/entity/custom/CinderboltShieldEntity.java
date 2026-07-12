package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class CinderboltShieldEntity extends Entity {

    public static final float SPIN_DEG_PER_TICK = -6.0f;
    public static final float ORBIT_RADIUS      = 1.2f;
    public static final float SHIELD_HEIGHT     = 1.6f;
    public static final float[] BASE_ANGLES     = { 0f, 120f, -120f };
    private SparkEntity shieldSpark1, shieldSpark2, shieldSpark3;
    private static final EntityDataAccessor<Boolean> CONTRACTING =
            SynchedEntityData.defineId(CinderboltShieldEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CURRENT_RADIUS =
            SynchedEntityData.defineId(CinderboltShieldEntity.class, EntityDataSerializers.FLOAT);
    public float clientRadius = 0f;
    public float prevClientRadius = 0f;

    private static final float MAX_RADIUS = 1.25f;

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(CinderboltShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOOST_TICKS_REMAINING =
            SynchedEntityData.defineId(CinderboltShieldEntity.class, EntityDataSerializers.INT);
    private static final float BOOSTED_SPIN_MULTIPLIER = 4.0f;
    private float accumulatedRotation = 0f;

    public LivingEntity owner;

    public CinderboltShieldEntity(EntityType<? extends CinderboltShieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public CinderboltShieldEntity(EntityType<? extends CinderboltShieldEntity> type,
                                  Level level, LivingEntity owner) {
        this(type, level);
        this.owner = owner;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        if (!level.isClientSide()) this.setOwnerId(owner.getId());
    }
    public void applySpinBoost(int durationTicks) {
        entityData.set(BOOST_TICKS_REMAINING, durationTicks);
    }

    private float getCurrentSpinSpeed() {
        int boostTicks = entityData.get(BOOST_TICKS_REMAINING);
        return boostTicks > 0 ? SPIN_DEG_PER_TICK * BOOSTED_SPIN_MULTIPLIER : SPIN_DEG_PER_TICK;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() && owner == null) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) owner = le;
        }
        if (!level().isClientSide()) {
            int boostTicks = entityData.get(BOOST_TICKS_REMAINING);
            if (boostTicks > 0) entityData.set(BOOST_TICKS_REMAINING, boostTicks - 1);
        }
        accumulatedRotation += getCurrentSpinSpeed();
        if (level().isClientSide()) {
            prevClientRadius = clientRadius;
            clientRadius = lerp(clientRadius, getCurrentRadius(), 0.5f);
        }

        if (owner != null) {
            xo = getX(); yo = getY(); zo = getZ();
            setPos(owner.getX(), owner.getY(), owner.getZ());

            if (!level().isClientSide()) {
                float radius = entityData.get(CURRENT_RADIUS);
                if (entityData.get(CONTRACTING)) {
                    radius = lerp(radius, 0f, 0.25f);
                    entityData.set(CURRENT_RADIUS, radius);
                    if (radius < 0.01f) { discard(); return; }
                } else {
                    radius = lerp(radius, MAX_RADIUS, 0.25f);
                    entityData.set(CURRENT_RADIUS, radius);
                }
            }

            if (level().isClientSide()) {
                net.minecraft.util.RandomSource rng = level().getRandom();
                for (float baseAngle : BASE_ANGLES) {
                    if (rng.nextFloat() > 0.7f) continue;
                    int steps = 16;
                    float rawAngle = baseAngle - accumulatedRotation;
                    float snappedAngle = (float)(Math.round(rawAngle / (360f / steps)) * (360f / steps));
                    float angle = (float) Math.toRadians(snappedAngle);
                    double px = getX() + getCurrentRadius() * Math.cos(angle);
                    double py = getY() + SHIELD_HEIGHT + rng.nextFloat() * 0.2;
                    double pz = getZ() + getCurrentRadius() * Math.sin(angle);
                    level().addParticle(
                            ParticleTypes.FLAME,
                            px + rng.nextGaussian() * 0.08, py,
                            pz + rng.nextGaussian() * 0.08,
                            0, 0.02, 0);
                }
            }

            if (!level().isClientSide()) {
                float currentRadius = getCurrentRadius();
                SparkEntity[] sparks = { shieldSpark1, shieldSpark2, shieldSpark3 };
                for (int i = 0; i < BASE_ANGLES.length; i++) {
                    int steps = 16;
                    float rawAngle = BASE_ANGLES[i] - accumulatedRotation;
                    float snappedAngle = (float)(Math.round(rawAngle / (360f / steps)) * (360f / steps));
                    float angle = (float) Math.toRadians(snappedAngle);
                    double px = getX() + currentRadius * Math.cos(angle);
                    double py = getY() + SHIELD_HEIGHT;
                    double pz = getZ() + currentRadius * Math.sin(angle);
                    if (!entityData.get(CONTRACTING) && tickCount % 40 == 0) {
                        level().playSound(
                                null,
                                getX(), getY() + SHIELD_HEIGHT, getZ(),
                                SoundEvents.BLASTFURNACE_FIRE_CRACKLE,
                                net.minecraft.sounds.SoundSource.BLOCKS,
                                0.35f,
                                1.0f
                        );
                    }

                    if (sparks[i] == null || !sparks[i].isAlive()) {
                        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level(), px, py, pz);
                        spark.applyPreset(SparkPresets.CINDERBOLT_SHIELD_TRAIL);
                        spark.setNoGravity(true);
                        spark.setDeltaMovement(Vec3.ZERO);
                        spark.forcedVelocity = Vec3.ZERO;
                        level().addFreshEntity(spark);
                        if (i == 0) shieldSpark1 = spark;
                        else if (i == 1) shieldSpark2 = spark;
                        else shieldSpark3 = spark;
                    } else {
                        sparks[i].setPos(px, py, pz);
                        sparks[i].setDeltaMovement(Vec3.ZERO);
                        sparks[i].forcedVelocity = Vec3.ZERO;
                    }
                }
            }
        }
    }
    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (shieldSpark1 != null && shieldSpark1.isAlive()) shieldSpark1.discard();
            if (shieldSpark2 != null && shieldSpark2.isAlive()) shieldSpark2.discard();
            if (shieldSpark3 != null && shieldSpark3.isAlive()) shieldSpark3.discard();
        }
        super.remove(reason);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(CONTRACTING, false);
        builder.define(CURRENT_RADIUS, 0f);
        builder.define(BOOST_TICKS_REMAINING, 0);
    }
    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public float getCurrentRadius() { return entityData.get(CURRENT_RADIUS); }

    public void startContracting() { entityData.set(CONTRACTING, true); }

    public int  getOwnerId()      { return entityData.get(OWNER_ID); }
    public void setOwnerId(int v) { entityData.set(OWNER_ID, v); }

    @Override public boolean shouldBeSaved()                                    { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)              {}
    @Override protected void addAdditionalSaveData(ValueOutput out)             {}
    @Override public PushReaction getPistonPushReaction()                       { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                       { return false; }
    @Override public boolean isPushable()                                       { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                { return d < 64.0 * 64.0; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                           { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                        { return false; }
}