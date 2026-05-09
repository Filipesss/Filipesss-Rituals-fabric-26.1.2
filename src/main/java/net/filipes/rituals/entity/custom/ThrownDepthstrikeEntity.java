package net.filipes.rituals.entity.custom;

import net.filipes.rituals.effect.ConductivityHelper;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ThrownDepthstrikeEntity extends AbstractArrow {

    public static final int   TRAIL_LENGTH = 10;
    public static final float HEX_LINE_LEN = 2.5f;
    private boolean returningToOwner = false;
    private static final int    RETURN_DELAY = 40;
    private static final double RETURN_SPEED = 1.8;
    private static final double RECALL_SPEED = 4.5;
    private boolean fastReturn = false;
    private boolean unstuckFromGround = false;
    private final Set<UUID> hitEntitiesThisFlight = new HashSet<>();
    private int flightHitClearTick = -1;

    public static final Set<UUID> CHARGED_PLAYERS = new HashSet<>();

    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int trailHead   = 0;
    public int trailSize   = 0;
    public int landingTick = -1;

    private boolean     trailSparkSpawned = false;
    private SparkEntity trailSpark        = null;
    public  boolean     isCharged         = false;

    private int secondWaveTick = -1;

    public ThrownDepthstrikeEntity(EntityType<? extends ThrownDepthstrikeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownDepthstrikeEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_DEPTHSTRIKE, owner, level, stack, null);

        UUID uuid = owner.getUUID();
        if (CHARGED_PLAYERS.remove(uuid)) {
            isCharged = true;
        }
    }

    @Override
    public void tick() {

        if (!level().isClientSide() && returningToOwner) {
            Entity owner = getOwner();
            if (owner instanceof LivingEntity livingOwner && livingOwner.isAlive()) {
                setNoGravity(true);

                if (isInGround()) {
                    setPos(getX(), getY() + 1.5, getZ());
                    unstuckFromGround = false;
                    super.tick();
                    return;
                }

                Vec3 target  = livingOwner.position().add(0, livingOwner.getBbHeight() * 0.5, 0);
                Vec3 toOwner = target.subtract(position());
                double dist  = toOwner.length();

                if (dist < 1.4) {
                    if (owner instanceof Player player) {
                        ItemStack returned = getDefaultPickupItem();
                        if (!player.hasInfiniteMaterials()) {
                            if (!player.getInventory().add(returned)) {
                                player.drop(returned, false);
                            }
                        }
                    }
                    playSound(SoundEvents.TRIDENT_RETURN, 1.0f, 1.0f);
                    discard();
                    return;
                }

                double baseSpeed = fastReturn ? RECALL_SPEED : RETURN_SPEED;
                double speed = Math.min(baseSpeed + (1.0 / dist), baseSpeed * 2.5);
                setDeltaMovement(toOwner.normalize().scale(speed));

            } else {
                returningToOwner   = false;
                unstuckFromGround  = false;
                setNoGravity(false);
            }
        }

        super.tick();

        if (!isInGround() && !level().isClientSide()) {

            SparkPreset trailPreset = isCharged
                    ? SparkPresets.DEPTHSTRIKE_TRAIL_GROUND_SHOCK
                    : SparkPresets.DEPTHSTRIKE_TRAIL;

            if (trailSpark == null || !trailSpark.isAlive()) {
                trailSpark = new SparkEntity(ModEntities.SPARK, level(), getX(), getY(), getZ());
                trailSpark.applyPreset(trailPreset);
                trailSpark.setNoGravity(true);
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
                level().addFreshEntity(trailSpark);
            } else {
                trailSpark.setPos(getX(), getY(), getZ());
                trailSpark.setYRot(getYRot());
                trailSpark.setXRot(getXRot());
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
            }
        }

        if (!level().isClientSide() && !returningToOwner && isInGround()
                && landingTick >= 0 && tickCount - landingTick >= RETURN_DELAY) {
            returningToOwner  = true;
            unstuckFromGround = false;
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            playSound(SoundEvents.TRIDENT_RETURN, 0.6f, 0.8f);
        }

        if (!level().isClientSide() && secondWaveTick > 0 && tickCount >= secondWaveTick) {
            secondWaveTick = -1;
            spawnSecondWave(new Vec3(getX(), getY(), getZ()));
        }
    }

    public boolean isThrownInGround() {
        return this.isInGround();
    }
    public void recallNow() {
        if (level().isClientSide()) return;
        returningToOwner  = true;
        fastReturn        = true;
        unstuckFromGround = false;
        if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
        playSound(SoundEvents.TRIDENT_RETURN, 1.1f, 0.9f);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (returningToOwner) return;
        Entity hit   = result.getEntity();
        Entity owner = getOwner();

        DamageSource source = owner != null
                ? level().damageSources().trident(this, owner)
                : level().damageSources().trident(this, this);

        // Base damage + conductivity bonus
        float baseDamage = 9.0f;
        float conductivityBonus = (hit instanceof LivingEntity livingHit)
                ? ConductivityHelper.getDamageBonus(livingHit)
                : 0f;

        hit.hurt(source, baseDamage + conductivityBonus);

        if (hit instanceof LivingEntity livingHit
                && !hitEntitiesThisFlight.contains(livingHit.getUUID())) {
            hitEntitiesThisFlight.add(livingHit.getUUID());
            flightHitClearTick = tickCount + 5;
            ConductivityHelper.applyOrStack(livingHit);
        }

        Vec3 vel = getDeltaMovement();
        hit.setDeltaMovement(hit.getDeltaMovement().add(vel.normalize().scale(0.4)));
        setDeltaMovement(-vel.x * 0.6, Math.abs(vel.y) * 0.4 + 0.15, -vel.z * 0.6);

        playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
        if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();

        if (!level().isClientSide()) {
            ServerLevel sv = (ServerLevel) level();
            double cx = hit.getX(), cy = hit.getY(), cz = hit.getZ();

            ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                    new Vec3(cx, cy, cz), isCharged ? 20f : 16f, isCharged ? 0.5f : 0.35f, 14);
            level().addFreshEntity(shake);

            if (isCharged) {
                spawnChargedFirstWave(sv, cx, cy, cz);
                knockbackNearby(sv, cx, cy, cz);
                secondWaveTick = tickCount + 8;
            } else {
                spawnDefaultWave(sv, cx, cy, cz);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (landingTick >= 0) return;
        landingTick = tickCount;

        if (trailSpark != null && trailSpark.isAlive()) {
            trailSpark.discard();
        }

        if (level().isClientSide()) return;

        ServerLevel sv = (ServerLevel) level();
        double cx = getX(), cy = getY(), cz = getZ();

        ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                new Vec3(cx, cy, cz), isCharged ? 20f : 16f, isCharged ? 0.5f : 0.35f, 14);
        level().addFreshEntity(shake);

        if (isCharged) {
            spawnChargedFirstWave(sv, cx, cy, cz);
            knockbackNearby(sv, cx, cy, cz);
            secondWaveTick = tickCount + 20;
        } else {
            spawnDefaultWave(sv, cx, cy, cz);
        }
    }

    private void spawnDefaultWave(ServerLevel sv, double cx, double cy, double cz) {
        double speed = 0.45;
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3.0 * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, sv, cx, cy + 0.1, cz);
            spark.applyPreset(SparkPresets.LIGHTNING_TRIPLE);
            spark.forcedVelocity = new Vec3(Math.cos(angle) * speed, 0.3, Math.sin(angle) * speed);
            sv.addFreshEntity(spark);
        }
    }

    private void spawnChargedFirstWave(ServerLevel sv, double cx, double cy, double cz) {
        double speed = 0.58;
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3.0 * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, sv, cx, cy + 0.1, cz);
            spark.applyPreset(SparkPresets.LIGHTNING_TRIPLE_RED_BIG);
            spark.forcedVelocity = new Vec3(Math.cos(angle) * speed, 0.3, Math.sin(angle) * speed);
            sv.addFreshEntity(spark);
        }
    }

    private void spawnSecondWave(Vec3 pos) {
        ServerLevel sv = (ServerLevel) level();
        double speed = 1.24;
        double offsetRad = Math.toRadians(90.0);
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3.0 * i + offsetRad;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, sv,
                    pos.x, pos.y + 0.1, pos.z);
            spark.applyPreset(SparkPresets.LIGHTNING_TRIPLE_RED_BIG);
            spark.forcedVelocity = new Vec3(Math.cos(angle) * speed, 0.4, Math.sin(angle) * speed);
            sv.addFreshEntity(spark);
        }
    }
    private void knockbackNearby(ServerLevel level, double cx, double cy, double cz) {
        double radius = 4.0;
        AABB box = new AABB(cx - radius, cy - 2, cz - radius,
                cx + radius, cy + 2, cz + radius);
        Entity owner = getOwner();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == owner) continue;
            Vec3 diff = target.position().subtract(cx, cy, cz);
            double dist = diff.length();
            if (dist < 0.1) continue;
            double strength = 1.5 * (1.0 - dist / radius);
            Vec3 impulse = diff.normalize().scale(strength).add(0, 0.5, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(impulse));
        }
    }
    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && trailSpark != null && trailSpark.isAlive()) {
            trailSpark.discard();
        }
        super.remove(reason);
    }
    @Override
    protected boolean canHitEntity(Entity target) {
        if (returningToOwner) return false;
        return super.canHitEntity(target);
    }

    @Override
    public boolean isPushable() {
        return !returningToOwner && super.isPushable();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DEPTHSTRIKE);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean("ReturningToOwner", returningToOwner);
        out.putBoolean("IsCharged", isCharged);
        out.putBoolean("UnstuckFromGround", unstuckFromGround);
        out.putBoolean("FastReturn", fastReturn);
    }

    @Override
    public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        returningToOwner  = in.getBooleanOr("ReturningToOwner", false);
        isCharged         = in.getBooleanOr("IsCharged", false);
        unstuckFromGround = in.getBooleanOr("UnstuckFromGround", false);
        fastReturn = in.getBooleanOr("FastReturn", false);
    }
}