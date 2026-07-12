package net.filipes.rituals.entity.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.effect.ConductivityHelper;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.DepthstrikeItem;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.jspecify.annotations.NonNull;

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
    private final float DAMAGE = 17.0f;

    public static final Set<UUID> CHARGED_PLAYERS = new HashSet<>();

    public static final float CHARGED_SPEED_MULTIPLIER = 1.2f;

    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int trailHead   = 0;
    public int trailSize   = 0;
    public int landingTick = -1;

    private boolean     trailSparkSpawned = false;
    private SparkEntity trailSpark        = null;
    public  boolean     isCharged         = false;

    private static final int    AURA_SPARK_COUNT     = 3;
    private static final double AURA_RADIUS          = 0.45;
    private static final double AURA_ORBIT_SPEED_DEG = 18.0;
    private static final double AURA_BOB_SPEED_DEG    = 9.0;
    private static final double AURA_BOB_HEIGHT       = 0.15;

    private final SparkEntity[] chargeAuraSparks = new SparkEntity[AURA_SPARK_COUNT];

    private static final int    SPLINTER_COUNT           = 4;
    private static final double SPLINTER_CONE_SPREAD_DEG = 24.0;

    private static final double SPLINTER_SPAWN_OFFSET_ENTITY = 0.9;

    private static final double KNOCKBACK_RADIUS         = 4.0;
    private static final double KNOCKBACK_STRENGTH       = 1.3;
    private static final double DIRECT_HIT_PUSH_STRENGTH = 1.5;
    private static final double DIRECT_HIT_PUSH_LIFT     = 0.12;
    private static final double WALL_SPLINTER_LATERAL_SPREAD = 0.5;

    private static final int SPLINTER_DELAY_TICKS = 3;
    private static final int INITIAL_HIT_SPARK_COUNT = 3;

    private boolean chargedSplitScheduled = false;
    private int     pendingSplitTick      = -1;
    private Vec3    pendingImpactPos;
    private Vec3    pendingDir;
    private boolean pendingGroundHit;
    private UUID    pendingExcludeUuid;

    private int weaponStage = 1;
    public void setWeaponStage(int stage) { this.weaponStage = stage; }
    private boolean silentlyAbandoned = false;

    public void abandonSilently() {
        this.silentlyAbandoned = true;
    }
    private boolean hitAnyEntity = false;

    public boolean hasLandedOrHit() {
        return landingTick >= 0 || hitAnyEntity;
    }

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
        if (silentlyAbandoned) {
            discard();
            return;
        }

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
                        ItemStack returned = getPickupItem();
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
        if (flightHitClearTick > 0 && tickCount >= flightHitClearTick) {
            hitEntitiesThisFlight.clear();
            flightHitClearTick = -1;
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

        if (!level().isClientSide() && isCharged && !isInGround() && !returningToOwner) {
            updateChargeAura((ServerLevel) level());
        } else if (!level().isClientSide()) {
            discardChargeAura();
        }

        if (!level().isClientSide() && !returningToOwner && isInGround()
                && landingTick >= 0 && tickCount - landingTick >= RETURN_DELAY) {
            returningToOwner  = true;
            unstuckFromGround = false;
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            playSound(SoundEvents.TRIDENT_RETURN, 0.6f, 0.8f);
        }

        if (!level().isClientSide() && pendingSplitTick >= 0 && tickCount >= pendingSplitTick) {
            pendingSplitTick = -1;
            splitIntoSplinters((ServerLevel) level(), pendingImpactPos, pendingDir, pendingGroundHit, pendingExcludeUuid);
        }
    }

    private void updateChargeAura(ServerLevel level) {
        double baseX = getX();
        double baseY = getY() + 0.15;
        double baseZ = getZ();
        double bob = Math.sin(Math.toRadians(tickCount * AURA_BOB_SPEED_DEG)) * AURA_BOB_HEIGHT;

        for (int i = 0; i < AURA_SPARK_COUNT; i++) {
            double angle = Math.toRadians(tickCount * AURA_ORBIT_SPEED_DEG) + i * (2.0 * Math.PI / AURA_SPARK_COUNT);
            double px = baseX + Math.cos(angle) * AURA_RADIUS;
            double py = baseY + bob;
            double pz = baseZ + Math.sin(angle) * AURA_RADIUS;

            SparkEntity spark = chargeAuraSparks[i];
            if (spark == null || !spark.isAlive()) {
                spark = new SparkEntity(ModEntities.SPARK, level, px, py, pz);
                spark.applyPreset(SparkPresets.DEPTHSTRIKE_TRAIL_GROUND_SHOCK);
                spark.setNoGravity(true);
                spark.setDeltaMovement(Vec3.ZERO);
                spark.forcedVelocity = Vec3.ZERO;
                level.addFreshEntity(spark);
                chargeAuraSparks[i] = spark;
            } else {
                spark.setPos(px, py, pz);
                spark.setDeltaMovement(Vec3.ZERO);
                spark.forcedVelocity = Vec3.ZERO;
            }
        }
    }

    private void discardChargeAura() {
        for (int i = 0; i < chargeAuraSparks.length; i++) {
            SparkEntity spark = chargeAuraSparks[i];
            if (spark != null && spark.isAlive()) spark.discard();
            chargeAuraSparks[i] = null;
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
    protected boolean tryPickup(Player player) {
        if (silentlyAbandoned) {
            discard();
            return false;
        }
        Entity owner = getOwner();
        if (owner != null && !player.getUUID().equals(owner.getUUID())) {
            return false;
        }
        return super.tryPickup(player);
    }

    @Override
    protected @NonNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (returningToOwner) return;
        Entity hit   = result.getEntity();
        Entity owner = getOwner();

        boolean isOwner = owner != null && hit.getUUID().equals(owner.getUUID());
        Vec3 vel = getDeltaMovement();

        if (!isOwner) {
            DamageSource source = owner != null
                    ? level().damageSources().trident(this, owner)
                    : level().damageSources().trident(this, this);

            float conductivityBonus = (hit instanceof LivingEntity livingHit)
                    ? ConductivityHelper.getDamageBonus(livingHit)
                    : 0f;

            hit.hurt(source, DAMAGE + conductivityBonus);
            hitAnyEntity = true;

            if (hit instanceof LivingEntity livingHitForReset) {
                livingHitForReset.invulnerableTime = 0;
            }

            if (hit instanceof LivingEntity livingHit
                    && livingHit != owner
                    && !hitEntitiesThisFlight.contains(livingHit.getUUID())) {

                hitEntitiesThisFlight.add(livingHit.getUUID());
                flightHitClearTick = tickCount + 8;

                if (this.weaponStage >= 2) {
                    ConductivityHelper.applyOrStack(livingHit);
                } else {
                    ConductivityHelper.applyLevelOne(livingHit);
                }
            }
        }

        if (isCharged && !isOwner) {
            hit.setDeltaMovement(hit.getDeltaMovement()
                    .add(vel.normalize().scale(DIRECT_HIT_PUSH_STRENGTH))
                    .add(0, DIRECT_HIT_PUSH_LIFT, 0));
            if (hit instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
            setDeltaMovement(Vec3.ZERO);
        } else {
            hit.setDeltaMovement(hit.getDeltaMovement().add(vel.normalize().scale(0.4)));
            setDeltaMovement(-vel.x * 0.6, Math.abs(vel.y) * 0.4 + 0.15, -vel.z * 0.6);
        }

        playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
        if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
        discardChargeAura();

        if (!level().isClientSide()) {
            ServerLevel sv = (ServerLevel) level();
            Vec3 impactPos = new Vec3(hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ());

            ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                    impactPos, isCharged ? 20f : 16f, isCharged ? 0.5f : 0.35f, 14);
            level().addFreshEntity(shake);

            if (!isOwner) {
                spawnRandomSparks(sv, impactPos, INITIAL_HIT_SPARK_COUNT);
            }

            if (isCharged && !chargedSplitScheduled) {
                chargedSplitScheduled = true;
                Vec3 flatDir = new Vec3(vel.x, 0, vel.z);
                if (flatDir.lengthSqr() < 1.0E-6) flatDir = new Vec3(1, 0, 0);
                pendingImpactPos   = impactPos;
                pendingDir         = flatDir.normalize();
                pendingGroundHit   = false;
                pendingExcludeUuid = hit.getUUID();
                pendingSplitTick   = tickCount + SPLINTER_DELAY_TICKS;
            } else if (!isCharged) {
                spawnDefaultWave(sv, impactPos.x, impactPos.y, impactPos.z);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        Vec3 velBeforeStop = getDeltaMovement();
        super.onHitBlock(result);

        if (landingTick >= 0) return;
        landingTick = tickCount;

        if (trailSpark != null && trailSpark.isAlive()) {
            trailSpark.discard();
        }
        discardChargeAura();

        if (level().isClientSide()) return;

        ServerLevel sv = (ServerLevel) level();
        Vec3 impactPos = result.getLocation();

        ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                impactPos, isCharged ? 20f : 16f, isCharged ? 0.5f : 0.35f, 14);
        level().addFreshEntity(shake);

        if (isCharged && !chargedSplitScheduled) {
            chargedSplitScheduled = true;
            Vec3 rawDir = velBeforeStop.lengthSqr() > 1.0E-4
                    ? velBeforeStop
                    : Vec3.directionFromRotation(getXRot(), getYRot());
            Vec3 flatDir = new Vec3(rawDir.x, 0, rawDir.z);
            if (flatDir.lengthSqr() < 1.0E-6) flatDir = new Vec3(1, 0, 0);
            pendingImpactPos   = impactPos;
            pendingDir         = flatDir.normalize();
            pendingGroundHit   = true;
            pendingExcludeUuid = null;
            pendingSplitTick   = tickCount + SPLINTER_DELAY_TICKS;
        } else if (!isCharged) {
            spawnDefaultWave(sv, impactPos.x, impactPos.y, impactPos.z);
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

    private void splitIntoSplinters(ServerLevel sv, Vec3 impactPos, Vec3 dir, boolean groundHit, UUID excludeUuid) {
        knockbackNearby(sv, impactPos, excludeUuid);

        double halfSpreadRad = Math.toRadians(SPLINTER_CONE_SPREAD_DEG / 2.0);
        Entity owner = getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity le ? le : null;
        if (livingOwner == null) return;

        double spawnOffset = groundHit ? 0.0 : SPLINTER_SPAWN_OFFSET_ENTITY;

        Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize();

        for (int i = 0; i < SPLINTER_COUNT; i++) {
            double t = (2.0 * i / (SPLINTER_COUNT - 1)) - 1.0;
            double offsetRad = t * halfSpreadRad;
            Vec3 splinterDir = rotateAroundY(dir, offsetRad);

            Vec3 spawnPos = impactPos.add(splinterDir.scale(spawnOffset));
            if (groundHit) {
                spawnPos = spawnPos.add(perp.scale(t * WALL_SPLINTER_LATERAL_SPREAD));
            }

            sv.addFreshEntity(new DepthstrikeSplinterEntity(sv, livingOwner, spawnPos, splinterDir));
        }
    }

    private static Vec3 rotateAroundY(Vec3 dir, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = dir.x * cos - dir.z * sin;
        double z = dir.x * sin + dir.z * cos;
        return new Vec3(x, dir.y, z).normalize();
    }
    private void spawnRandomSparks(ServerLevel level, Vec3 pos, int count) {
        for (int i = 0; i < count; i++) {
            double theta = level.getRandom().nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * level.getRandom().nextDouble() - 1.0);
            double speed = 0.16 + level.getRandom().nextDouble() * 0.14;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x, pos.y, pos.z);
            spark.applyPreset(SparkPresets.LIGHTNING_TRIPLE_RED);
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * speed,
                    Math.abs(Math.cos(phi)) * speed,
                    Math.sin(phi) * Math.sin(theta) * speed
            );
            level.addFreshEntity(spark);
        }
    }

    private void knockbackNearby(ServerLevel level, Vec3 center, UUID excludeUuid) {
        AABB box = new AABB(
                center.x - KNOCKBACK_RADIUS, center.y - 2, center.z - KNOCKBACK_RADIUS,
                center.x + KNOCKBACK_RADIUS, center.y + 2, center.z + KNOCKBACK_RADIUS);
        Entity owner = getOwner();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == owner) continue;
            if (excludeUuid != null && target.getUUID().equals(excludeUuid)) continue;
            Vec3 diff = target.position().subtract(center);
            double dist = diff.length();
            if (dist < 0.1) continue;
            double strength = KNOCKBACK_STRENGTH * (1.0 - dist / KNOCKBACK_RADIUS);
            if (strength <= 0) continue;
            Vec3 impulse = diff.normalize().scale(strength).add(0, 0.5, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(impulse));

            if (target instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
            discardChargeAura();
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
        out.putInt("WeaponStage", weaponStage);
    }

    @Override
    public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        returningToOwner  = in.getBooleanOr("ReturningToOwner", false);
        isCharged         = in.getBooleanOr("IsCharged", false);
        unstuckFromGround = in.getBooleanOr("UnstuckFromGround", false);
        fastReturn        = in.getBooleanOr("FastReturn", false);
        weaponStage       = in.getIntOr("WeaponStage", 1);
    }
}