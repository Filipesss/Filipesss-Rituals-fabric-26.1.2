package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PolarityTornadoBlueEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(PolarityTornadoBlueEntity.class, EntityDataSerializers.FLOAT);

    private static final double PUSH_RADIUS       = 6.0;
    private static final double PUSH_STRENGTH     = 0.35;
    private static final double MAX_PUSH_PER_TICK = 0.1;

    private static final int START_DELAY_TICKS = 10;

    private int          lifetime       = -1;
    private Vec3         travelVelocity = Vec3.ZERO;
    private boolean      landed         = false;
    private SparkEntity  trailSpark     = null;

    public PolarityTornadoBlueEntity(EntityType<? extends PolarityTornadoBlueEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position) {
        this(ModEntities.POLARITY_TORNADO_BLUE, level);
        this.setPos(position);
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position, int lifetime) {
        this(level, position);
        this.lifetime = lifetime;
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position, int lifetime, float visualScale) {
        this(level, position, lifetime);
        this.setVisualScale(visualScale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VISUAL_SCALE, 1.0f);
    }

    public float getVisualScale() { return this.entityData.get(DATA_VISUAL_SCALE); }
    public void  setVisualScale(float scale) { this.entityData.set(DATA_VISUAL_SCALE, scale); }

    public void launch(Vec3 velocity) {
        this.travelVelocity = velocity;
        this.landed = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (lifetime > 0 && this.tickCount >= lifetime) {
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            if (!this.level().isClientSide()) spawnDeathExplosion();
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            if (!landed) applyTrajectory();
            if (this.tickCount >= START_DELAY_TICKS) applyPush();
            tickTrailSpark();
            spawnEffects();
        }
    }

    private void applyTrajectory() {
        double nx = getX() + travelVelocity.x;
        double ny = getY() + travelVelocity.y;
        double nz = getZ() + travelVelocity.z;

        BlockPos groundCheck = BlockPos.containing(nx, ny - 0.4, nz);
        boolean wouldHitGround = !level().getBlockState(groundCheck)
                .getCollisionShape(level(), groundCheck)
                .isEmpty();

        if (wouldHitGround && travelVelocity.y <= 0) {
            setPos(nx, groundCheck.getY() + 1.0, nz);
            landed = true;
            travelVelocity = Vec3.ZERO;
        } else {
            setPos(nx, ny, nz);
            travelVelocity = new Vec3(
                    travelVelocity.x * 0.99,
                    travelVelocity.y - 0.025,
                    travelVelocity.z * 0.99
            );
        }
    }

    private void applyPush() {
        Vec3 center = this.position();

        AABB searchBox = new AABB(
                center.x - PUSH_RADIUS, center.y - PUSH_RADIUS, center.z - PUSH_RADIUS,
                center.x + PUSH_RADIUS, center.y + PUSH_RADIUS, center.z + PUSH_RADIUS
        );

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class, searchBox, e -> e.isAlive()
        );

        for (LivingEntity target : nearby) {
            Vec3 fromTornado = target.position().subtract(center);
            double distSq = fromTornado.lengthSqr();
            if (distSq > PUSH_RADIUS * PUSH_RADIUS || distSq < 1e-4) continue;

            double clampedForce = Math.min(PUSH_STRENGTH / distSq, MAX_PUSH_PER_TICK);
            Vec3 pushDelta = fromTornado.normalize().scale(clampedForce);

            target.setDeltaMovement(target.getDeltaMovement().add(pushDelta));
            target.fallDistance = 0;

            if (target instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }
    }

    private void tickTrailSpark() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 pos = this.position();
        double tx = pos.x, ty = pos.y + 0.5, tz = pos.z;

        if (trailSpark == null || !trailSpark.isAlive()) {
            trailSpark = new SparkEntity(ModEntities.SPARK, serverLevel, tx, ty, tz);
            trailSpark.applyPreset(SparkPresets.POLARITY_BLUE_DOUBLE);
            trailSpark.setNoGravity(true);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
            serverLevel.addFreshEntity(trailSpark);
        } else {
            trailSpark.setPos(tx, ty, tz);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
        }
    }
    private void spawnDeathExplosion() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        var  random = serverLevel.getRandom();
        Vec3 center = this.position();
        double cx = center.x, cy = center.y + 0.5, cz = center.z;

        int sparkCount = 18;
        for (int i = 0; i < sparkCount; i++) {
            double yaw   = random.nextDouble() * Math.PI * 2.0;
            double pitch = Math.asin(random.nextDouble() * 2.0 - 1.0);
            double speed = 0.25 + random.nextDouble() * 0.55;

            double cosPitch = Math.cos(pitch);
            Vec3 vel = new Vec3(
                    Math.cos(yaw) * cosPitch * speed,
                    Math.sin(pitch) * speed,
                    Math.sin(yaw) * cosPitch * speed
            );

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel, cx, cy, cz);
            spark.applyPreset(SparkPresets.POLARITY_BLUE_SINGLE);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            serverLevel.addFreshEntity(spark);
        }

        int ringCount = 10;
        for (int i = 0; i < ringCount; i++) {
            double angle = (Math.PI * 2.0 / ringCount) * i;
            double speed = 0.45 + random.nextDouble() * 0.3;
            Vec3 vel = new Vec3(Math.cos(angle) * speed, 0.08 + random.nextDouble() * 0.18,
                    Math.sin(angle) * speed);

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel, cx, cy, cz);
            spark.applyPreset(SparkPresets.POLARITY_BLUE_DOUBLE);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            serverLevel.addFreshEntity(spark);
        }

        serverLevel.sendParticles(new DustParticleOptions(0x00B2FF, 2.8f),
                cx, cy, cz, 22, 0.5, 0.5, 0.5, 0.55);
        serverLevel.sendParticles(new DustParticleOptions(0x00B2FF, 1.4f),
                cx, cy, cz, 14, 0.3, 0.4, 0.3, 1.1);

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                cx, cy, cz, 12, 0.3, 0.4, 0.3, 0.25);

        serverLevel.playSound(null, cx, cy, cz,
                SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.85f, 1.5f);
        serverLevel.playSound(null, cx, cy, cz,
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f, 1.6f);
    }

    private void spawnEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        var    random = serverLevel.getRandom();
        Vec3   base   = this.position();
        double cx = base.x, cy = base.y, cz = base.z;

        double spawnAngle  = random.nextDouble() * Math.PI * 2.0;
        double spawnRadius = 0.15 + random.nextDouble() * 0.45;
        double spawnY      = cy + 0.1 + random.nextDouble() * 0.8;
        double spawnX      = cx + Math.cos(spawnAngle) * spawnRadius;
        double spawnZ      = cz + Math.sin(spawnAngle) * spawnRadius;

        double speed  = 0.10 + random.nextDouble() * 0.35;
        double velYaw = random.nextDouble() * Math.PI * 2.0;
        double velY   = -0.05 + random.nextDouble() * 0.30;

        Vec3 vel = new Vec3(Math.cos(velYaw) * speed, velY, Math.sin(velYaw) * speed);

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel, spawnX, spawnY, spawnZ);
        spark.applyPreset(SparkPresets.POLARITY_BLUE_SINGLE);
        spark.setNoGravity(false);
        spark.setDeltaMovement(vel);
        spark.forcedVelocity = vel;
        serverLevel.addFreshEntity(spark);

        serverLevel.sendParticles(new DustParticleOptions(0x00B2FF, 1.1f),
                cx, cy + 0.5, cz, 1, 0.2, 0.35, 0.2, 0.22);

        if (this.tickCount % 20 == 0) {
            serverLevel.sendParticles(new DustParticleOptions(0x00B2FF, 2.2f),
                    cx, cy + 0.5, cz, 3, 0.4, 0.35, 0.4, 0.12);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifetime  = input.getIntOr("Lifetime", this.lifetime);
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
        this.landed    = input.getBooleanOr("Landed", false);
        this.setVisualScale(input.getFloatOr("VisualScale", 1.0f));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Lifetime", this.lifetime);
        output.putBoolean("Landed", this.landed);
        output.putInt("Age", this.tickCount);
        output.putFloat("VisualScale", this.getVisualScale());
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean isPickable()                        { return false; }
    @Override public boolean canCollideWith(Entity entity)       { return false; }
    @Override public boolean canBeCollidedWith(Entity entity)    { return false; }
    @Override public boolean isPushable()                        { return false; }
    @Override public PushReaction getPistonPushReaction()        { return PushReaction.IGNORE; }
}