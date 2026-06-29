package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
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


    private SparkEntity risingSpark1;
    private SparkEntity risingSpark2;
    private float riseStartY;

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

            risingSpark1 = spawnRisingSpark(sl, owner, 0f);
            risingSpark2 = spawnRisingSpark(sl, owner, (float) Math.PI);
        }

        if (tickCount <= RISING_TICKS) {
            float angle1 = tickCount * RISING_ORBIT_SPEED;
            float angle2 = angle1 + (float) Math.PI;
            float rise   = riseStartY + 0.1f + tickCount * RISING_SPEED;

            repositionRisingSpark(risingSpark1, owner, angle1, rise);
            repositionRisingSpark(risingSpark2, owner, angle2, rise);
        } else {
            if (risingSpark1 != null && risingSpark1.isAlive()) { risingSpark1.discard(); risingSpark1 = null; }
            if (risingSpark2 != null && risingSpark2.isAlive()) { risingSpark2.discard(); risingSpark2 = null; }
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
        spark.maxLifetime = RISING_TICKS + 5;
        level.addFreshEntity(spark);
        return spark;
    }

    private void repositionRisingSpark(SparkEntity spark, Entity owner,
                                       float angle, float y) {
        if (spark == null || !spark.isAlive()) return;
        spark.setPos(
                owner.getX() + Math.cos(angle) * RISING_ORBIT_RADIUS,
                y,
                owner.getZ() + Math.sin(angle) * RISING_ORBIT_RADIUS);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;
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