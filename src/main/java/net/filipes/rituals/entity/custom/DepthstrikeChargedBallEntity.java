package net.filipes.rituals.entity.custom;

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

public class DepthstrikeChargedBallEntity extends ThrowableProjectile {

    private static final int   MAX_LIFETIME          = 120;
    public  static final float SPEED                 = 0.35f;
    private static final float DIRECT_DAMAGE         = 24.0f;
    private static final float AOE_DAMAGE            = 14.0f;
    private static final float EXPLOSION_RADIUS      = 3.5f;
    private static final int   AMBIENT_SPARK_INTERVAL = 3;

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

    // -------------------------------------------------------------------------
    // Tick — ThrowableProjectile.tick() moves the entity AND raycasts for hits
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        // ThrowableProjectile applies 0.99 drag each tick — counteract it so
        // the ball maintains constant speed.
        if (!hasExploded) {
            Vec3 motion = getDeltaMovement();
            double currentSpeed = motion.length();
            if (currentSpeed > 0.001) {
                setDeltaMovement(motion.normalize().scale(SPEED));
            }
        }

        super.tick(); // handles movement + block/entity raycasting → onHit*

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

    // -------------------------------------------------------------------------
    // Collision filtering — ThrowableProjectile calls canHitEntity() before
    // registering an entity hit, so override that (not canCollideWith).
    // -------------------------------------------------------------------------

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == getOwner()) return false;
        if (target instanceof LivingEntity) return true;
        return super.canHitEntity(target);
    }

    // -------------------------------------------------------------------------
    // Hit handlers — called by ThrowableProjectile's internal raycast
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Explosion
    // -------------------------------------------------------------------------

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

        // AOE damage
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

        // Impact sparks — use DEPTHSTRIKE_CHARGED_BALL_IMPACT preset
        // Ring
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
        // Scattered
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

        // LIGHTNING_EXPLOSION — scaled up
        LightningExplosionEntity explosion = new LightningExplosionEntity(
                ModEntities.LIGHTNING_EXPLOSION, level);
        explosion.setPos(explodeX, explodeY, explodeZ);
        explosion.setEntityScale(2.5f);
        level.addFreshEntity(explosion);

        tickTrail = tickCount + 4;
        tickSpark = tickCount + 8;
    }

    // -------------------------------------------------------------------------
    // Delayed effects
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Visual centre for spark/explosion placement
    // -------------------------------------------------------------------------

    private Vec3 visualCenter() {
        return new Vec3(getX(), getY() + 0.1, getZ());
    }

    // -------------------------------------------------------------------------
    // Trail spark
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Ambient sparks
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Cleanup / persistence / flags
    // -------------------------------------------------------------------------

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