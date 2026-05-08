package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.Scalable;
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

public class LightningExplosionEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT = 9;
    public static final float QUAD_SIZE   = 2.0f;

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(LightningExplosionEntity.class, EntityDataSerializers.FLOAT);

    public LightningExplosionEntity(EntityType<? extends LightningExplosionEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LightningExplosionEntity(EntityType<? extends LightningExplosionEntity> type, Level level,
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

        if (tickCount >= FRAME_COUNT) {
            discard();
        }
    }

    public int getCurrentFrame() {
        return Math.min(tickCount, FRAME_COUNT - 1);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(DATA_SCALE, 1.0f); }
    @Override public boolean      shouldBeSaved()                                 { return false; }
    @Override protected void      readAdditionalSaveData(ValueInput in)           {}
    @Override protected void      addAdditionalSaveData(ValueOutput out)          {}
    @Override public PushReaction  getPistonPushReaction()                        { return PushReaction.IGNORE; }
    @Override public boolean       isPickable()                                   { return false; }
    @Override public boolean       isPushable()                                   { return false; }
    @Override public boolean       shouldRenderAtSqrDistance(double d)           { return d < (256.0 * 256.0); }
    @Override public boolean       hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean       canCollideWith(Entity e)                       { return false; }
    @Override public boolean       canBeCollidedWith(Entity e)                    { return false; }

    @Override public float getEntityScale()        { return entityData.get(DATA_SCALE); }
    @Override public void  setEntityScale(float s) { entityData.set(DATA_SCALE, s); }
}