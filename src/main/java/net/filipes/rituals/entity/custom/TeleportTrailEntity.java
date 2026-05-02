package net.filipes.rituals.entity.custom;

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

public class TeleportTrailEntity extends Entity {

    private static final float FADE_IN  = 2f;
    private static final float HOLD     = 10f;
    private static final float FADE_OUT = 14f;
    public  static final int   TOTAL_LIFETIME = (int)(FADE_IN + HOLD + FADE_OUT) + 2;

    private static final EntityDataAccessor<Float> END_X =
            SynchedEntityData.defineId(TeleportTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y =
            SynchedEntityData.defineId(TeleportTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z =
            SynchedEntityData.defineId(TeleportTrailEntity.class, EntityDataSerializers.FLOAT);

    public TeleportTrailEntity(EntityType<? extends TeleportTrailEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public TeleportTrailEntity(EntityType<? extends TeleportTrailEntity> type, Level level,
                               LivingEntity owner, Vec3 start, Vec3 end) {
        this(type, level);
        setPos(start.x, start.y, start.z);
        if (!level.isClientSide()) {
            entityData.set(END_X, (float) end.x);
            entityData.set(END_Y, (float) end.y);
            entityData.set(END_Z, (float) end.z);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= TOTAL_LIFETIME) discard();
    }

    public float getAlpha(float pt) {
        float age = tickCount - 1 + pt;
        if      (age < FADE_IN)                    return age / FADE_IN;
        else if (age < FADE_IN + HOLD)             return 1f;
        else {
            float f = (age - FADE_IN - HOLD) / FADE_OUT;
            return Math.max(0f, 1f - f);
        }
    }

    public float getEndX() { return entityData.get(END_X); }
    public float getEndY() { return entityData.get(END_Y); }
    public float getEndZ() { return entityData.get(END_Z); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(END_X, 0f);
        builder.define(END_Y, 0f);
        builder.define(END_Z, 0f);
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