package net.filipes.rituals.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiBurstSparkEntity extends Entity {

    private static final EntityDataAccessor<Integer> BURST_COUNT =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRAIL_COLOR =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float>   TRAIL_WIDTH =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TRAIL_ALPHA =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFETIME =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRAIL_LENGTH =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WINDOW_SIZE =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float>   TRAIL_JITTER =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   GRAVITY =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   SPEED_MIN =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   SPEED_MAX =
            SynchedEntityData.defineId(MultiBurstSparkEntity.class, EntityDataSerializers.FLOAT);

    public int   getBurstCount() { return entityData.get(BURST_COUNT); }
    public int   getTrailColor() { return entityData.get(TRAIL_COLOR); }
    public float getTrailWidth() { return entityData.get(TRAIL_WIDTH); }
    public int   getTrailAlpha() { return entityData.get(TRAIL_ALPHA); }
    public int   getMaxLifetime(){ return entityData.get(MAX_LIFETIME); }
    public int   getTrailLength(){ return entityData.get(TRAIL_LENGTH); }
    public int   getWindowSize() { return entityData.get(WINDOW_SIZE); }
    public float getTrailJitter(){ return entityData.get(TRAIL_JITTER); }
    public float getSpeedMin()   { return entityData.get(SPEED_MIN); }
    public float getSpeedMax()   { return entityData.get(SPEED_MAX); }

    public List<List<Vec3>> clientTrails = null;
    private double[][] cPos;
    private double[][] cVel;

    public MultiBurstSparkEntity(EntityType<? extends MultiBurstSparkEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(BURST_COUNT, 12);
        b.define(TRAIL_COLOR, (255 << 16) | (160 << 8) | 30);
        b.define(TRAIL_WIDTH, 0.045f);
        b.define(TRAIL_ALPHA, 95);
        b.define(MAX_LIFETIME, 55);
        b.define(TRAIL_LENGTH, 22);
        b.define(WINDOW_SIZE, 5);
        b.define(TRAIL_JITTER, 0f);
        b.define(GRAVITY, 0.055f);
        b.define(SPEED_MIN, 0.15f);
        b.define(SPEED_MAX, 0.45f);
    }

    public void setup(int burstCount,
                      int r, int g, int b,
                      float width, int alpha,
                      int maxLifetime, int trailLength, int windowSize,
                      float jitter, float gravity,
                      float speedMin, float speedMax) {
        entityData.set(BURST_COUNT, burstCount);
        entityData.set(TRAIL_COLOR, (r << 16) | (g << 8) | b);
        entityData.set(TRAIL_WIDTH, width);
        entityData.set(TRAIL_ALPHA, alpha);
        entityData.set(MAX_LIFETIME, maxLifetime);
        entityData.set(TRAIL_LENGTH, trailLength);
        entityData.set(WINDOW_SIZE, windowSize);
        entityData.set(TRAIL_JITTER, jitter);
        entityData.set(GRAVITY, gravity);
        entityData.set(SPEED_MIN, speedMin);
        entityData.set(SPEED_MAX, speedMax);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (clientTrails == null) initClient();
            else                     tickClient();
        } else {
            if (tickCount >= entityData.get(MAX_LIFETIME)) discard();
        }
    }

    private void initClient() {
        int   n        = entityData.get(BURST_COUNT);
        float speedMin = entityData.get(SPEED_MIN);
        float speedMax = entityData.get(SPEED_MAX);

        cPos         = new double[n][3];
        cVel         = new double[n][3];
        clientTrails = new ArrayList<>(n);

        Random rng = new Random(getUUID().getMostSignificantBits());
        double sx = getX(), sy = getY(), sz = getZ();

        for (int i = 0; i < n; i++) {
            cPos[i][0] = sx; cPos[i][1] = sy; cPos[i][2] = sz;

            double angle = rng.nextDouble() * 2.0 * Math.PI;
            double elev  = (rng.nextDouble() * 0.7 - 0.15) * Math.PI;
            double speed = speedMin + rng.nextDouble() * (speedMax - speedMin);
            double cosE  = Math.cos(elev);
            cVel[i][0] = cosE * Math.cos(angle) * speed;
            cVel[i][1] = Math.sin(elev)          * speed;
            cVel[i][2] = cosE * Math.sin(angle)  * speed;

            clientTrails.add(new ArrayList<>());
        }
    }

    private void tickClient() {
        float g      = entityData.get(GRAVITY);
        int   maxLen = entityData.get(TRAIL_LENGTH);

        for (int i = 0; i < cPos.length; i++) {
            cVel[i][1] -= g;
            cPos[i][0] += cVel[i][0];
            cPos[i][1] += cVel[i][1];
            cPos[i][2] += cVel[i][2];

            List<Vec3> trail = clientTrails.get(i);
            trail.add(new Vec3(cPos[i][0], cPos[i][1], cPos[i][2]));
            if (trail.size() > maxLen) trail.remove(0);
        }
    }

    @Override public boolean isOnFire()                                            { return false; }
    @Override public void   setRemainingFireTicks(int t)                           {}
    @Override public boolean shouldBeSaved()                                       { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)                 {}
    @Override protected void addAdditionalSaveData(ValueOutput out)                {}
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a)    { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                   { return d < 128 * 128; }
}