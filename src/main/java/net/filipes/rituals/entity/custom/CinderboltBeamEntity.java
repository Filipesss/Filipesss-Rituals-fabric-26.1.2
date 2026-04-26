package net.filipes.rituals.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CinderboltBeamEntity extends Entity {

    public static final double RADIUS         = 30.0;
    public static final double CASTER_Y_OFFSET = 2.7;

    private static final EntityDataAccessor<Float>   YAW       = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   PITCH     = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION  = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FIRE      = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float>   DAMAGE    = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   HP_DAMAGE = SynchedEntityData.defineId(CinderboltBeamEntity.class, EntityDataSerializers.FLOAT);

    public LivingEntity caster;

    public double endPosX, endPosY, endPosZ;
    public double collidePosX, collidePosY, collidePosZ;
    public double prevCollidePosX, prevCollidePosY, prevCollidePosZ;

    public float renderYaw, renderPitch;
    public float prevRenderYaw, prevRenderPitch;

    public int appearTimer;
    public int prevAppearTimer;

    public boolean  on        = true;
    public Direction blockSide = null;

    public CinderboltBeamEntity(EntityType<? extends CinderboltBeamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public CinderboltBeamEntity(EntityType<? extends CinderboltBeamEntity> type, Level level,
                                LivingEntity caster,
                                double x, double y, double z,
                                float yaw, float pitch,
                                int duration, float damage, float hpDamage) {
        this(type, level);
        this.caster = caster;
        this.setLaserYaw(yaw);
        this.setLaserPitch(pitch);
        this.setDuration(duration);
        this.setDamage(damage);
        this.setHpDamage(hpDamage);
        this.setPos(x, y, z);
        this.calculateEndPos();
        if (!level.isClientSide()) {
            this.setCasterID(caster.getId());
        }
    }

    @Override
    public void tick() {
        super.tick();

        prevCollidePosX = collidePosX;
        prevCollidePosY = collidePosY;
        prevCollidePosZ = collidePosZ;
        prevRenderYaw   = renderYaw;
        prevRenderPitch = renderPitch;
        prevAppearTimer = appearTimer;
        xo = getX(); yo = getY(); zo = getZ();

        if (tickCount == 1 && level().isClientSide()) {
            Entity e = level().getEntity(getCasterID());
            if (e instanceof LivingEntity le) caster = le;
        }

        if (!level().isClientSide() && caster != null) {
            this.setLaserYaw  ((float) ((caster.yHeadRot + 90.0) * Math.PI / 180.0));
            this.setLaserPitch((float) (-caster.getXRot()         * Math.PI / 180.0));
            this.setPos(caster.getX(), caster.getY() + CASTER_Y_OFFSET, caster.getZ());
        }

        if (caster != null) {
            renderYaw   = (float) ((caster.yHeadRot + 90.0) * Math.PI / 180.0);
            renderPitch = (float) (-caster.getXRot()         * Math.PI / 180.0);
        } else {
            renderYaw   = getLaserYaw();
            renderPitch = getLaserPitch();
        }

        if (caster != null && !caster.isAlive()) { discard(); return; }

        if (!on) {
            if (appearTimer > 0) appearTimer--;
            else { discard(); return; }
        } else if (tickCount > 20) {
            if (appearTimer < 3) appearTimer++;
        } else {
            if (appearTimer > 0) appearTimer--;
        }

        if (tickCount - 20 > getDuration()) on = false;

        if (tickCount > 20) {
            calculateEndPos();

            List<LivingEntity> hitEntities = performRaytrace(
                    level(),
                    new Vec3(getX(), getY(), getZ()),
                    new Vec3(endPosX, endPosY, endPosZ));

            if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                if (blockSide != null && getFire()) {
                    BlockPos bp = BlockPos.containing(collidePosX, collidePosY, collidePosZ);
                    if (level().isEmptyBlock(bp))
                        level().setBlockAndUpdate(bp, BaseFireBlock.getState(level(), bp));
                }

                for (LivingEntity target : hitEntities) {
                    if (caster != null && !caster.isAlliedTo(target) && target != caster) {
                        float flat  = getDamage();
                        float bonus = Math.min(flat, target.getMaxHealth() * getHpDamage() * 0.01f);
                        DamageSource src = serverLevel.damageSources().magic();
                        boolean didHurt = target.hurtServer(serverLevel, src, flat + bonus);
                        if (getFire() && didHurt) target.setRemainingFireTicks(100);
                    }
                }
            }
        }
    }

    private void calculateEndPos() {
        float yaw   = level().isClientSide() ? renderYaw   : getLaserYaw();
        float pitch = level().isClientSide() ? renderPitch : getLaserPitch();
        endPosX = getX() + RADIUS * Math.cos(yaw) * Math.cos(pitch);
        endPosY = getY() + RADIUS * Math.sin(pitch);
        endPosZ = getZ() + RADIUS * Math.sin(yaw) * Math.cos(pitch);
    }

    private List<LivingEntity> performRaytrace(Level world, Vec3 from, Vec3 to) {
        HitResult blockHit = world.clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (blockHit instanceof BlockHitResult bhr && blockHit.getType() == HitResult.Type.BLOCK) {
            Vec3 loc = bhr.getLocation();
            collidePosX = loc.x; collidePosY = loc.y; collidePosZ = loc.z;
            blockSide   = bhr.getDirection();
        } else {
            collidePosX = endPosX; collidePosY = endPosY; collidePosZ = endPosZ;
            blockSide   = null;
        }

        AABB scanBox = new AABB(
                Math.min(getX(), collidePosX) - 1, Math.min(getY(), collidePosY) - 1, Math.min(getZ(), collidePosZ) - 1,
                Math.max(getX(), collidePosX) + 1, Math.max(getY(), collidePosY) + 1, Math.max(getZ(), collidePosZ) + 1);

        List<LivingEntity> result = new ArrayList<>();
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, scanBox, e -> e != caster)) {
            float pad = entity.getPickRadius() + 0.5f;
            AABB padded = entity.getBoundingBox().inflate(pad, pad, pad);
            Optional<Vec3> hit = padded.clip(from, to);
            if (padded.contains(from) || hit.isPresent()) result.add(entity);
        }
        return result;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(YAW,       0f);
        builder.define(PITCH,     0f);
        builder.define(DURATION,  60);
        builder.define(CASTER_ID, -1);
        builder.define(FIRE,      false);
        builder.define(DAMAGE,    0f);
        builder.define(HP_DAMAGE, 0f);
    }

    public float   getLaserYaw()           { return entityData.get(YAW); }
    public void    setLaserYaw(float v)    { entityData.set(YAW, v); }
    public float   getLaserPitch()         { return entityData.get(PITCH); }
    public void    setLaserPitch(float v)  { entityData.set(PITCH, v); }
    public int     getDuration()           { return entityData.get(DURATION); }
    public void    setDuration(int v)      { entityData.set(DURATION, v); }
    public int     getCasterID()           { return entityData.get(CASTER_ID); }
    public void    setCasterID(int v)      { entityData.set(CASTER_ID, v); }
    public boolean getFire()               { return entityData.get(FIRE); }
    public void    setFire(boolean v)      { entityData.set(FIRE, v); }
    public float   getDamage()             { return entityData.get(DAMAGE); }
    public void    setDamage(float v)      { entityData.set(DAMAGE, v); }
    public float   getHpDamage()           { return entityData.get(HP_DAMAGE); }
    public void    setHpDamage(float v)    { entityData.set(HP_DAMAGE, v); }

    @Override public boolean   shouldBeSaved()                              { return false; }
    @Override protected void   readAdditionalSaveData(ValueInput in)        {}
    @Override protected void   addAdditionalSaveData(ValueOutput out)       {}
    @Override public PushReaction getPistonPushReaction()                   { return PushReaction.IGNORE; }
    @Override public boolean   isPickable()                                 { return false; }
    @Override public boolean   isPushable()                                 { return false; }
    @Override public boolean   shouldRenderAtSqrDistance(double d)          { return d < (1024.0 * 1024.0); }
    @Override public boolean   hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean   canCollideWith(Entity e)                     { return false; }
    @Override public boolean   canBeCollidedWith(Entity e)                  { return false; }
}