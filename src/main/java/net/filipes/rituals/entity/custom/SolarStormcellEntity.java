package net.filipes.rituals.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.network.TwinsStartCooldownPacket;
import net.filipes.rituals.sound.ModSounds;
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

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SolarStormcellEntity extends Entity {

    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(SolarStormcellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(SolarStormcellEntity.class, EntityDataSerializers.INT);

    public LivingEntity owner;
    private LivingEntity target;
    private SparkEntity  trailSpark;

    private boolean launched             = false;
    private Vec3    velocity             = null;
    private boolean velocityInitialized  = false;
    private Vec3    launchDirection      = null;
    private int     flightTick           = 0;
    private int     chainCount           = 0;
    private final Set<UUID> hitUUIDs     = new HashSet<>();
    private boolean resonanceActive = false;
    private int     bonusChains     = 0;
    private float damageMultiplier = 1.124f;

    private static final float SPEED                 = 1.5f;
    private static final float TURN_RATE             = 0.75f;
    private static final float SPARK_Y_OFFSET        = 0.35f;
    private static final float CHAIN_DAMAGE          = 19.0f;
    private static final float CHAIN_DAMAGE_FALLOFF  = 0.10f;
    private static final int   MAX_CHAINS            = 7;
    private static final int   MAX_FLIGHT_TICKS      = 80;
    private static final float CHAIN_RADIUS          = 15.0f;
    private static final int   HIT_SPARK_COUNT       = 6;
    private boolean bounceMode       = false;
    private UUID    lastHitUUID      = null;
    private static final float BOUNCE_CHAIN_RADIUS = 3.0f;
    private static final int   BOUNCE_MAX_CHAINS   = 60;
    private int chainDelayTick = 0;
    private static final int CHAIN_DELAY = 4;

    public SolarStormcellEntity(EntityType<? extends SolarStormcellEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.entityData.set(OWNER_ID, owner.getId());
    }
    public void setBounceMode(boolean bounce) {
        this.bounceMode = bounce;
    }

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }

    public void launch(@Nullable LivingEntity firstTarget, Vec3 forwardDir) {
        this.launchDirection = forwardDir.normalize();
        this.launched        = true;

        if (firstTarget != null) {
            this.target = firstTarget;
            this.entityData.set(TARGET_ID, firstTarget.getId());
            if (!bounceMode) hitUUIDs.add(firstTarget.getUUID());
            lastHitUUID = firstTarget.getUUID();
        }

        trailSpark = new SparkEntity(ModEntities.SPARK, level(),
                getX(), getY() + SPARK_Y_OFFSET, getZ());
        trailSpark.applyPreset(SparkPresets.SOLAR_STORMCELL_TRAIL);
        trailSpark.setNoGravity(true);
        trailSpark.setDeltaMovement(Vec3.ZERO);
        trailSpark.forcedVelocity = Vec3.ZERO;
        level().addFreshEntity(trailSpark);

        level().addFreshEntity(new ScreenShakeEntity(level(),
                new Vec3(getX(), getY(), getZ()), 8f, 0.15f, 6));
        this.playSound(ModSounds.LIGHTNING_BOLT, 1.0f, 1.2f);
    }

    public int getOwnerId()  { return this.entityData.get(OWNER_ID); }
    public int getTargetId() { return this.entityData.get(TARGET_ID); }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (owner == null) {
                Entity e = level().getEntity(getOwnerId());
                if (e instanceof LivingEntity le) owner = le;
            }
            return;
        }

        if (!launched) return;

        if (target == null && getTargetId() != -1) {
            Entity e = level().getEntity(getTargetId());
            if (e instanceof LivingEntity le) target = le;
        }
        if (chainDelayTick > 0) {
            chainDelayTick--;
            // Keep trail spark in place during the pause
            if (trailSpark != null && trailSpark.isAlive()) {
                trailSpark.setPos(getX(), getY() + SPARK_Y_OFFSET, getZ());
                trailSpark.setDeltaMovement(Vec3.ZERO);
                trailSpark.forcedVelocity = Vec3.ZERO;
            }
            return;
        }

        if (!velocityInitialized) {
            velocityInitialized = true;
            velocity = (target != null)
                    ? target.getBoundingBox().getCenter().subtract(this.position()).normalize().scale(SPEED)
                    : launchDirection.scale(SPEED);
        }
        if (velocity == null) velocity = launchDirection.scale(SPEED);

        if (target != null && target.isAlive()) {
            Vec3 toTarget = target.getBoundingBox().getCenter()
                    .subtract(this.position()).normalize();
            velocity = velocity.normalize()
                    .add(toTarget.scale(TURN_RATE))
                    .normalize()
                    .scale(SPEED);
        }

        Vec3 previousPos = this.position();
        this.setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);

        if (trailSpark != null && trailSpark.isAlive()) {
            trailSpark.setPos(getX(), getY() + SPARK_Y_OFFSET, getZ());
            trailSpark.setDeltaMovement(Vec3.ZERO);
            trailSpark.forcedVelocity = Vec3.ZERO;
        }

        flightTick++;
        if (flightTick > MAX_FLIGHT_TICKS)                { finishAndDiscard(); return; }
        if (target != null && !target.isAlive())           { finishAndDiscard(); return; }

        if (target != null && flightTick > 0) {
            AABB targetBB = target.getBoundingBox().inflate(0.2);
            boolean segmentHit   = targetBB.clip(previousPos, this.position()).isPresent();
            boolean insideTarget = targetBB.contains(this.position());

            if (segmentHit || insideTarget) {
                onHitTarget((ServerLevel) level());
            }
        }
    }

    private void onHitTarget(ServerLevel sl) {
        float damage = CHAIN_DAMAGE * damageMultiplier
                * Math.max(0.5f, 1.0f - chainCount * CHAIN_DAMAGE_FALLOFF);
        DamageSource src = (owner != null)
                ? sl.damageSources().indirectMagic(this, owner)
                : sl.damageSources().magic();
        target.hurtServer(sl, src, damage);

        lastHitUUID = target.getUUID();
        if (!bounceMode) hitUUIDs.add(target.getUUID());

        Vec3 hitPos = target.getBoundingBox().getCenter();
        sl.addFreshEntity(new ScreenShakeEntity(level(), hitPos, 14f, 0.3f, 10));

        for (int i = 0; i < HIT_SPARK_COUNT; i++) {
            double angle = (Math.PI * 2.0 / HIT_SPARK_COUNT) * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, sl,
                    hitPos.x, hitPos.y, hitPos.z);
            spark.applyPreset(SparkPresets.SOLAR_STORMCELL_SINGLE);
            spark.forcedVelocity = new Vec3(
                    Math.cos(angle) * 0.35,
                    0.2 + sl.getRandom().nextDouble() * 0.25,
                    Math.sin(angle) * 0.35);
            sl.addFreshEntity(spark);
        }

        this.playSound(net.filipes.rituals.sound.ModSounds.LIGHTNING_BOLT, 1.0f,
                0.9f + sl.getRandom().nextFloat() * 0.2f);

        if (resonanceActive) spawnResonanceFragment(sl, target);

        chainCount++;
        int maxChains = bounceMode ? BOUNCE_MAX_CHAINS : MAX_CHAINS + bonusChains + getDayBonus();
        if (chainCount >= maxChains) { finishAndDiscard(); return; }

        LivingEntity next = findChainTarget(sl);
        if (next == null) { finishAndDiscard(); return; }

        if (!bounceMode) hitUUIDs.add(next.getUUID());
        target              = next;
        entityData.set(TARGET_ID, next.getId());
        velocity            = null;
        velocityInitialized = false;
        flightTick          = 0;
        chainDelayTick      = CHAIN_DELAY;
    }

    private LivingEntity findChainTarget(ServerLevel sl) {
        float radius = bounceMode ? BOUNCE_CHAIN_RADIUS : CHAIN_RADIUS;
        Vec3 travelDir = (velocity != null && velocity.lengthSqr() > 0)
                ? velocity.normalize() : Vec3.ZERO;

        double bestScore = Double.MAX_VALUE;
        LivingEntity best = null;

        for (LivingEntity le : sl.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e.isAlive()
                        && e != owner
                        && (bounceMode
                        ? !e.getUUID().equals(lastHitUUID)
                        : !hitUUIDs.contains(e.getUUID())))) {

            double dist = this.distanceTo(le);
            double dot = travelDir.lengthSqr() > 0
                    ? travelDir.dot(le.position().subtract(this.position()).normalize())
                    : 0;
            double score = dist * (1.0 - dot * 0.4);
            if (score < bestScore) { bestScore = score; best = le; }
        }
        return best;
    }
    public void activateResonance() {
        if (resonanceActive) return;
        resonanceActive = true;
        bonusChains     = 2;

        this.playSound(ModSounds.LIGHTNING_BOLT, 0.9f, 0.5f);
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.8f);

        if (level() instanceof ServerLevel sl) {
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2.0 / 8) * i;
                SparkEntity spark = new SparkEntity(ModEntities.SPARK, sl,
                        getX(), getY(), getZ());
                spark.applyPreset(SparkPresets.SOLAR_STORMCELL_SINGLE);
                spark.forcedVelocity = new Vec3(
                        Math.cos(angle) * 0.4, 0.35, Math.sin(angle) * 0.4);
                sl.addFreshEntity(spark);
            }
        }
    }
    private int getDayBonus() {
        if (!(level() instanceof ServerLevel sl)) return 0;
        long time = sl.getDefaultClockTime() % 24000L;
        if (time >= 4000 && time < 8000) return 2;
        if (time >= 2000 && time < 10000) return 1;
        return 0;
    }

    private void spawnResonanceFragment(ServerLevel sl, LivingEntity fragmentTarget) {
        LunarFragmentEntity fragment = new LunarFragmentEntity(ModEntities.LUNAR_FRAGMENT, sl);
        fragment.setResonanceMode(true);
        if (owner != null) {
            fragment.setOwner(owner);
            fragment.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ()); // ← player position
        } else {
            fragment.setPos(getX(), getY(), getZ());
        }
        fragment.setSlot(0);
        fragment.launch(fragmentTarget, chainCount);
        sl.addFreshEntity(fragment);
    }

    private void finishAndDiscard() {
        // Resonance finale: burst extra fragments at the last target
        if (resonanceActive && target != null && target.isAlive()
                && level() instanceof ServerLevel sl) {
            for (int i = 0; i < 2; i++) {
                spawnResonanceFragment(sl, target);
            }
        }
        if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            if (trailSpark != null && trailSpark.isAlive()) trailSpark.discard();
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID,  -1);
        builder.define(TARGET_ID, -1);
    }

    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}

    @Override public boolean shouldBeSaved()                                     { return false; }
    @Override public PushReaction getPistonPushReaction()                        { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                        { return false; }
    @Override public boolean isPushable()                                        { return false; }
    @Override public boolean canCollideWith(Entity e)                            { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                         { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                 { return d < 64.0 * 64.0; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a)  { return false; }
}