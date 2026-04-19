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
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class DeathLaserEntity extends Entity {

    // ── Synced Data ───────────────────────────────────────────────────────────

    private static final EntityDataAccessor<Vector3fc> ORIGIN =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3fc> DIRECTION =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> MAX_DISTANCE =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_SPEED =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_WIDTH =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BEAM_COLOR =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOLD_TICKS =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> HIT_DISTANCE =
            SynchedEntityData.defineId(DeathLaserEntity.class, EntityDataSerializers.FLOAT);

    public DeathLaserEntity(EntityType<? extends DeathLaserEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
        this.setSilent(true);
    }

    public DeathLaserEntity(Level level, Vec3 start, Vec3 end) {
        this(level, start, end, 2.0f, 0.22f, 0xFFFF44, 20, -1f);
    }

    public DeathLaserEntity(Level level, Vec3 start, Vec3 end,
                            float speed, float width, int color, int holdTicks, float hitDistance) {
        this(ModEntities.DEATH_LASER, level);
        configureLaser(start, end, speed, width, color, holdTicks, hitDistance);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ORIGIN,      new Vector3f(0f, 0f, 0f));
        builder.define(DIRECTION,   new Vector3f(0f, 0f, 1f));
        builder.define(MAX_DISTANCE, 10f);
        builder.define(BEAM_SPEED,  2.0f);
        builder.define(BEAM_WIDTH,  0.22f);
        builder.define(BEAM_COLOR,  0xFF_FF_FF_44);  // opaque yellow
        builder.define(LIFETIME,    200);
        builder.define(HOLD_TICKS,  20);
        builder.define(HIT_DISTANCE, -1f);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setLaserOrigin(readVec3(input, "Origin", this.position()));
        setLaserDirection(readVec3(input, "Direction", new Vec3(0, 0, 1)));
        setMaxDistance(input.getFloatOr("MaxDistance",  getMaxDistance()));
        setBeamSpeed(input.getFloatOr("Speed",          getBeamSpeed()));
        setBeamWidth(input.getFloatOr("Width",          getBeamWidth()));
        setBeamColor(input.getIntOr("Color",            getBeamColor()));
        setLifetime(input.getIntOr("Lifetime",          getLifetime()));
        setHoldTicks(input.getIntOr("HoldTicks",        getHoldTicks()));
        setHitDistance(input.getFloatOr("HitDistance",  getHitDistance()));
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        writeVec3(output, "Origin",    getLaserOrigin());
        writeVec3(output, "Direction", getLaserDirection());
        output.putFloat("MaxDistance", getMaxDistance());
        output.putFloat("Speed",       getBeamSpeed());
        output.putFloat("Width",       getBeamWidth());
        output.putInt("Color",         getBeamColor());
        output.putInt("Lifetime",      getLifetime());
        output.putInt("HoldTicks",     getHoldTicks());
        output.putFloat("HitDistance", getHitDistance());
        output.putInt("Age",           this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();
        // Animation: grow → hold → shrink (mirror grow duration)
        int growTicks  = (int) Math.ceil(getMaxDistance() / getBeamSpeed());
        int totalTicks = growTicks * 2 + getHoldTicks();
        if (this.tickCount >= totalTicks || this.tickCount >= getLifetime()) {
            this.discard();
        }
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amt) { return false; }
    @Override public boolean isPickable()                         { return false; }
    @Override public boolean canCollideWith(Entity e)             { return false; }
    @Override public boolean canBeCollidedWith(Entity e)          { return false; }
    @Override public boolean isPushable()                         { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)  { return true; }
    @Override public boolean shouldBeSaved()                      { return false; }
    @Override public PushReaction getPistonPushReaction()         { return PushReaction.IGNORE; }

    public void configureLaser(Vec3 start, Vec3 end,
                               float speed, float width, int color,
                               int holdTicks, float hitDistance) {
        Vec3 delta = end.subtract(start);
        double dist = delta.length();
        Vec3 dir = dist < 0.0001 ? new Vec3(0, 0, 1) : delta.scale(1.0 / dist);

        setLaserOrigin(start);
        setLaserDirection(dir);
        setMaxDistance((float) dist);
        setBeamSpeed(speed);
        setBeamWidth(width);
        setBeamColor(color);
        setHoldTicks(holdTicks);
        setHitDistance(hitDistance < 0 ? -1f : Math.min(hitDistance, (float) dist));

        int growTicks = (int) Math.ceil(dist / speed);
        setLifetime(growTicks * 2 + holdTicks + 5);
        this.setPos(start.x, start.y, start.z);
    }

    public static DeathLaserEntity spawn(ServerLevel level,
                                         Vec3 start, Vec3 end,
                                         float speed, float width, int color,
                                         int holdTicks, float hitDistance) {
        DeathLaserEntity e = new DeathLaserEntity(level, start, end, speed, width, color, holdTicks, hitDistance);
        level.addFreshEntity(e);
        return e;
    }

    public Vec3 getLaserOrigin() { return toVec3(entityData.get(ORIGIN)); }
    public void setLaserOrigin(Vec3 v) {
        entityData.set(ORIGIN, toV3f(v));
        this.setPos(v.x, v.y, v.z);
    }

    public Vec3 getLaserDirection() { return toVec3(entityData.get(DIRECTION)); }
    public void setLaserDirection(Vec3 v) {
        double len = v.length();
        Vec3 n = len < 1e-6 ? new Vec3(0, 0, 1) : v.scale(1.0 / len);
        entityData.set(DIRECTION, toV3f(n));
    }

    public float getMaxDistance() { return entityData.get(MAX_DISTANCE); }
    public void setMaxDistance(float v) { entityData.set(MAX_DISTANCE, Math.max(0f, v)); }

    public float getBeamSpeed() { return entityData.get(BEAM_SPEED); }
    public void setBeamSpeed(float v) { entityData.set(BEAM_SPEED, Math.max(0.05f, v)); }

    public float getBeamWidth() { return entityData.get(BEAM_WIDTH); }
    public void setBeamWidth(float v) { entityData.set(BEAM_WIDTH, Math.max(0.01f, v)); }

    public int getBeamColor() { return entityData.get(BEAM_COLOR); }
    public void setBeamColor(int v) { entityData.set(BEAM_COLOR, v | 0xFF000000); }

    public int getLifetime() { return entityData.get(LIFETIME); }
    public void setLifetime(int v) { entityData.set(LIFETIME, Math.max(1, v)); }

    public int getHoldTicks() { return entityData.get(HOLD_TICKS); }
    public void setHoldTicks(int v) { entityData.set(HOLD_TICKS, Math.max(0, v)); }

    public float getHitDistance() { return entityData.get(HIT_DISTANCE); }
    public void setHitDistance(float v) { entityData.set(HIT_DISTANCE, v); }

    private static Vec3     toVec3(Vector3fc v) { return new Vec3(v.x(), v.y(), v.z()); }
    private static Vector3f toV3f (Vec3 v)      { return new Vector3f((float)v.x, (float)v.y, (float)v.z); }

    private static Vec3 readVec3(ValueInput in, String prefix, Vec3 fb) {
        return new Vec3(in.getDoubleOr(prefix+"X", fb.x),
                in.getDoubleOr(prefix+"Y", fb.y),
                in.getDoubleOr(prefix+"Z", fb.z));
    }
    private static void writeVec3(ValueOutput out, String prefix, Vec3 v) {
        out.putDouble(prefix+"X", v.x);
        out.putDouble(prefix+"Y", v.y);
        out.putDouble(prefix+"Z", v.z);
    }
}