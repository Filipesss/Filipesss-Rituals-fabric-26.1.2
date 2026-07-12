package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TemporalShieldEntity extends Entity {

    private static final EntityDataAccessor<Boolean> EXPLODING =
            SynchedEntityData.defineId(TemporalShieldEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CURRENT_RADIUS =
            SynchedEntityData.defineId(TemporalShieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(TemporalShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DAMAGE_FLASH =
            SynchedEntityData.defineId(TemporalShieldEntity.class, EntityDataSerializers.INT);

    private static final float MAX_RADIUS = 2.5f;
    private static final float BLAST_RADIUS = 5.5f;
    private static final int MAX_AGE = 400;
    private static final int MAX_HITS = 4;
    private static final int EXPLOSION_DURATION = 20;

    private int lifeTimeTicks = 0;
    private int hitCount = 0;
    private int explosionTicks = 0;
    private int hitCooldownTicks = 0;

    private final List<Integer> immuneEntityIds = new ArrayList<>();
    private static final int[] TEMPORAL_COLORS = { 0x4ae061, 0xed4747, 0x719be3, 0xcc82e0 };

    public float clientRadius = 0f;
    public float prevClientRadius = 0f;
    public LivingEntity owner;

    public TemporalShieldEntity(EntityType<? extends TemporalShieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public TemporalShieldEntity(EntityType<? extends TemporalShieldEntity> type, Level level, LivingEntity owner) {
        this(type, level);
        this.owner = owner;
        if (!level.isClientSide()) {
            this.setOwnerId(owner.getId());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.owner == null && getOwnerId() != -1) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) {
                this.owner = le;
            }
        }

        if (level().isClientSide()) {
            prevClientRadius = clientRadius;
            clientRadius = lerp(clientRadius, getCurrentRadius(), 0.5f);
            return;
        }

        lifeTimeTicks++;

        if (getDamageFlash() > 0) {
            this.entityData.set(DAMAGE_FLASH, getDamageFlash() - 1);
        }
        if (this.hitCooldownTicks > 0) {
            this.hitCooldownTicks--;
        }

        if (lifeTimeTicks == 1 && level() instanceof ServerLevel serverLevel) {
            spawnInitialEffects(serverLevel);
        }

        if (entityData.get(EXPLODING)) {
            explosionTicks++;

            float progress = (float) explosionTicks / EXPLOSION_DURATION;
            float expProgress = progress * progress;

            float currentExplosiveRadius = lerp(MAX_RADIUS, BLAST_RADIUS, expProgress);
            entityData.set(CURRENT_RADIUS, currentExplosiveRadius);

            damageSurroundingEntities(currentExplosiveRadius);

            if (explosionTicks >= EXPLOSION_DURATION) {
                triggerFinalBlastVisuals();
                discard();
            }
            return;
        }

        float radius = entityData.get(CURRENT_RADIUS);
        radius = lerp(radius, MAX_RADIUS, 0.25f);
        entityData.set(CURRENT_RADIUS, radius);

        if (level() instanceof ServerLevel serverLevel && lifeTimeTicks % 10 == 0) {
            spawnAmbientShieldHull(serverLevel, radius);
        }

        if (lifeTimeTicks >= MAX_AGE) {
            startExploding();
            return;
        }

        AABB detectionBox = this.getBoundingBox().inflate(radius, 1.5, radius);

        immuneEntityIds.removeIf(id -> {
            Entity entity = level().getEntity(id);
            return entity == null || !detectionBox.intersects(entity.getBoundingBox());
        });

        for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, detectionBox)) {
            Entity projOwner = projectile.getOwner();
            if (projOwner == this.owner || (projOwner != null && projOwner.getId() == getOwnerId()) || !projectile.isAlive()) {
                continue;
            }

            reflectProjectile(projectile, projOwner);

            if (this.owner != null) {
                projectile.setOwner(this.owner);
            }

            if (level() instanceof ServerLevel serverLevel) {
                spawnImpactRipple(serverLevel, projectile.position(), 0x00B2FF);
            }

            level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0f, 1.6f);

            registerHit();
            if (entityData.get(EXPLODING)) return;
        }
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, detectionBox)) {
            if (target == this.owner || target.getId() == getOwnerId() || !target.isAlive()) {
                continue;
            }
            if (immuneEntityIds.contains(target.getId())) continue;

            DamageSource source = level().damageSources().magic();
            target.hurtServer((ServerLevel) level(), source, 4.0f);

            Vec3 knockbackVector = target.position().subtract(this.position());
            Vec3 horizontalPush = new Vec3(knockbackVector.x, 0, knockbackVector.z).normalize().scale(1.6);

            target.setDeltaMovement(horizontalPush.x, 0.45, horizontalPush.z);
            target.hurtMarked = true;
            immuneEntityIds.add(target.getId());

            if (level() instanceof ServerLevel serverLevel) {
                spawnImpactRipple(serverLevel, target.position().add(0, 1.0, 0), 0xFF0000);
            }

            level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.2f, 0.7f);

            registerHit();
            if (entityData.get(EXPLODING)) return;
        }
    }

    private void spawnInitialEffects(ServerLevel serverLevel) {
        for (int hexColor : TEMPORAL_COLORS) {
            serverLevel.sendParticles(new DustParticleOptions(hexColor, 1.5f),
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    6, 0.3, 0.3, 0.3, 0.2);
        }
        spawnVisualSparks(serverLevel, 10, 0.4);
    }

    private void spawnAmbientShieldHull(ServerLevel level, float radius) {
        double u = random.nextDouble();
        double v = random.nextDouble();
        double theta = u * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * v - 1.0);

        double x = Math.sin(phi) * Math.cos(theta) * radius;
        double y = (Math.cos(phi) * radius * 0.7) + 0.6;
        double z = Math.sin(phi) * Math.sin(theta) * radius;

        int color = TEMPORAL_COLORS[random.nextInt(TEMPORAL_COLORS.length)];
        level.sendParticles(new DustParticleOptions(color, 0.7f), getX() + x, getY() + y, getZ() + z, 1, 0, 0, 0, 0);
    }

    private void spawnImpactRipple(ServerLevel level, Vec3 impactPos, int color) {
        level.sendParticles(new DustParticleOptions(color, 1.0f), impactPos.x, impactPos.y, impactPos.z, 4, 0.2, 0.2, 0.2, 0.1);

        level.sendParticles(ParticleTypes.GUST, impactPos.x, impactPos.y, impactPos.z, 1, 0, 0, 0, 0);
    }
    private void reflectProjectile(Projectile projectile, Entity attacker) {
        Vec3 currentVelocity = projectile.getDeltaMovement();
        double speed = currentVelocity.length();

        Vec3 newVelocity;
        if (attacker != null && attacker.isAlive()) {
            Vec3 toAttacker = attacker.getEyePosition().subtract(projectile.position());
            if (toAttacker.lengthSqr() > 1.0E-4) {
                newVelocity = toAttacker.normalize().scale(speed * 1.5);
            } else {
                newVelocity = currentVelocity.scale(-1.5);
            }
        } else {
            newVelocity = currentVelocity.scale(-1.5);
        }

        projectile.setDeltaMovement(newVelocity);
        projectile.hurtMarked = true;
    }

    private void spawnVisualSparks(ServerLevel level, int count, double baseSpeed) {
        for (int i = 0; i < count; i++) {
            double yaw = this.random.nextDouble() * Math.PI * 2.0;
            double pitch = (this.random.nextDouble() - 0.5) * Math.PI;
            double randomizedSpeed = baseSpeed * (0.5 + this.random.nextDouble() * 1.3);

            double cosPitch = Math.cos(pitch);
            Vec3 velocity = new Vec3(
                    Math.cos(yaw) * cosPitch * randomizedSpeed,
                    (Math.sin(pitch) * randomizedSpeed) + 0.3,
                    Math.sin(yaw) * cosPitch * randomizedSpeed
            );

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, this.getX(), this.getY() + 0.5, this.getZ());

            switch (this.random.nextInt(4)) {
                case 0 -> spark.applyPreset(SparkPresets.TEMPORAL_GREEN);
                case 1 -> spark.applyPreset(SparkPresets.TEMPORAL_RED);
                case 2 -> spark.applyPreset(SparkPresets.TEMPORAL_PURPLE);
                default -> spark.applyPreset(SparkPresets.TEMPORAL_BLUE);
            }

            spark.setNoGravity(false);

            spark.maxLifetime = 14 + random.nextInt(12);

            spark.setDeltaMovement(velocity);
            spark.forcedVelocity = velocity;
            level.addFreshEntity(spark);
        }
    }

    private void registerHit() {
        if (this.lifeTimeTicks < 2) return;

        if (this.hitCooldownTicks > 0) return;

        this.hitCount++;
        this.hitCooldownTicks = 10;
        this.entityData.set(DAMAGE_FLASH, 15);

        if (level() instanceof ServerLevel serverLevel) {
            spawnVisualSparks(serverLevel, 8, 0.6);
        }

        if (this.hitCount >= MAX_HITS) {
            startExploding();
        }
    }

    public void startExploding() {
        if (entityData.get(EXPLODING)) return;
        entityData.set(EXPLODING, true);

        level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WIND_CHARGE_BURST, SoundSource.NEUTRAL, 1.5f, 0.4f);
    }

    private void damageSurroundingEntities(float activeRadius) {
        AABB blastZone = this.getBoundingBox().inflate(activeRadius, 2.0, activeRadius);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, blastZone,
                e -> e != this.owner && e.getId() != getOwnerId() && e.isAlive());

        DamageSource explosionSource = level().damageSources().explosion(this, this.owner);
        for (LivingEntity victim : targets) {
            victim.hurtServer((ServerLevel) level(), explosionSource, 8.0f);
            Vec3 push = victim.position().subtract(this.position()).normalize().scale(1.4);
            victim.setDeltaMovement(push.x, 0.5, push.z);
            victim.hurtMarked = true;
        }
    }

    private void triggerFinalBlastVisuals() {
        if (level() instanceof ServerLevel serverLevel) {
            for (int hexColor : TEMPORAL_COLORS) {
                serverLevel.sendParticles(new DustParticleOptions(hexColor, 1.5f),
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        8, 0.8, 0.5, 0.8, 0.3);
            }

            serverLevel.sendParticles(ParticleTypes.GUST, this.getX(), this.getY() + 0.5, this.getZ(), 4, 1.5, 0.6, 1.5, 0.1);

            spawnVisualSparks(serverLevel, 15, 0.65);

            level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.4f, 0.9f);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(EXPLODING, false);
        builder.define(CURRENT_RADIUS, 0f);
        builder.define(DAMAGE_FLASH, 0);
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public float getCurrentRadius() { return entityData.get(CURRENT_RADIUS); }
    public int getDamageFlash() { return entityData.get(DAMAGE_FLASH); }
    public int getOwnerId() { return entityData.get(OWNER_ID); }
    public void setOwnerId(int v) { entityData.set(OWNER_ID, v); }

    @Override public boolean shouldBeSaved()                                    { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)              {}
    @Override protected void addAdditionalSaveData(ValueOutput out)             {}
    @Override public PushReaction getPistonPushReaction()                       { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                       { return false; }
    @Override public boolean isPushable()                                       { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                { return d < 64.0 * 64.0; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                           { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                        { return false; }
}