package net.filipes.rituals.entity.custom;

import net.filipes.rituals.effect.ConductivityHelper;
import net.filipes.rituals.entity.ModEntities;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DepthstrikeChargedBallEntity extends ThrowableProjectile {

    private static final int   MAX_LIFETIME          = 120;
    public  static final float SPEED                 = 0.35f;
    private static final float DIRECT_DAMAGE         = 24.0f;
    private static final float AOE_DAMAGE            = 14.0f;
    private static final float EXPLOSION_RADIUS      = 3.5f;
    private static final int   AMBIENT_SPARK_INTERVAL = 3;
    private static final float[] HOMING_STRENGTHS = { 0.03f, 0.06f, 0.10f, 0.15f, 0.20f };
    private static final double  HOMING_RANGE     = 24.0;
    private static final int     HOMING_INTERVAL  = 5;

    @Nullable
    private LivingEntity homingTarget     = null;
    private          int           nextHomingSearch = 0;

    private SparkEntity trailSpark  = null;
    private boolean     hasExploded = false;

    private int    tickTrail = -1;
    private int    tickSpark = -1;
    private double explodeX, explodeY, explodeZ;

    public DepthstrikeChargedBallEntity(EntityType<? extends DepthstrikeChargedBallEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public DepthstrikeChargedBallEntity(Level level, LivingEntity owner) {
        this(ModEntities.DEPTHSTRIKE_CHARGED_BALL, level);
        this.setOwner(owner);

        Vec3 look = owner.getLookAngle();
        this.setPos(
                owner.getX() + look.x * 1.2,
                owner.getEyeY() - 0.1,
                owner.getZ() + look.z * 1.2
        );
        this.setDeltaMovement(look.normalize().scale(SPEED));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    @Override
    public void tick() {
        if (!hasExploded) {
            Vec3 motion = getDeltaMovement();
            if (motion.length() > 0.001) {
                if (!level().isClientSide()) {

                    if (tickCount >= nextHomingSearch) {
                        nextHomingSearch = tickCount + HOMING_INTERVAL;
                        homingTarget = findHomingTarget();
                    }

                    if (homingTarget != null && homingTarget.isAlive()) {
                        int amplifier = ConductivityHelper.getLevel(homingTarget) - 1;
                        if (amplifier >= 0) {
                            float strength = HOMING_STRENGTHS[amplifier];
                            Vec3 toTarget  = homingTarget.position()
                                    .add(0, homingTarget.getBbHeight() * 0.5, 0)
                                    .subtract(position())
                                    .normalize();
                            motion = motion.normalize().add(toTarget.scale(strength));
                        }
                    }
                }
                setDeltaMovement(motion.normalize().scale(SPEED));
            }
        }

        super.tick();

        if (!level().isClientSide()) {
            if (tickTrail >= 0 && tickCount >= tickTrail) {
                tickTrail = -1;
                spawnDelayedTrail((ServerLevel) level());
            }
            if (tickSpark >= 0 && tickCount >= tickSpark) {
                tickSpark = -1;
                spawnDelayedSpark((ServerLevel) level());
            }

            if (!hasExploded && isAlive()) {
                updateTrailSpark();
                if (tickCount % AMBIENT_SPARK_INTERVAL == 0) {
                    spawnAmbientSparks((ServerLevel) level());
                }
            }
        }

        if (!hasExploded && tickCount >= MAX_LIFETIME) {
            cleanupTrail();
            discard();
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
        if (!level().isClientSide() && !hasExploded) {
            triggerExplosion(null);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!level().isClientSide() && !hasExploded
                && hit instanceof LivingEntity target) {
            Entity owner = getOwner();
            DamageSource source = (owner instanceof LivingEntity living)
                    ? level().damageSources().indirectMagic(this, living)
                    : level().damageSources().magic();
            target.hurt(source, DIRECT_DAMAGE);
            triggerExplosion(target);
        }
    }
    @Nullable
    private LivingEntity findHomingTarget() {
        Entity owner = getOwner();
        AABB searchBox = new AABB(
                getX() - HOMING_RANGE, getY() - HOMING_RANGE, getZ() - HOMING_RANGE,
                getX() + HOMING_RANGE, getY() + HOMING_RANGE, getZ() + HOMING_RANGE);

        LivingEntity best     = null;
        double       bestDist = Double.MAX_VALUE;

        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (candidate == owner) continue;
            if (ConductivityHelper.getLevel(candidate) == 0) continue;   // no effect
            double dist = candidate.position().distanceToSqr(position());
            if (dist < bestDist) {
                bestDist = dist;
                best     = candidate;
            }
        }
        return best;
    }

    private void triggerExplosion(LivingEntity directHit) {
        hasExploded = true;
        cleanupTrail();
        setDeltaMovement(Vec3.ZERO);

        Vec3 c = visualCenter();
        explodeX = c.x;
        explodeY = c.y;
        explodeZ = c.z;

        ServerLevel level = (ServerLevel) level();

        level.playSound(null, explodeX, explodeY, explodeZ,
                SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE,
                1.5f, 0.5f + random.nextFloat() * 0.25f);

        level.addFreshEntity(new ScreenShakeEntity(level,
                new Vec3(explodeX, explodeY, explodeZ), 24f, 0.65f, 20));

        Entity owner = getOwner();
        DamageSource aoeSource = (owner instanceof LivingEntity living)
                ? level.damageSources().indirectMagic(this, living)
                : level.damageSources().magic();

        AABB box = new AABB(
                explodeX - EXPLOSION_RADIUS, explodeY - EXPLOSION_RADIUS, explodeZ - EXPLOSION_RADIUS,
                explodeX + EXPLOSION_RADIUS, explodeY + EXPLOSION_RADIUS, explodeZ + EXPLOSION_RADIUS);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == owner || target == directHit) continue;
            double dist = target.position().distanceTo(new Vec3(explodeX, explodeY, explodeZ));
            if (dist > EXPLOSION_RADIUS) continue;
            float dmg = AOE_DAMAGE * (1f - (float)(dist / EXPLOSION_RADIUS) * 0.4f);
            target.hurt(aoeSource, dmg);
            Vec3 push = target.position().subtract(explodeX, explodeY, explodeZ)
                    .normalize().scale(1.4).add(0, 0.5, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(push));
        }

        for (int i = 0; i < 14; i++) {
            double angle = (Math.PI * 2.0 / 14) * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, explodeX, explodeY, explodeZ);
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL_IMPACT);
            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * 0.7,
                    0.2 + random.nextDouble() * 0.4,
                    Math.sin(angle) * 0.7);
            level.addFreshEntity(spark);
        }
        // Upward
        for (int i = 0; i < 6; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, explodeX, explodeY, explodeZ);
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL_IMPACT);
            spark.forcedVelocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.4,
                    0.6 + random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.4);
            level.addFreshEntity(spark);
        }

        for (int i = 0; i < 8; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, explodeX, explodeY, explodeZ);
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL_IMPACT);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double spd   = 0.3 + random.nextDouble() * 0.5;
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * spd,
                    Math.cos(phi) * spd,
                    Math.sin(phi) * Math.sin(theta) * spd);
            level.addFreshEntity(spark);
        }

        LightningExplosionEntity explosion = new LightningExplosionEntity(
                ModEntities.LIGHTNING_EXPLOSION, level);
        explosion.setPos(explodeX, explodeY, explodeZ);
        explosion.setEntityScale(2.5f);
        level.addFreshEntity(explosion);

        tickTrail = tickCount + 4;
        tickSpark = tickCount + 8;
    }

    private void spawnDelayedTrail(ServerLevel level) {
        LightningTrailEntity trail = new LightningTrailEntity(ModEntities.LIGHTNING_TRAIL, level);
        trail.setPos(explodeX, explodeY, explodeZ);
        trail.setEntityScale(2.0f);
        level.addFreshEntity(trail);

        for (int i = 0; i < 6; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, explodeX, explodeY, explodeZ);
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL_IMPACT);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double spd   = 0.25 + random.nextDouble() * 0.35;
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * spd,
                    Math.cos(phi) * spd,
                    Math.sin(phi) * Math.sin(theta) * spd);
            level.addFreshEntity(spark);
        }
    }

    private void spawnDelayedSpark(ServerLevel level) {
        LightningSparkEntity spark = new LightningSparkEntity(ModEntities.LIGHTNING_SPARK, level);
        spark.setPos(explodeX, explodeY, explodeZ);
        spark.setEntityScale(2.0f);
        level.addFreshEntity(spark);

        for (int i = 0; i < 5; i++) {
            SparkEntity s = new SparkEntity(ModEntities.SPARK, level, explodeX, explodeY, explodeZ);
            s.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL_IMPACT);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double spd   = 0.2 + random.nextDouble() * 0.3;
            s.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * spd,
                    Math.cos(phi) * spd,
                    Math.sin(phi) * Math.sin(theta) * spd);
            level.addFreshEntity(s);
        }

        discard();
    }


    private Vec3 visualCenter() {
        return new Vec3(getX(), getY() + 0.1, getZ());
    }


    private void updateTrailSpark() {
        Vec3 c = visualCenter();
        if (trailSpark == null || !trailSpark.isAlive()) {
            trailSpark = new SparkEntity(ModEntities.SPARK, level(), c.x, c.y, c.z);
            trailSpark.applyPreset(SparkPresets.LIGHTNING_TRIPLE);
            trailSpark.setNoGravity(true);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
            level().addFreshEntity(trailSpark);
        } else {
            trailSpark.setPos(c.x, c.y, c.z);
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
        }
    }

    private void cleanupTrail() {
        if (trailSpark != null && trailSpark.isAlive()) {
            trailSpark.discard();
            trailSpark = null;
        }
    }

    private void spawnAmbientSparks(ServerLevel level) {
        Vec3 c = visualCenter();
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, c.x, c.y, c.z);
            spark.applyPreset(SparkPresets.DEPTHSTRIKE_CHARGED_BALL);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double spd   = 0.15 + random.nextDouble() * 0.25;
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * spd,
                    Math.cos(phi) * spd,
                    Math.sin(phi) * Math.sin(theta) * spd);
            level.addFreshEntity(spark);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) cleanupTrail();
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override protected void readAdditionalSaveData(ValueInput input) {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}

    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) { return false; }
    @Override public boolean isPickable()                 { return false; }
    @Override public boolean isPushable()                 { return false; }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
}