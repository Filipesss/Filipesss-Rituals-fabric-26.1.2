package net.filipes.rituals.entity.custom;

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

public class VortexBoomEntity extends Entity {

    public static final int   FRAME_COUNT = 16;
    public static final int   LIFETIME    = 16;
    public static final float QUAD_SIZE   = 1.5f;
    public static final float STEP_SIZE   = 1.0f;

    private static final EntityDataAccessor<Float> DATA_BEAM_YAW =
            SynchedEntityData.defineId(VortexBoomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEAM_PITCH =
            SynchedEntityData.defineId(VortexBoomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEAM_LENGTH =
            SynchedEntityData.defineId(VortexBoomEntity.class, EntityDataSerializers.FLOAT);

    public VortexBoomEntity(EntityType<? extends VortexBoomEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public VortexBoomEntity(EntityType<? extends VortexBoomEntity> type, Level level,
                            double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        xo = getX();
        yo = getY();
        zo = getZ();
        if (tickCount >= LIFETIME) discard();
    }

    public int getCurrentFrame() {
        return tickCount % FRAME_COUNT;
    }

    public void setBeamDirection(float yaw, float pitch) {
        entityData.set(DATA_BEAM_YAW, yaw);
        entityData.set(DATA_BEAM_PITCH, pitch);
    }

    public void setBeamLength(float length) {
        entityData.set(DATA_BEAM_LENGTH, length);
    }

    public float getBeamYaw()    { return entityData.get(DATA_BEAM_YAW); }
    public float getBeamPitch()  { return entityData.get(DATA_BEAM_PITCH); }
    public float getBeamLength() { return entityData.get(DATA_BEAM_LENGTH); }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BEAM_YAW, 0f);
        builder.define(DATA_BEAM_PITCH, 0f);
        builder.define(DATA_BEAM_LENGTH, 20f);
    }

    @Override public boolean      shouldBeSaved()                                     { return false; }
    @Override protected void      readAdditionalSaveData(ValueInput in)               {}
    @Override protected void      addAdditionalSaveData(ValueOutput out)              {}
    @Override public PushReaction  getPistonPushReaction()                            { return PushReaction.IGNORE; }
    @Override public boolean       isPickable()                                       { return false; }
    @Override public boolean       isPushable()                                       { return false; }
    @Override public boolean       shouldRenderAtSqrDistance(double d)               { return d < (512.0 * 512.0); }
    @Override public boolean       hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean       canCollideWith(Entity e)                           { return false; }
    @Override public boolean       canBeCollidedWith(Entity e)                        { return false; }
}