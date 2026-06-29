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

public class TemporalSlowZoneGroundEntity extends Entity {

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(TemporalSlowZoneGroundEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(TemporalSlowZoneGroundEntity.class, EntityDataSerializers.INT);

    public TemporalSlowZoneGroundEntity(EntityType<? extends TemporalSlowZoneGroundEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setRadius(float radius)       { entityData.set(RADIUS, radius); }
    public float getRadius()                  { return entityData.get(RADIUS); }
    public void setDurationTicks(int ticks)   { entityData.set(DURATION, ticks); }
    public int getDurationTicks()             { return entityData.get(DURATION); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (tickCount >= getDurationTicks()) discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 8.0f);
        builder.define(DURATION, 100);
    }

    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public boolean shouldBeSaved()                                    { return false; }
    @Override public PushReaction getPistonPushReaction()                       { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                       { return false; }
    @Override public boolean isPushable()                                       { return false; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                           { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                        { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                { return d < 96.0 * 96.0; }
}