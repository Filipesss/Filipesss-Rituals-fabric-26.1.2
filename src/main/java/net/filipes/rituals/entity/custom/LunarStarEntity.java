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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LunarStarEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(LunarStarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FLARE =
            SynchedEntityData.defineId(LunarStarEntity.class, EntityDataSerializers.FLOAT);

    private static final int   RISING_TICKS        = 20;
    private static final float RISING_ORBIT_RADIUS = 0.6f;
    private static final float RISING_ORBIT_SPEED  = 0.22f;
    private static final float RISING_SPEED        = 0.06f;

    private static final float CIRCLE_ORBIT_RADIUS = 0.7f;
    private static final float SINE_AMPLITUDE      = 0.35f;
    private static final float SINE_PERIOD_TICKS   = 70f;

    private SparkEntity risingSpark1;
    private SparkEntity risingSpark2;
    private float riseStartY;
    private float circleBaseY;

    private UUID ownerUUID;
    public Entity ownerEntity;

    public LunarStarEntity(EntityType<? extends LunarStarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwnerUUID(UUID uuid)  { this.ownerUUID = uuid; }
    public void setOwnerEntityId(int id) { this.entityData.set(OWNER_ID, id); }
    public int  getOwnerEntityId()       { return this.entityData.get(OWNER_ID); }

    public void triggerFlare() { entityData.set(FLARE, 1.0f); }
    public float getFlare()    { return entityData.get(FLARE); }

    @Override
    public void tick() {
        super.tick();

        float flare = entityData.get(FLARE);
        if (flare > 0f) entityData.set(FLARE, Math.max(0f, flare - 0.12f));

        if (level().isClientSide()) {
            ownerEntity = level().getEntity(getOwnerEntityId());
            return;
        }

        if (!(level() instanceof ServerLevel sl)) return;
        if (ownerUUID == null) { discard(); return; }

        Entity owner = sl.getEntity(ownerUUID);
        if (owner == null || !owner.isAlive()) { discard(); return; }

        xo = getX(); yo = getY(); zo = getZ();
        setPos(owner.getX(), owner.getY() + 0.02, owner.getZ());

        if (tickCount == 1) {
            riseStartY = (float) owner.getY();
            circleBaseY = riseStartY + 0.1f + RISING_TICKS * RISING_SPEED;

            risingSpark1 = spawnRisingSpark(sl, owner, 0f);
            risingSpark2 = spawnRisingSpark(sl, owner, (float) Math.PI);
        }

        if (tickCount <= RISING_TICKS) {
            float angle1 = tickCount * RISING_ORBIT_SPEED;
            float angle2 = angle1 + (float) Math.PI;
            float rise   = riseStartY + 0.1f + tickCount * RISING_SPEED;

            repositionOrbitSpark(risingSpark1, owner, angle1, rise, RISING_ORBIT_RADIUS);
            repositionOrbitSpark(risingSpark2, owner, angle2, rise, RISING_ORBIT_RADIUS);
        } else {
            long tSinceRise = tickCount - RISING_TICKS;
            float angle1 = tickCount * RISING_ORBIT_SPEED;
            float angle2 = angle1 + (float) Math.PI;
            float sineOffset = (float) Math.sin((tSinceRise / SINE_PERIOD_TICKS) * Math.PI * 2.0) * SINE_AMPLITUDE;
            float y = circleBaseY + sineOffset;

            if (risingSpark1 == null || !risingSpark1.isAlive()) risingSpark1 = spawnRisingSpark(sl, owner, angle1);
            if (risingSpark2 == null || !risingSpark2.isAlive()) risingSpark2 = spawnRisingSpark(sl, owner, angle2);

            repositionOrbitSpark(risingSpark1, owner, angle1, y, CIRCLE_ORBIT_RADIUS);
            repositionOrbitSpark(risingSpark2, owner, angle2, y, CIRCLE_ORBIT_RADIUS);

            spawnAmbientParticles(sl, owner);
        }

        if (!LunarBladeOnHitTracker.isActive(ownerUUID)) discard();
    }

    private SparkEntity spawnRisingSpark(ServerLevel level, Entity owner, float startAngle) {
        double sx = owner.getX() + Math.cos(startAngle) * RISING_ORBIT_RADIUS;
        double sy = owner.getY() + 0.1;
        double sz = owner.getZ() + Math.sin(startAngle) * RISING_ORBIT_RADIUS;

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, sx, sy, sz);
        spark.applyPreset(SparkPresets.LUNAR_FRAGMENT_SINGLE_SHORT);
        spark.setNoGravity(true);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;
        spark.maxLifetime = 200;
        level.addFreshEntity(spark);
        return spark;
    }

    private void repositionOrbitSpark(SparkEntity spark, Entity owner,
                                      float angle, float y, float radius) {
        if (spark == null || !spark.isAlive()) return;
        spark.setPos(
                owner.getX() + Math.cos(angle) * radius,
                y,
                owner.getZ() + Math.sin(angle) * radius);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;
    }

    private void spawnAmbientParticles(ServerLevel level, Entity owner) {
        var random = level.getRandom();

        if (tickCount % 10 == 0) {
            double mx = owner.getX() + (random.nextDouble() - 0.5) * 1.4;
            double my = owner.getY() + 0.3 + random.nextDouble() * 1.2;
            double mz = owner.getZ() + (random.nextDouble() - 0.5) * 1.4;
            level.sendParticles(ModParticles.MOON, mx, my, mz, 1, 0.05, 0.05, 0.05, 0.01);
        }

        int dustCount = 2;
        for (int i = 0; i < dustCount; i++) {
            double dx = owner.getX() + (random.nextDouble() - 0.5) * 1.6;
            double dy = owner.getY() + 0.2 + random.nextDouble() * 1.4;
            double dz = owner.getZ() + (random.nextDouble() - 0.5) * 1.6;
            level.sendParticles(new DustParticleOptions(0xAADDFF, 1.6f), dx, dy, dz, 1, 0.06, 0.06, 0.06, 0.0);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (risingSpark1 != null && risingSpark1.isAlive()) risingSpark1.discard();
            if (risingSpark2 != null && risingSpark2.isAlive()) risingSpark2.discard();
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(FLARE, 0.0f);
    }

    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public boolean shouldBeSaved()                                     { return false; }
    @Override public PushReaction getPistonPushReaction()                        { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                        { return false; }
    @Override public boolean isPushable()                                        { return false; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a)  { return false; }
    @Override public boolean canCollideWith(Entity e)                            { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                         { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                 { return d < 64.0 * 64.0; }
}