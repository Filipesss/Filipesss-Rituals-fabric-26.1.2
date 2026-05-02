package net.filipes.rituals.entity.custom;

import net.filipes.rituals.component.ModDataComponents;
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

import java.util.ArrayList;
import java.util.List;

public class DashStabEntity extends Entity {

    public static final int STAB_COUNT    = 6;
    public static final int EMERGE_TICKS  = 2;
    public static final int HOLD_TICKS    = 35;
    public static final int RETRACT_TICKS = 3;
    public static final int STAGGER_TICKS = 3;

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(DashStabEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> END_X =
            SynchedEntityData.defineId(DashStabEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z =
            SynchedEntityData.defineId(DashStabEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_STAGE =
            SynchedEntityData.defineId(DashStabEntity.class, EntityDataSerializers.INT);

    public int totalLifetime;
    public List<StabData> stabs = new ArrayList<>();
    private boolean stabsBuilt = false;

    public static class StabData {
        public float originX, originY, originZ;
        public int   spawnTick;
        public float angle;
    }

    public DashStabEntity(EntityType<? extends DashStabEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        totalLifetime = STAGGER_TICKS * STAB_COUNT + EMERGE_TICKS + HOLD_TICKS + RETRACT_TICKS + 10;
    }

    public DashStabEntity(EntityType<? extends DashStabEntity> type, Level level,
                          LivingEntity owner, Vec3 start, Vec3 end) {
        this(type, level);
        setPos(start.x, start.y, start.z);
        if (!level.isClientSide()) {
            setOwnerId(owner.getId());
            entityData.set(END_X, (float) end.x);
            entityData.set(END_Z, (float) end.z);
            entityData.set(OWNER_STAGE, ModDataComponents.getStage(owner.getMainHandItem()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount == 1) buildStabs();
        if (tickCount >= totalLifetime) discard();
    }

    private void buildStabs() {
        if (stabsBuilt) return;
        stabsBuilt = true;
        stabs.clear();

        float sx = (float) getX();
        float sz = (float) getZ();
        float ex = entityData.get(END_X);
        float ez = entityData.get(END_Z);
        float dashAngle = (float) Math.atan2(ez - sz, ex - sx);

        for (int i = 0; i < STAB_COUNT; i++) {
            float t = STAB_COUNT <= 1 ? 0.5f : i / (float)(STAB_COUNT - 1);
            StabData s = new StabData();
            s.originX   = sx + (ex - sx) * t;
            s.originY   = (float) getY();
            s.originZ   = sz + (ez - sz) * t;
            s.spawnTick = i * STAGGER_TICKS;
            s.angle     = dashAngle;
            stabs.add(s);
        }
    }

    public float getStabProgress(StabData s, float pt) {
        float age = tickCount - 1 + pt - s.spawnTick;
        if (age < 0) return -1f;
        if (age < EMERGE_TICKS) {
            float raw = age / (float) EMERGE_TICKS;
            return 1f - (1f - raw) * (1f - raw);
        }
        if (age < EMERGE_TICKS + HOLD_TICKS) return 1f;
        float retractAge = age - EMERGE_TICKS - HOLD_TICKS;
        if (retractAge < RETRACT_TICKS) {
            float raw = retractAge / (float) RETRACT_TICKS;
            return 1f - raw * raw;
        }
        return -1f;
    }

    public int getOwnerStage() { return entityData.get(OWNER_STAGE); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(END_X, 0f);
        builder.define(END_Z, 0f);
        builder.define(OWNER_STAGE, 1);
    }

    public int  getOwnerId()      { return entityData.get(OWNER_ID); }
    public float getEndX() { return entityData.get(END_X); }
    public float getEndZ() { return entityData.get(END_Z); }
    public void setOwnerId(int v) { entityData.set(OWNER_ID, v); }

    @Override public boolean shouldBeSaved()                                    { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)              {}
    @Override protected void addAdditionalSaveData(ValueOutput out)             {}
    @Override public PushReaction getPistonPushReaction()                       { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                       { return false; }
    @Override public boolean isPushable()                                       { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                { return d < (128.0 * 128.0); }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                           { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                        { return false; }
}