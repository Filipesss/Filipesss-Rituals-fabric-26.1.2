package net.filipes.rituals.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.network.TwinsStartCooldownPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LunarFragmentEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(LunarFragmentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SLOT =
            SynchedEntityData.defineId(LunarFragmentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LAUNCHED =
            SynchedEntityData.defineId(LunarFragmentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(LunarFragmentEntity.class, EntityDataSerializers.INT);


    public LivingEntity owner;
    private SparkEntity trailSpark;

    private LivingEntity launchTarget;
    private int     launchTick          = 0;
    private int     launchIndex         = 0;
    private Vec3    velocity            = null;
    private boolean velocityInitialized = false;
    private float   currentSpeed        = 0f;
    private boolean resonanceMode    = false;
    private boolean consumedForSolar = false;

    private static final float ORBIT_RADIUS        = 1.1f;
    private static final float ORBIT_SPEED         = 0.04f;
    private static final float ORBIT_HEIGHT        = 1.1f;
    private static final float SPARK_HEIGHT_OFFSET = 0.35f;
    private static final float SPARK_LEAD_TICKS    = 3.0f;
    private static final int   MAX_ORBIT_TICKS     = 300;
    private static final int   INTRO_TICKS         = 15;

    private static final float LAUNCH_SPEED_MIN  = 0.15f;
    private static final float LAUNCH_SPEED_MAX  = 0.9f;
    private static final float ACCELERATION      = 0.045f;
    private static final float RESONANCE_ACCELERATION = 0.12f;
    private static final float TURN_RATE         = 0.55f;
    private static final float HIT_RADIUS_SQ     = 0.2f * 0.2f;
    private static final float LAUNCH_DAMAGE     = 12.0f;
    private static final float DAMAGE_RAMP       = 3.0f;
    private static final float SPEED_RAMP        = 0.08f;
    private static final int   MAX_LAUNCH_TICKS  = 100;
    private static final float LAUNCH_SPARK_HEIGHT = 0.35f;
    private static final float LAUNCH_SPARK_LEAD   = 0.45f;
    private boolean shardMode             = false;
    private boolean shardOutward          = false;
    private UUID    shardOriginalTarget   = null;
    private UUID shardMarkOwnerUUID    = null;
    private static final int   SHARD_MAX_TICKS  = 40;
    private static final float SHARD_DAMAGE     = 7.0f;
    private static final float SHARD_INWARD_HIT_SQ = 0.2f * 0.2f;
    private float shardTurnRate    = TURN_RATE;
    private float shardAcceleration = ACCELERATION;
    private int shardTick = 0;
    private float damageMult = 1.0f;
    public void setDamageMult(float mult) { this.damageMult = mult; }
    private boolean customSpawnPos = false;

    public void setCustomSpawnPos(boolean v) { this.customSpawnPos = v; }

    public LunarFragmentEntity(EntityType<? extends LunarFragmentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.entityData.set(OWNER_ID, owner.getId());
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
    }
    public void initAsShard(boolean outward, Vec3 startVelocity,
                            @Nullable UUID originalMarkTarget,
                            @Nullable UUID markOwnerUUID) {
        this.shardMode           = true;
        this.shardOutward        = outward;
        this.shardOriginalTarget = originalMarkTarget;
        this.shardMarkOwnerUUID  = markOwnerUUID;
        this.velocity            = startVelocity;
        this.velocityInitialized = true;
        this.currentSpeed        = (float) startVelocity.length();
        this.entityData.set(LAUNCHED, true);

        this.shardTurnRate    = 0.30f + (float) Math.random() * 0.45f;
        this.shardAcceleration = 0.025f + (float) Math.random() * 0.040f;
    }

    public void setShardInwardTarget(LivingEntity target) {
        this.launchTarget = target;
        this.entityData.set(TARGET_ID, target.getId());
    }

    public void setSlot(int slot)   { this.entityData.set(SLOT, slot); }
    public int  getOwnerId()        { return this.entityData.get(OWNER_ID); }
    public int  getSlot()           { return this.entityData.get(SLOT); }
    public boolean isLaunched()     { return this.entityData.get(LAUNCHED); }
    public int  getTargetId()       { return this.entityData.get(TARGET_ID); }

    public void setResonanceMode(boolean v) { this.resonanceMode = v; }
    public void launch(LivingEntity target, int launchIndex) {

        if (owner != null && !resonanceMode && !customSpawnPos) {  // ← check flag
            int   slot       = getSlot();
            float t          = this.tickCount;
            float slotOffset = slot * (float)(Math.PI * 2.0 / 4);
            float angle      = t * ORBIT_SPEED + slotOffset
                    + (float)Math.sin(t * 0.031f + slotOffset) * 0.35f;
            float r          = ORBIT_RADIUS
                    + (float)Math.sin(t * 0.027f + slotOffset * 1.3f) * 0.25f;
            float bob        = (float)Math.sin(t * 0.07f + slotOffset * 0.9f) * 0.35f;

            this.setPos(
                    owner.getX() + Math.cos(angle) * r,
                    owner.getY() + ORBIT_HEIGHT + bob,
                    owner.getZ() + Math.sin(angle) * r);
        }

        this.entityData.set(LAUNCHED, true);
        this.entityData.set(TARGET_ID, target.getId());
        this.launchTarget        = target;
        this.launchIndex         = launchIndex;
        this.launchTick          = 0;
        this.velocity            = null;
        this.velocityInitialized = false;
        this.currentSpeed        = LAUNCH_SPEED_MIN;

        float pitch = 1.6f + launchIndex * 0.15f;
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.9f, pitch);
    }

    @Override
    public void tick() {
        super.tick();

        if (isLaunched()) {
            tickLaunched();
            return;
        }

        if (level().isClientSide() && owner == null) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) owner = le;
        }

        if (owner == null) {
            if (!level().isClientSide()) discard();
            return;
        }

        if (!level().isClientSide() && tickCount > MAX_ORBIT_TICKS) {
            discard();
            return;
        }

        xo = getX(); yo = getY(); zo = getZ();
        setPos(owner.getX(), owner.getY(), owner.getZ());

        if (!level().isClientSide()) {
            int   slot   = this.entityData.get(SLOT);
            float t      = this.tickCount;
            float sparkT = t + SPARK_LEAD_TICKS;

            float slotOffset = slot * (float)(Math.PI * 2.0 / 4);
            float angle = sparkT * ORBIT_SPEED + slotOffset
                    + (float)Math.sin(sparkT * 0.031f + slotOffset) * 0.35f;
            float r   = ORBIT_RADIUS
                    + (float)Math.sin(sparkT * 0.027f + slotOffset * 1.3f) * 0.25f;
            float bob = (float)Math.sin(sparkT * 0.07f + slotOffset * 0.9f) * 0.35f;

            double sx = owner.getX() + Math.cos(angle) * r;
            double sy = owner.getY() + ORBIT_HEIGHT + bob + SPARK_HEIGHT_OFFSET;
            double sz = owner.getZ() + Math.sin(angle) * r;

            if (tickCount <= INTRO_TICKS) {
                float introP = tickCount / (float) INTRO_TICKS;
                float eased  = 1.0f - (float) Math.pow(1.0f - introP, 3.0f);
                sx = owner.getX() + (sx - owner.getX()) * eased;
                sy = owner.getY() + (sy - owner.getY()) * eased;
                sz = owner.getZ() + (sz - owner.getZ()) * eased;
            }

            if (tickCount < 3) return;

            if (trailSpark == null || !trailSpark.isAlive()) {
                trailSpark = new SparkEntity(ModEntities.SPARK, level(), sx, sy, sz);
                trailSpark.applyPreset(SparkPresets.LUNAR_FRAGMENT_SINGLE_SHORT);
                trailSpark.setNoGravity(true);
                double smooth = 0.35;
                trailSpark.setPos(
                        trailSpark.getX() + (sx - trailSpark.getX()) * smooth,
                        trailSpark.getY() + (sy - trailSpark.getY()) * smooth,
                        trailSpark.getZ() + (sz - trailSpark.getZ()) * smooth);
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
                level().addFreshEntity(trailSpark);
            } else {
                trailSpark.setPos(sx, sy, sz);
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
            }
        }
    }

    private void tickLaunched() {
        if (shardMode) { tickShard(); return; }
        if (level().isClientSide() && owner == null) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) owner = le;
        }

        if (launchTarget == null) {
            Entity e = level().getEntity(getTargetId());
            if (e instanceof LivingEntity le) launchTarget = le;
        }

        if (!velocityInitialized) {
            velocityInitialized = true;
            if (resonanceMode && launchTarget != null) {
                Vec3 toTarget = launchTarget.getBoundingBox().getCenter()
                        .subtract(this.position())
                        .normalize();
                Vec3 perp = new Vec3(-toTarget.z, 0, toTarget.x)
                        .scale(0.4 + random.nextDouble() * 0.3);
                velocity = toTarget.add(perp).normalize().scale(LAUNCH_SPEED_MIN);
            } else if (owner != null) {
                Vec3 toFragment = this.position()
                        .subtract(owner.getX(), owner.getY(), owner.getZ());
                Vec3 horizontal = new Vec3(toFragment.x, 0, toFragment.z);
                if (horizontal.lengthSqr() > 0.001) {
                    horizontal = horizontal.normalize();
                    Vec3 tangent = new Vec3(-horizontal.z, 0, horizontal.x);
                    velocity = tangent.add(0, 0.12, 0).normalize().scale(LAUNCH_SPEED_MIN);
                } else {
                    velocity = new Vec3(0, 1, 0).scale(LAUNCH_SPEED_MIN);
                }
            } else {
                velocity = Vec3.ZERO;
            }
        }
        if (velocity == null) velocity = Vec3.ZERO;

        float maxSpeed = resonanceMode
                ? LAUNCH_SPEED_MAX * 1.5f
                : LAUNCH_SPEED_MAX + launchIndex * SPEED_RAMP;
        currentSpeed = Math.min(currentSpeed + (resonanceMode ? RESONANCE_ACCELERATION : ACCELERATION), maxSpeed);

        if (launchTarget != null && launchTarget.isAlive()) {
            Vec3 targetCenter = launchTarget.getBoundingBox().getCenter();
            Vec3 toTarget     = targetCenter.subtract(this.position()).normalize();
            velocity = velocity.normalize()
                    .add(toTarget.scale(TURN_RATE))
                    .normalize()
                    .scale(currentSpeed);
        }

        Vec3 newPos = this.position().add(velocity);

        if (!level().isClientSide()) {
            BlockHitResult blockHit = level().clip(new ClipContext(
                    this.position(), newPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this));

            if (blockHit.getType() != HitResult.Type.MISS) {
                spawnImpactSparks(getX(), getY(), getZ(), null);
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8f, 1.1f);
                discard();
                return;
            }
        }

        this.setPos(newPos.x, newPos.y, newPos.z);

        if (level().isClientSide()) return;

        launchTick++;
        if (launchTick > MAX_LAUNCH_TICKS)                   { discard(); return; }
        if (launchTarget == null || !launchTarget.isAlive()) { discard(); return; }

        if (launchTick == 1) {
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            trailSpark = null;
        }

        Vec3 leadDir = velocity.lengthSqr() > 0 ? velocity.normalize() : Vec3.ZERO;
        double sx = getX() + leadDir.x * LAUNCH_SPARK_LEAD;
        double sy = getY() + LAUNCH_SPARK_HEIGHT + leadDir.y * LAUNCH_SPARK_LEAD;
        double sz = getZ() + leadDir.z * LAUNCH_SPARK_LEAD;

        if (trailSpark == null || !trailSpark.isAlive()) {
            trailSpark = new SparkEntity(ModEntities.SPARK, level(), sx, sy, sz);
            trailSpark.applyPreset(resonanceMode
                    ? SparkPresets.LUNAR_FRAGMENT_RESONANCE
                    : SparkPresets.LUNAR_FRAGMENT_TRAIL);
            trailSpark.setNoGravity(true);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
            level().addFreshEntity(trailSpark);
        } else {
            trailSpark.setPos(sx, sy, sz);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
        }

        if (launchTarget.getBoundingBox().distanceToSqr(this.position()) < HIT_RADIUS_SQ) {
            ServerLevel sl  = (ServerLevel) level();

            float damage = resonanceMode
                    ? LAUNCH_DAMAGE * 1.5f * getNightBonus() * damageMult
                    : (LAUNCH_DAMAGE + launchIndex * DAMAGE_RAMP) * getNightBonus() * damageMult;
            DamageSource src = (owner != null && owner.isAlive())
                    ? sl.damageSources().indirectMagic(this, owner)
                    : sl.damageSources().magic();
            launchTarget.hurtServer(sl, src, damage);

            float pitch = 0.7f + launchIndex * 0.1f;
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.2f, pitch);

            spawnImpactSparks(launchTarget.getX(), launchTarget.getY(), launchTarget.getZ(), launchTarget);

            sl.addFreshEntity(new ScreenShakeEntity(sl,
                    new Vec3(launchTarget.getX(),
                            launchTarget.getY() + launchTarget.getBbHeight() * 0.5,
                            launchTarget.getZ()),
                    6f, 0.15f, 5));

            discard();
        }
    }
    private void tickShard() {
        if (shardOutward) tickShardOutward();
        else              tickShardInward();
    }

    private void tickShardInward() {
        if (level().isClientSide()) {
            if (velocity != null) setPos(position().add(velocity));
            return;
        }

        shardTick++;
        if (shardTick > SHARD_MAX_TICKS) { discard(); return; }

        if (launchTarget == null) {
            Entity e = level().getEntity(getTargetId());
            if (e instanceof LivingEntity le) launchTarget = le;
        }

        currentSpeed = Math.min(currentSpeed + shardAcceleration, 0.80f);

        if (launchTarget != null && launchTarget.isAlive()) {
            Vec3 toTarget = launchTarget.getBoundingBox().getCenter()
                    .subtract(position()).normalize();
            velocity = velocity.normalize()
                    .add(toTarget.scale(shardTurnRate))
                    .normalize()
                    .scale(currentSpeed);
        }

        setPos(position().add(velocity));
        updateShardTrail(false);

        if (launchTarget != null && launchTarget.isAlive()
                && launchTarget.getBoundingBox().distanceToSqr(position()) < SHARD_INWARD_HIT_SQ) {
            ServerLevel sl = (ServerLevel) level();
            launchTarget.hurtServer(sl, sl.damageSources().magic(), SHARD_DAMAGE);
            spawnImpactSparks(launchTarget.getX(), launchTarget.getY(), launchTarget.getZ(), launchTarget);
            level().playSound(null, launchTarget.getX(), launchTarget.getY(), launchTarget.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.3f);
            discard();
        }
    }

    private void tickShardOutward() {
        velocity = velocity
                .add(0, -0.022, 0)
                .add(
                        (Math.random() - 0.5) * 0.022,
                        (Math.random() - 0.5) * 0.010,
                        (Math.random() - 0.5) * 0.022
                );

        double spd = velocity.length();
        if (spd > 0.52) velocity = velocity.normalize().scale(0.52);

        if (level().isClientSide()) {
            setPos(position().add(velocity));
            return;
        }

        shardTick++;
        if (shardTick > SHARD_MAX_TICKS) { discard(); return; }

        setPos(position().add(velocity));
        updateShardTrail(true);

        ServerLevel sl = (ServerLevel) level();
        AABB hitBox = getBoundingBox().inflate(0.5);
        List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, hitBox,
                e -> e.isAlive()
                        && (shardOriginalTarget == null || !e.getUUID().equals(shardOriginalTarget))
                        && !(e instanceof ServerPlayer sp
                        && shardMarkOwnerUUID != null
                        && sp.getUUID().equals(shardMarkOwnerUUID)));

        if (!nearby.isEmpty()) {
            LivingEntity hit = nearby.get(0);
            double mx = hit.getX();
            double my = hit.getY() + hit.getBbHeight() * 0.5;
            double mz = hit.getZ();

            LunarMarkEntity newMark = new LunarMarkEntity(ModEntities.LUNAR_MARK, sl, mx, my, mz);
            newMark.setTargetUUID(hit.getUUID());
            if (shardMarkOwnerUUID != null) newMark.setOwnerUUID(shardMarkOwnerUUID);
            sl.addFreshEntity(newMark);

            spawnImpactSparks(mx, my, mz, hit);
            sl.playSound(null, mx, my, mz,
                    SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.1f);
            discard();
        }
    }

    private void updateShardTrail(boolean outward) {
        double sx = getX();
        double sy = getY();
        double sz = getZ();

        if (trailSpark == null || !trailSpark.isAlive()) {
            trailSpark = new SparkEntity(ModEntities.SPARK, level(), sx, sy, sz);
            trailSpark.applyPreset(outward
                    ? SparkPresets.LUNAR_FRAGMENT_TRAIL
                    : SparkPresets.LUNAR_FRAGMENT_RESONANCE);
            trailSpark.setNoGravity(true);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
            level().addFreshEntity(trailSpark);
        } else {
            trailSpark.setPos(sx, sy, sz);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
        }
    }

    public Vec3 getOrbitPosition() {
        if (owner == null) return this.position();
        int   slot       = getSlot();
        float t          = this.tickCount;
        float slotOffset = slot * (float)(Math.PI * 2.0 / 4);
        float angle      = t * ORBIT_SPEED + slotOffset
                + (float)Math.sin(t * 0.031f + slotOffset) * 0.35f;
        float r          = ORBIT_RADIUS
                + (float)Math.sin(t * 0.027f + slotOffset * 1.3f) * 0.25f;
        float bob        = (float)Math.sin(t * 0.07f + slotOffset * 0.9f) * 0.35f;
        return new Vec3(
                owner.getX() + Math.cos(angle) * r,
                owner.getY() + ORBIT_HEIGHT + bob,
                owner.getZ() + Math.sin(angle) * r);
    }
    private float getNightBonus() {
        if (!(level() instanceof ServerLevel sl)) return 1.0f;
        long time = sl.getDefaultClockTime() % 24000L;
        if (time >= 15000 && time < 21000) return 1.15f;
        if (time >= 13000 || time < 1000) return 1.08f;
        return 1.0f;
    }

    public void consumeForSolar() {
        consumedForSolar = true;
        Vec3 pos = getOrbitPosition();
        for (int i = 0; i < 5; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level(),
                    pos.x + (random.nextDouble() - 0.5) * 0.6,
                    pos.y + (random.nextDouble() - 0.5) * 0.4,
                    pos.z + (random.nextDouble() - 0.5) * 0.6);
            spark.applyPreset(resonanceMode
                    ? SparkPresets.LUNAR_FRAGMENT_RESONANCE
                    : SparkPresets.LUNAR_FRAGMENT_SINGLE);
            spark.setNoGravity(false);
            level().addFreshEntity(spark);
        }
        level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.7f, 1.9f);
        discard();
    }

    private void spawnImpactSparks(double x, double y, double z, @org.jetbrains.annotations.Nullable LivingEntity hitEntity) {
        int count = 5 + this.random.nextInt(4); // 5–8
        double height = hitEntity != null ? hitEntity.getBbHeight() : 1.0;
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 1.6;
            double oy =  this.random.nextDouble() * height;
            double oz = (this.random.nextDouble() - 0.5) * 1.6;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level(),
                    x + ox, y + oy, z + oz);
            spark.applyPreset(SparkPresets.LUNAR_FRAGMENT_SINGLE);
            spark.setNoGravity(false);
            level().addFreshEntity(spark);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            if (!level().isClientSide() && shardMode) {
                // Clean up trail only — don't touch twin cooldown logic
                if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
                super.remove(reason);
                return;
            }

            if (owner instanceof ServerPlayer sp) {
                int ownerId = sp.getId();
                ServerLevel sl = (ServerLevel) level();
                AABB searchBox = sp.getBoundingBox().inflate(70);
                List<LunarFragmentEntity> remaining = sl.getEntitiesOfClass(
                        LunarFragmentEntity.class,
                        searchBox,
                        f -> f != this && f.getOwnerId() == ownerId && f.isAlive()
                );
                if (remaining.isEmpty() && !consumedForSolar && !resonanceMode && !customSpawnPos) {
                    ServerPlayNetworking.send(sp, new TwinsStartCooldownPacket());
                }
            }
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID,  -1);
        builder.define(SLOT,       0);
        builder.define(LAUNCHED,  false);
        builder.define(TARGET_ID, -1);
    }

    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}

    @Override public boolean shouldBeSaved()                                     { return false; }
    @Override public PushReaction getPistonPushReaction()                        { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                        { return false; }
    @Override public boolean isPushable()                                        { return false; }
    @Override public boolean canCollideWith(Entity e)                            { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                         { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                 { return d < 64.0 * 64.0; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a)  { return false; }
}