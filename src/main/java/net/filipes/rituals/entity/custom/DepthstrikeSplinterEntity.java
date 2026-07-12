package net.filipes.rituals.entity.custom;

import net.filipes.rituals.effect.ConductivityHelper;
import net.filipes.rituals.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DepthstrikeSplinterEntity extends ThrowableProjectile {

    private static final int   MAX_LIFETIME    = 10;
    private static final float SPEED           = 0.78f;
    private static final float DIRECT_DAMAGE   = 11.0f;
    private static final float SPLASH_DAMAGE   = 9.0f;
    private static final double SPLASH_RADIUS  = 2.5;
    private static final double PREEMPT_CHECK_MARGIN = 0.35;
    private static final double PREEMPT_PULLBACK     = 0.25;
    private static final double VISUAL_PULLBACK = 0.22;

    private SparkEntity trailSpark = null;

    public DepthstrikeSplinterEntity(EntityType<? extends DepthstrikeSplinterEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public DepthstrikeSplinterEntity(Level level, LivingEntity owner, Vec3 origin, Vec3 direction) {
        this(ModEntities.DEPTHSTRIKE_SPLINTER, level);
        this.setOwner(owner);
        this.setPos(origin.x, origin.y, origin.z);
        this.xo = origin.x;
        this.yo = origin.y;
        this.zo = origin.z;

        Vec3 vel = direction.normalize().scale(SPEED);
        this.setDeltaMovement(vel);
        faceDirection(vel);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            checkImminentWallHit((ServerLevel) level());
            if (this.isRemoved()) return;
        }

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6) {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            faceDirection(motion);
        }

        super.tick();

        if (this.isRemoved()) return;

        if (!level().isClientSide()) {
            ServerLevel sv = (ServerLevel) level();

            if (trailSpark == null || !trailSpark.isAlive()) {
                trailSpark = new SparkEntity(ModEntities.SPARK, sv, getX(), getY(), getZ());
                trailSpark.applyPreset(SparkPresets.LIGHTNING_TRIPLE_RED_BIG);
                trailSpark.setNoGravity(true);
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
                sv.addFreshEntity(trailSpark);
            } else {
                trailSpark.setPos(getX(), getY(), getZ());
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
            }

            if (tickCount >= MAX_LIFETIME) {
                detonate(sv, position());
            }
        }
    }

    private void checkImminentWallHit(ServerLevel level) {
        Vec3 from = position();
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6) return;

        Vec3 dir = motion.normalize();
        double checkDist = motion.length() + PREEMPT_CHECK_MARGIN;
        Vec3 to = from.add(dir.scale(checkDist));

        ClipContext ctx = new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
        BlockHitResult hit = level.clip(ctx);

        if (hit.getType() == BlockHitResult.Type.BLOCK) {
            Vec3 explodePos = hit.getLocation().subtract(dir.scale(PREEMPT_PULLBACK));
            detonate(level, explodePos);
        }
    }

    private void faceDirection(Vec3 dir) {
        double horizontalDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.setYRot((float) (Mth.atan2(dir.x, dir.z) * (180.0 / Math.PI)));
        this.setXRot((float) (Mth.atan2(dir.y, horizontalDist) * (180.0 / Math.PI)));
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == getOwner()) return false;
        if (target instanceof LivingEntity) return true;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;

        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity livingHit) {
            hurtIgnoringRecentInvulnerability(livingHit, DIRECT_DAMAGE);
        }

        detonate((ServerLevel) level(), position());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            detonate((ServerLevel) level(), result.getLocation());
        }
    }

    private void detonate(ServerLevel level, Vec3 pos) {
        if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();

        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS,
                0.9f, 1.3f);

        Vec3 motion = getDeltaMovement();
        Vec3 visualPos = motion.lengthSqr() > 1.0E-6
                ? pos.subtract(motion.normalize().scale(VISUAL_PULLBACK))
                : pos;

        LightningExplosionEntity flash = new LightningExplosionEntity(ModEntities.LIGHTNING_EXPLOSION, level);
        flash.setPos(visualPos.x, visualPos.y + 0.2, visualPos.z);
        flash.setEntityScale(1.1f);
        level.addFreshEntity(flash);

        int sparkCount = 1 + level.getRandom().nextInt(2); // 1 or 2
        for (int i = 0; i < sparkCount; i++) {
            double theta = level.getRandom().nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * level.getRandom().nextDouble() - 1.0);
            double speed = 0.16 + level.getRandom().nextDouble() * 0.14;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, visualPos.x, visualPos.y + 0.15, visualPos.z);
            spark.applyPreset(SparkPresets.LIGHTNING_TRIPLE_RED_BIG);
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * speed,
                    Math.abs(Math.cos(phi)) * speed,
                    Math.sin(phi) * Math.sin(theta) * speed
            );
            level.addFreshEntity(spark);
        }

        Entity owner = getOwner();
        AABB box = AABB.ofSize(pos, SPLASH_RADIUS * 2, SPLASH_RADIUS * 2, SPLASH_RADIUS * 2);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive())) {
            double dist = target.position().distanceTo(pos);
            if (dist > SPLASH_RADIUS) continue;
            float dmg = (float) (SPLASH_DAMAGE * (1.0 - dist / SPLASH_RADIUS));
            if (dmg > 0.3f) {
                hurtIgnoringRecentInvulnerability(target, dmg);
            }
        }

        discard();
    }

    private void hurtIgnoringRecentInvulnerability(LivingEntity target, float amount) {
        Entity owner = getOwner();
        DamageSource source = (owner instanceof LivingEntity living)
                ? level().damageSources().trident(this, living)
                : level().damageSources().trident(this, this);

        float conductivityBonus = ConductivityHelper.getDamageBonus(target);

        target.invulnerableTime = 0;
        target.hurtServer((ServerLevel) level(), source, amount + conductivityBonus);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && trailSpark != null && trailSpark.isAlive()) {
            trailSpark.discard();
        }
        super.remove(reason);
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