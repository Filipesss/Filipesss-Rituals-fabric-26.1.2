package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DepthstrikeGroundEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(DepthstrikeGroundEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_DELAY_TICKS =
            SynchedEntityData.defineId(DepthstrikeGroundEntity.class, EntityDataSerializers.INT);

    private static final int ACTIVE_LIFETIME = 50;
    private static final int DAMAGE_TICK     = 17;
    private boolean hasDamaged = false;

    public DepthstrikeGroundEntity(EntityType<? extends DepthstrikeGroundEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public DepthstrikeGroundEntity(Level level, Vec3 position, int delayTicks) {
        this(ModEntities.DEPTHSTRIKE_GROUND, level);
        this.setPos(position);
        this.setDelayTicks(delayTicks);
    }

    public DepthstrikeGroundEntity(Level level, Vec3 position) {
        this(level, position, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VISUAL_SCALE, 1.0f);
        builder.define(DATA_DELAY_TICKS, 0);
    }

    public float getVisualScale()        { return this.entityData.get(DATA_VISUAL_SCALE); }
    public void  setVisualScale(float s) { this.entityData.set(DATA_VISUAL_SCALE, s); }
    public int   getDelayTicks()         { return this.entityData.get(DATA_DELAY_TICKS); }
    public void  setDelayTicks(int d)    { this.entityData.set(DATA_DELAY_TICKS, d); }

    @Override
    public void tick() {
        super.tick();

        int effectiveTick = this.tickCount - getDelayTicks();
        if (effectiveTick < 0) return;

        if (effectiveTick == 0 && !level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.EVOKER_FANGS_ATTACK,
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.0f, 0.8f + this.getRandom().nextFloat() * 0.4f);
        }

        if (effectiveTick == DAMAGE_TICK && !hasDamaged) {
            hasDamaged = true;
            if (!level().isClientSide()) {
                damageNearby((ServerLevel) level());
                spawnEmergeEffects((ServerLevel) level());
            }
        }

        if (effectiveTick >= ACTIVE_LIFETIME) {
            this.discard();
        }
    }

    private void spawnEmergeEffects(ServerLevel level) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI / 2.0 * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                    getX(), getY() + 0.3, getZ());
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_TRAIL);
            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * 0.35, 0.5, Math.sin(angle) * 0.35);
            level.addFreshEntity(spark);
        }
    }

    private void damageNearby(ServerLevel level) {
        AABB hitbox = this.getBoundingBox().inflate(1.5, 1.0, 1.5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitbox);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), 6.0f);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setDelayTicks(input.getIntOr("DelayTicks", 0));
        setVisualScale(input.getFloatOr("VisualScale", 1.0f));
        hasDamaged = input.getBooleanOr("HasDamaged", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("DelayTicks", getDelayTicks());
        output.putFloat("VisualScale", getVisualScale());
        output.putBoolean("HasDamaged", hasDamaged);
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean isPickable()                        { return false; }
    @Override public boolean canCollideWith(Entity entity)       { return false; }
    @Override public boolean canBeCollidedWith(Entity entity)    { return false; }
    @Override public boolean isPushable()                        { return false; }
    @Override public PushReaction getPistonPushReaction()        { return PushReaction.IGNORE; }
}