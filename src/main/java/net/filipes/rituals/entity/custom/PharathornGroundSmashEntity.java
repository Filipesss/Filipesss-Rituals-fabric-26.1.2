package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
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
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PharathornGroundSmashEntity extends Entity {

    private static final EntityDataAccessor<Float>   DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(PharathornGroundSmashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_DELAY_TICKS  =
            SynchedEntityData.defineId(PharathornGroundSmashEntity.class, EntityDataSerializers.INT);

    public static final int EMERGE_TICKS    = 15;
    public static final int HOLD_TICKS      = 20;
    public static final int RETRACT_TICKS   = 12;
    public static final int ACTIVE_LIFETIME = EMERGE_TICKS + HOLD_TICKS + RETRACT_TICKS;

    private boolean hasDamaged = false;

    public PharathornGroundSmashEntity(EntityType<? extends PharathornGroundSmashEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PharathornGroundSmashEntity(Level level,
                                       double x, double y, double z,
                                       int delayTicks, float yRot, float visualScale) {
        this(ModEntities.PHARATHORN_GROUND_SMASH, level);
        this.setPos(x, y, z);
        this.setDelayTicks(delayTicks);
        this.setYRot(yRot);
        this.setVisualScale(visualScale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VISUAL_SCALE, 1.0f);
        builder.define(DATA_DELAY_TICKS,  0);
    }

    public float getVisualScale()        { return entityData.get(DATA_VISUAL_SCALE); }
    public void  setVisualScale(float s) { entityData.set(DATA_VISUAL_SCALE, s); }
    public int   getDelayTicks()         { return entityData.get(DATA_DELAY_TICKS); }
    public void  setDelayTicks(int d)    { entityData.set(DATA_DELAY_TICKS, d); }

    @Override
    public void tick() {
        super.tick();

        int eff = tickCount - getDelayTicks();
        if (eff < 0) return;

        // Damage at the moment the spike fully emerges
        if (!level().isClientSide() && eff == EMERGE_TICKS && !hasDamaged) {
            hasDamaged = true;
            AABB hitbox = getBoundingBox().inflate(0.9, 1.8, 0.9);
            for (LivingEntity t : ((ServerLevel) level()).getEntitiesOfClass(LivingEntity.class, hitbox))
                t.hurt(level().damageSources().magic(), 5.0f);
        }

        if (eff >= ACTIVE_LIFETIME) discard();
    }

    @Override public boolean shouldBeSaved()                           { return false; }
    @Override protected void readAdditionalSaveData(ValueInput  in)   {
        setDelayTicks(in.getIntOr("DelayTicks", 0));
        setVisualScale(in.getFloatOr("VisualScale", 1.0f));
        hasDamaged = in.getBooleanOr("HasDamaged", false);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("DelayTicks", getDelayTicks());
        out.putFloat("VisualScale", getVisualScale());
        out.putBoolean("HasDamaged", hasDamaged);
    }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean isPickable()                      { return false; }
    @Override public boolean canCollideWith(Entity e)          { return false; }
    @Override public boolean canBeCollidedWith(Entity e)       { return false; }
    @Override public boolean isPushable()                      { return false; }
    @Override public PushReaction getPistonPushReaction()      { return PushReaction.IGNORE; }
}