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

public class LightningSparkEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT = 10;
    public static final int LOOP_COUNT = 3;
    public static final int LIFETIME = FRAME_COUNT * LOOP_COUNT;
    public static final float QUAD_SIZE   = 1.0f;

    private final boolean renderFlipped;
    private float renderOffsetX;
    private float renderOffsetY;
    private float renderOffsetZ;


    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(LightningSparkEntity.class, EntityDataSerializers.FLOAT);

    public LightningSparkEntity(EntityType<? extends LightningSparkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;

        this.renderOffsetX = (random.nextFloat() - 0.5f) * 1.1f;
        this.renderOffsetY = (random.nextFloat() - 0.5f) * 0.4f;
        this.renderOffsetZ = (random.nextFloat() - 0.5f) * 1.1f;

        this.renderFlipped = random.nextBoolean();
    }

    public LightningSparkEntity(EntityType<? extends LightningSparkEntity> type, Level level,
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

        if (tickCount >= LIFETIME) {
            discard();
        }
    }
    public boolean isRenderFlipped() {
        return renderFlipped;
    }

    public int getCurrentFrame() {
        return tickCount % FRAME_COUNT;
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
    public float getRenderOffsetX() { return renderOffsetX; }
    public float getRenderOffsetY() { return renderOffsetY; }
    public float getRenderOffsetZ() { return renderOffsetZ; }
}