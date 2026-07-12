package net.filipes.rituals.entity.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LightningStrikeEntity extends Entity {

    private static final EntityDataAccessor<Float>   STRIKE_HEIGHT = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION      = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR_R       = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR_G       = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR_B       = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float>   DAMAGE        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   DAMAGE_RADIUS = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> IMPACT_SPARKS = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);

    public static final int APPEAR_TICKS = 6;

    public int  appearTimer;
    public int  prevAppearTimer;
    public boolean on = true;

    private int sparksSpawned = 0;
    private int novaSparksSpawned = 0;
    private UUID ownerUUID;

    public LightningStrikeEntity(EntityType<? extends LightningStrikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawnAt(ServerLevel level, double x, double y, double z,
                               float height, int duration,
                               int r, int g, int b,
                               float damage, float damageRadius,
                               int impactSparks, LivingEntity owner) {
        var e = ModEntities.LIGHTNING_STRIKE.create(level, EntitySpawnReason.TRIGGERED);
        if (e == null) return;
        e.setPos(x, y, z);
        e.setStrikeHeight(height);
        e.setDuration(duration);
        e.setR(r); e.setG(g); e.setB(b);
        e.setDamage(damage);
        e.setDamageRadius(damageRadius);
        e.setImpactSparks(impactSparks);

        if (owner != null) {
            e.ownerUUID = owner.getUUID();
        }

        level.addFreshEntity(e);
    }

    @Override
    public void tick() {
        super.tick();

        prevAppearTimer = appearTimer;
        xo = getX(); yo = getY(); zo = getZ();

        if (!on) {
            if (appearTimer > 0) appearTimer--;
            else { discard(); return; }
        } else {
            if (appearTimer < APPEAR_TICKS) appearTimer++;
        }

        if (!level().isClientSide() && on && level() instanceof ServerLevel serverLevel) {

            if (tickCount == APPEAR_TICKS + 1) {
                applyImpactDamage(serverLevel);
                spawnInstantImpactVisuals(serverLevel);
            }

            if (tickCount >= APPEAR_TICKS + 1) {
                tickNovaSparks(serverLevel);
            }

            if (tickCount >= APPEAR_TICKS) {
                tickSparkSpawning(serverLevel);
            }
        }

        if (tickCount > APPEAR_TICKS + getDuration()) {
            on = false;
        }
    }

    private void tickNovaSparks(ServerLevel level) {
        int totalNovaSparks = 14;
        if (novaSparksSpawned >= totalNovaSparks) return;

        double x = getX();
        double y = getY();
        double z = getZ();

        // Spawns 1 per tick instead of 2 to guarantee they separate cleanly over time
        double angle = (novaSparksSpawned * (Math.PI * 2.0)) / totalNovaSparks;
        angle += (level.getRandom().nextDouble() - 0.5) * 0.35; // Added more organic noise to the angle

        double speed = 0.4 + level.getRandom().nextDouble() * 0.3;
        double vy    = 0.15 + level.getRandom().nextDouble() * 0.25;

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y + 0.2, z);
        spark.applyPreset(SparkPresets.LIGHTNING_STRIKE_IMPACT);

        spark.forcedVelocity = new Vec3(
                Math.cos(angle) * speed,
                vy,
                Math.sin(angle) * speed
        );

        level.addFreshEntity(spark);
        novaSparksSpawned++;
    }

    private void tickSparkSpawning(ServerLevel level) {
        int total = getImpactSparks();
        if (sparksSpawned >= total) return;

        int elapsed = tickCount - APPEAR_TICKS;
        int duration = Math.max(1, getDuration());
        if (elapsed < 0) return;

        // Dynamic target tracking distributes sparks completely evenly across the entire duration
        int targetToSpawn = (int) Math.min(total, ((double) elapsed / duration) * total);

        while (sparksSpawned < targetToSpawn) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            double speed = 0.25 + level.getRandom().nextDouble() * 0.30;
            double vy    = 0.10 + level.getRandom().nextDouble() * 0.15;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, getX(), getY(), getZ());
            spark.applyPreset(SparkPresets.LIGHTNING_STRIKE_IMPACT);

            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * speed,
                    vy,
                    Math.sin(angle) * speed
            );

            level.addFreshEntity(spark);
            sparksSpawned++;
        }
    }

    private void applyImpactDamage(ServerLevel server) {
        float dmg    = getDamage();
        float radius = getDamageRadius();
        if (dmg <= 0f || radius <= 0f) return;

        DamageSource src = server.damageSources().lightningBolt();
        double r2 = radius * radius;
        AABB box = new AABB(
                getX() - radius, getY() - radius, getZ() - radius,
                getX() + radius, getY() + radius, getZ() + radius);

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) {
                continue;
            }

            double dx = target.getX() - getX();
            double dy = target.getY() - getY();
            double dz = target.getZ() - getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) {
                target.invulnerableTime = 0;
                target.hurtServer(server, src, dmg);
                target.addEffect(new MobEffectInstance(
                        ModStatusEffects.STUN, 60, 0, false, true, true));
            }
        }
    }

    private void spawnInstantImpactVisuals(ServerLevel level) {
        double x = getX();
        double y = getY();
        double z = getZ();
        float radius = getDamageRadius();

        int dustCount = (int) (radius * 15);
        level.sendParticles(
                new DustParticleOptions(0xAADDFF, 1.6f),
                x, y + 0.2, z,
                dustCount,
                radius * 0.5, 0.2, radius * 0.5,
                0.0
        );

        int vanillaSparks = (int) (radius * 15);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.2, z, vanillaSparks,
                radius * 0.6, 0.2, radius * 0.6, 0.4);

        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 8, 0.3, 0.1, 0.3, 0.05);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STRIKE_HEIGHT,  14f);
        builder.define(DURATION,        20);
        builder.define(COLOR_R,         80);
        builder.define(COLOR_G,        160);
        builder.define(COLOR_B,        255);
        builder.define(DAMAGE,           0f);
        builder.define(DAMAGE_RADIUS,    3f);
        builder.define(IMPACT_SPARKS,   18);
    }

    public float getStrikeHeight()          { return entityData.get(STRIKE_HEIGHT); }
    public void  setStrikeHeight(float v)   { entityData.set(STRIKE_HEIGHT, v); }

    public int   getDuration()              { return entityData.get(DURATION); }
    public void  setDuration(int v)         { entityData.set(DURATION, v); }

    public int   getR()                     { return entityData.get(COLOR_R); }
    public void  setR(int v)               { entityData.set(COLOR_R, v); }
    public int   getG()                     { return entityData.get(COLOR_G); }
    public void  setG(int v)               { entityData.set(COLOR_G, v); }
    public int   getB()                     { return entityData.get(COLOR_B); }
    public void  setB(int v)               { entityData.set(COLOR_B, v); }

    public float getDamage()                { return entityData.get(DAMAGE); }
    public void  setDamage(float v)         { entityData.set(DAMAGE, v); }

    public float getDamageRadius()          { return entityData.get(DAMAGE_RADIUS); }
    public void  setDamageRadius(float v)   { entityData.set(DAMAGE_RADIUS, v); }

    public int   getImpactSparks()          { return entityData.get(IMPACT_SPARKS); }
    public void  setImpactSparks(int v)     { entityData.set(IMPACT_SPARKS, v); }

    @Override public boolean      shouldBeSaved()                                    { return false; }
    @Override protected void      readAdditionalSaveData(ValueInput in)              {}
    @Override protected void      addAdditionalSaveData(ValueOutput out)             {}
    @Override public PushReaction  getPistonPushReaction()                           { return PushReaction.IGNORE; }
    @Override public boolean       isPickable()                                      { return false; }
    @Override public boolean       isPushable()                                      { return false; }
    @Override public boolean       shouldRenderAtSqrDistance(double d)              { return d < (512.0 * 512.0); }
    @Override public boolean       hurtServer(ServerLevel l, DamageSource s, float a){ return false; }
    @Override public boolean       canCollideWith(Entity e)                          { return false; }
    @Override public boolean       canBeCollidedWith(Entity e)                       { return false; }
}