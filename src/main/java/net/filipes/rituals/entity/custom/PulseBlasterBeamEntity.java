package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PulseBlasterBeamEntity extends Projectile {

    private static final float BASE_DAMAGE   = 8.0f;
    private static final int   FIRE_SECONDS  = 5;
    private static final float BEAM_SPEED    = 1.5f;
    private static final int   MAX_AGE       = 80;

    private final Level storedWorld;

    public PulseBlasterBeamEntity(EntityType<? extends PulseBlasterBeamEntity> type, Level level) {
        super(type, level);
        this.storedWorld = level;
        this.setNoGravity(true);
    }

    public PulseBlasterBeamEntity(Level level, LivingEntity owner) {
        this(ModEntities.PULSE_BLASTER_BEAM, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0f, BEAM_SPEED, 0.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void tick() {
        super.tick();

        Vec3 currentPos = this.position();
        Vec3 velocity   = this.getDeltaMovement();
        Vec3 nextPos    = currentPos.add(velocity);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                storedWorld, this, currentPos, nextPos,
                this.getBoundingBox().expandTowards(velocity).inflate(1.0),
                entity -> !entity.isSpectator()
                        && entity != this.getOwner()
                        && !(entity instanceof PulseBlasterBeamEntity)  // ← add
                        && !(entity instanceof SparkEntity)              // ← add
                        && !(entity instanceof BurstSparkEntity)
        );

        if (entityHit != null && !storedWorld.isClientSide()) {
            Entity      target      = entityHit.getEntity();
            ServerLevel serverLevel = (ServerLevel) storedWorld;
            Entity      owner       = this.getOwner();

            float multiplier = (owner instanceof LivingEntity le)
                    ? PulseBlasterItem.getDamageMultiplier(le.getUUID())
                    : 1.0f;

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 0;
            }

            target.hurtServer(
                    serverLevel,
                    serverLevel.damageSources().thrown(this, owner),
                    BASE_DAMAGE * multiplier
            );
            target.igniteForSeconds(FIRE_SECONDS);
            spawnImpactSparks(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            this.discard();
            return;
        }

        BlockHitResult blockHit = storedWorld.clip(new ClipContext(
                currentPos, nextPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
        ));

        if (blockHit.getType() != HitResult.Type.MISS && !storedWorld.isClientSide()) {
            BlockPos    firePos     = blockHit.getBlockPos().relative(blockHit.getDirection());
            ServerLevel serverLevel = (ServerLevel) storedWorld;

            if (storedWorld.isEmptyBlock(firePos)) {
                storedWorld.setBlockAndUpdate(firePos, BaseFireBlock.getState(storedWorld, firePos));
            }
            Vec3 loc = blockHit.getLocation();
            spawnImpactSparks(loc.x, loc.y, loc.z);
            this.discard();
            return;
        }

        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        if (this.tickCount > MAX_AGE) {
            this.discard();
        }
    }
    private void spawnImpactSparks(double cx, double cy, double cz) {
        if (storedWorld.isClientSide()) return;
        for (int i = 0; i < 3; i++) {
            double angle  = Math.random() * 2.0 * Math.PI;
            double upward = Math.random() * 0.4 + 0.1;
            double speed  = Math.random() * 0.4 + 0.2;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, storedWorld, cx, cy, cz);
            spark.applyPreset(SparkPresets.PULSE_BEAM_IMPACT);
            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * speed,
                    upward,
                    Math.sin(angle) * speed
            );
            storedWorld.addFreshEntity(spark);
            spark.setRemainingFireTicks(0);
        }
    }
}