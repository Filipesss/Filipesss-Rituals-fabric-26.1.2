package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.particle.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ShadeshatterSpellEntity extends ThrowableProjectile {

    private static final int   MAX_LIFETIME   = 120;
    public  static final float SPEED          = 0.95f;
    private static final float DAMAGE         = 18.0f;
    private static final int   SPARK_DELAY    = 5;

    private static final double TRAIL_RADIUS_MIN = 0.18;
    private static final double TRAIL_RADIUS_MAX = 0.38;
    private static final int    TRAIL_PARTICLES_PER_TICK = 2;

    private boolean hasImpacted   = false;
    private int     tickSpark     = -1;
    private double  impactX, impactY, impactZ;

    public ShadeshatterSpellEntity(EntityType<? extends ShadeshatterSpellEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ShadeshatterSpellEntity(Level level, LivingEntity owner) {
        this(ModEntities.SHADESHATTER_SPELL, level);
        this.setOwner(owner);

        Vec3 look = owner.getLookAngle();
        double spawnX = owner.getX() + look.x * 1.2;
        double spawnY = owner.getEyeY() - 0.1;
        double spawnZ = owner.getZ() + look.z * 1.2;

        this.setPos(spawnX, spawnY, spawnZ);
        this.xo = spawnX;
        this.yo = spawnY;
        this.zo = spawnZ;

        this.setDeltaMovement(look.normalize().scale(SPEED));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level();

            if (tickSpark >= 0 && tickCount >= tickSpark) {
                tickSpark = -1;
                spawnDelayedSparks(serverLevel);
                discard();
                return;
            }

            setDeltaMovement(getDeltaMovement().add(0, -0.015, 0));

            if (!hasImpacted) {
                spawnOrbitalTrail(serverLevel);
            }

            if (!hasImpacted && tickCount >= MAX_LIFETIME) {
                discard();
            }
        }
    }

    private void spawnOrbitalTrail(ServerLevel level) {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-4) return;

        Vec3 dir = motion.normalize();

        Vec3 arbitrary = Math.abs(dir.y) < 0.99 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 right = dir.cross(arbitrary).normalize();
        Vec3 up    = dir.cross(right).normalize();

        double cx = getX();
        double cy = getY();
        double cz = getZ();

        for (int i = 0; i < TRAIL_PARTICLES_PER_TICK; i++) {
            double theta  = random.nextDouble() * Math.PI * 2.0;
            double radius = TRAIL_RADIUS_MIN + random.nextDouble() * (TRAIL_RADIUS_MAX - TRAIL_RADIUS_MIN);

            Vec3 offset = right.scale(Math.cos(theta) * radius)
                    .add(up.scale(Math.sin(theta) * radius));

            double px = cx + offset.x;
            double py = cy + offset.y;
            double pz = cz + offset.z;

            level.sendParticles(
                    ModParticles.LIGHTNING_BOLT_MINI,
                    px, py, pz,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == getOwner()) return false;
        if (target instanceof LivingEntity) return true;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide() && !hasImpacted) {
            Vec3 pos = position();
            onImpact(pos, true);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!level().isClientSide() && !hasImpacted && hit instanceof LivingEntity target) {
            Entity owner = getOwner();
            DamageSource source = (owner instanceof LivingEntity living)
                    ? level().damageSources().indirectMagic(this, living)
                    : level().damageSources().magic();
            target.hurt(source, DAMAGE);
            onImpact(position(), false);
        }
    }

    private void onImpact(Vec3 pos, boolean blockHit) {
        hasImpacted = true;
        setDeltaMovement(Vec3.ZERO);
        impactX = pos.x;
        impactY = pos.y;
        impactZ = pos.z;

        ServerLevel level = (ServerLevel) level();

        level.playSound(null, impactX, impactY, impactZ,
                SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE,
                1.0f, 1.2f + random.nextFloat() * 0.2f);

        LightningExplosionEntity explosion = new LightningExplosionEntity(
                ModEntities.LIGHTNING_EXPLOSION, level);
        explosion.setPos(impactX, impactY + 0.3, impactZ);
        explosion.setEntityScale(1.25f);
        level.addFreshEntity(explosion);

        LightningTrailEntity trail = new LightningTrailEntity(
                ModEntities.LIGHTNING_TRAIL, level);
        trail.setPos(impactX, impactY + 0.8, impactZ);
        trail.setEntityScale(2.0f);
        level.addFreshEntity(trail);

        level.addFreshEntity(new ScreenShakeEntity(level,
                new Vec3(impactX, impactY, impactZ), 16f, 0.4f, 12));

        spawnImpactSparks(level, pos);

        if (blockHit) {
            tickSpark = tickCount + SPARK_DELAY;
        } else {
            discard();
        }
    }

    private void spawnDelayedSparks(ServerLevel level) {
        for (int i = 0; i < 2; i++) {
            LightningSparkEntity spark = new LightningSparkEntity(
                    ModEntities.LIGHTNING_SPARK, level);
            spark.setPos(impactX, impactY + 0.5, impactZ);
            spark.setEntityScale(2.0f);
            level.addFreshEntity(spark);
        }
    }

    private void spawnImpactSparks(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 10; i++) {
            double angle = (Math.PI * 2.0 / 10) * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x, pos.y, pos.z);
            spark.applyPreset(SparkPresets.SHADESHATTER_SPELL_IMPACT);
            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * 0.5,
                    0.15 + random.nextDouble() * 0.3,
                    Math.sin(angle) * 0.5);
            level.addFreshEntity(spark);
        }
        for (int i = 0; i < 6; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x, pos.y, pos.z);
            spark.applyPreset(SparkPresets.SHADESHATTER_SPELL_IMPACT);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double spd   = 0.2 + random.nextDouble() * 0.35;
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * spd,
                    Math.abs(Math.cos(phi)) * spd,
                    Math.sin(phi) * Math.sin(theta) * spd);
            level.addFreshEntity(spark);
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override protected void readAdditionalSaveData(ValueInput input)  {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}

    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) { return false; }
    @Override public boolean isPickable()                 { return false; }
    @Override public boolean isPushable()                 { return false; }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
}