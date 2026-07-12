package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
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

public class PolarityShieldEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(PolarityShieldEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_RED =
            SynchedEntityData.defineId(PolarityShieldEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION_STATE =
            SynchedEntityData.defineId(PolarityShieldEntity.class, EntityDataSerializers.INT);

    public LivingEntity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 10;
    public int deathTicks = 0;
    public static final int MAX_DEATH_TICKS = 10;

    public PolarityShieldEntity(EntityType<? extends PolarityShieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public PolarityShieldEntity(EntityType<? extends PolarityShieldEntity> type, Level level, LivingEntity owner, boolean isRed) {
        this(type, level);
        this.owner = owner;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        if (!level.isClientSide()) {
            this.setOwnerId(owner.getId());
            this.setIsRed(isRed);
        }
    }
    public int getActionState() { return entityData.get(ACTION_STATE); }
    public void setActionState(int v) { entityData.set(ACTION_STATE, v); }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() && owner == null) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) owner = le;
        }

        int animState = getActionState();

        if (animState == 2) {
            deathTicks++;
            if (!level().isClientSide() && deathTicks >= MAX_DEATH_TICKS) {
                this.discard();
            }
            return;
        }

        lifetime++;

        if (lifetime >= 5 && animState == 0) {
            setActionState(1);
            if (!level().isClientSide()) {
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, isRed() ? 0.8f : 1.4f);
            }
        }

        if (animState == 1) {
            deathTicks++;
            if (!level().isClientSide() && owner != null) {
                Vec3 look = new Vec3(owner.getLookAngle().x, 0, owner.getLookAngle().z).normalize();
                Vec3 rightDir = new Vec3(-look.z, 0, look.x);
                Vec3 dashDir = isRed() ? rightDir : rightDir.scale(-1);

                spawnExpiryTrailSparks(isRed(), dashDir);
            }

            if (!level().isClientSide() && lifetime >= 12) {
                this.discard();
                return;
            }
        }

        if (owner != null) {
            xo = getX(); yo = getY(); zo = getZ();

            Vec3 look = new Vec3(owner.getLookAngle().x, 0, owner.getLookAngle().z).normalize();
            Vec3 rightDir = new Vec3(-look.z, 0, look.x);
            boolean red = isRed();
            Vec3 dashDir = red ? rightDir : rightDir.scale(-1);

            Vec3 leadingShieldOffset = dashDir.scale(1.1).add(look.scale(0.4));
            this.setPos(owner.getX() + leadingShieldOffset.x, owner.getY() + 0.3, owner.getZ() + leadingShieldOffset.z);

            float targetYaw = red ? owner.getYRot() + 90.0F : owner.getYRot() - 90.0F;
            this.setYRot(targetYaw);
            this.setXRot(owner.getXRot() * 0.5F);

            if (animState == 0 && !level().isClientSide()) {
                AABB hitZone = new AABB(
                        getX() - 0.85, getY() - 0.9, getZ() - 0.85,
                        getX() + 0.85, getY() + 1.1, getZ() + 0.85
                );

                List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, hitZone,
                        e -> e != owner && e.isAlive());

                if (!targets.isEmpty()) {
                    LivingEntity target = targets.get(0);
                    setActionState(2);
                    deathTicks = 0;
                    explodeImpact(target, red, dashDir);
                    return;
                }
            }

            if (level().isClientSide() && tickCount % 2 == 0) {
                int color = red ? 0xFF1A1A : 0x00B2FF;
                level().addParticle(new DustParticleOptions(color, 1.0f),
                        getX() + (random.nextDouble() - 0.5) * 0.3,
                        getY() + random.nextDouble() * 1.2,
                        getZ() + (random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
        } else if (!level().isClientSide()) {
            this.discard();
        }
    }

    private void explodeExpirySparks(boolean red, Vec3 driftDirection) {
        ServerLevel serverLevel = (ServerLevel) level();
        Vec3 centerPos = this.position().add(0, 0.6, 0);

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, red ? 0.8f : 1.4f);

        for (int i = 0; i < 6; i++) {
            double randomAngle = random.nextDouble() * 2.0 * Math.PI;
            double speed = 0.08 + random.nextDouble() * 0.2;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel, centerPos.x, centerPos.y, centerPos.z);
            spark.applyPreset(red ? SparkPresets.POLARITY_RED_SHIELD : SparkPresets.POLARITY_BLUE_SHIELD);

            spark.forcedVelocity = new Vec3(
                    Math.cos(randomAngle) * speed + driftDirection.x * 0.08,
                    (random.nextDouble() - 0.4) * 0.15,
                    Math.sin(randomAngle) * speed + driftDirection.z * 0.08
            );
            serverLevel.addFreshEntity(spark);
        }
    }
    private void spawnExpiryTrailSparks(boolean red, Vec3 dashDirection) {
        ServerLevel serverLevel = (ServerLevel) level();

        Vec3 centerPos = this.position().add(0, 0.6, 0);

        Vec3 playerVelocity = owner != null ? owner.getDeltaMovement() : Vec3.ZERO;

        for (int i = 0; i < 5; i++) {
            double randomAngle = random.nextDouble() * 2.0 * Math.PI;
            double speed = 0.05 + random.nextDouble() * 0.15;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel, centerPos.x, centerPos.y, centerPos.z);
            spark.applyPreset(red ? SparkPresets.POLARITY_RED_SHIELD : SparkPresets.POLARITY_BLUE_SHIELD);

            spark.forcedVelocity = new Vec3(
                    Math.cos(randomAngle) * speed + (dashDirection.x * 0.05) + playerVelocity.x,
                    ((random.nextDouble() - 0.4) * 0.1) + playerVelocity.y,
                    Math.sin(randomAngle) * speed + (dashDirection.z * 0.05) + playerVelocity.z
            );

            serverLevel.addFreshEntity(spark);
        }
    }

    private void explodeImpact(LivingEntity target, boolean red, Vec3 pushDirection) {

        Vec3 knockback = pushDirection.add(0.0, 0.35, 0.0).normalize().scale(1.3);
        target.setDeltaMovement(knockback);
        target.hurtMarked = true;
        if (target instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetEntityMotionPacket(target));
        }

        ServerLevel serverLevel = (ServerLevel) level();

        if (owner != null) {
            var source = owner instanceof net.minecraft.world.entity.player.Player
                    ? serverLevel.damageSources().playerAttack((net.minecraft.world.entity.player.Player) owner)
                    : serverLevel.damageSources().mobAttack(owner);
            target.hurtServer(serverLevel, source, 14.0f);
        }

        if (owner != null) {
            owner.setDeltaMovement(owner.getDeltaMovement().scale(0.2));
            owner.hurtMarked = true;
        }


        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.3f, red ? 0.75f : 1.35f);
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6f, 1.5f);

        Vec3 impactPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, impactPos.x, impactPos.y, impactPos.z, 1, 0, 0, 0, 0);

        int visualColor = red ? 0xFF1A1A : 0x00B2FF;
        DustParticleOptions explosionDust = new DustParticleOptions(visualColor, 1.4f);

        serverLevel.sendParticles(explosionDust, impactPos.x, impactPos.y, impactPos.z, 12, 0.4, 0.4, 0.4, 0.1);

        for (int i = 0; i < 22; i++) {

            double theta = random.nextDouble() * 2.0 * Math.PI;
            double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
            double speed = 0.15 + random.nextDouble() * 0.5;
            double velocityX = Math.sin(phi) * Math.cos(theta) * speed;
            double velocityY = Math.cos(phi) * speed + 0.15;
            double velocityZ = Math.sin(phi) * Math.sin(theta) * speed;

            SparkEntity detSpark = new SparkEntity(ModEntities.SPARK, serverLevel, impactPos.x, impactPos.y, impactPos.z);
            detSpark.applyPreset(red ? SparkPresets.POLARITY_RED_SHIELD : SparkPresets.POLARITY_BLUE_SHIELD);

            detSpark.forcedVelocity = new Vec3(velocityX, velocityY, velocityZ);
            detSpark.setNoGravity(false);

            serverLevel.addFreshEntity(detSpark);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(IS_RED, false);
        builder.define(ACTION_STATE, 0);
    }


    public int getOwnerId() { return entityData.get(OWNER_ID); }
    public void setOwnerId(int v) { entityData.set(OWNER_ID, v); }

    public boolean isRed() { return entityData.get(IS_RED); }
    public void setIsRed(boolean v) { entityData.set(IS_RED, v); }

    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in) {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 64.0 * 64.0; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e) { return false; }
    @Override public boolean canBeCollidedWith(Entity e) { return false; }
}