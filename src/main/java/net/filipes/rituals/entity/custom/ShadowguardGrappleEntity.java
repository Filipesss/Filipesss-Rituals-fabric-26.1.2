package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class ShadowguardGrappleEntity extends ThrowableProjectile {

    public static final float SPEED          = 1.8f;
    private static final float PULL_SPEED    = 2f;
    private static final int   MAX_LIFETIME  = 120;
    private static final float MAX_RANGE     = 32.0f;
    private static final float DAMAGE        = 10.0f;
    private static final float ORBIT_RADIUS  = 0.4f;

    private boolean hooked      = false;
    private Vec3    hookPos     = null;
    private int     pullTick    = 0;
    private static final int PULL_DELAY = 6;

    private LivingEntity hookedEntity = null;

    private SparkEntity orbitSparkA = null;
    private SparkEntity orbitSparkB = null;

    public ShadowguardGrappleEntity(EntityType<? extends ShadowguardGrappleEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public ShadowguardGrappleEntity(Level level, LivingEntity owner) {
        this(ModEntities.SHADOWGUARD_GRAPPLE, level);
        this.setOwner(owner);

        Vec3 look = owner.getLookAngle();
        this.setPos(
                owner.getX() + look.x * 0.8,
                owner.getEyeY() - 0.1,
                owner.getZ() + look.z * 0.8
        );
        this.setDeltaMovement(look.normalize().scale(SPEED));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());

        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0f, 1.4f);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;

        Entity rangeOwner = getOwner();
        if (rangeOwner != null && position().distanceTo(rangeOwner.position()) > MAX_RANGE) {
            discardOrbitSparks();
            discard();
            return;
        }

        if (hooked) {
            pullTick++;
            if (pullTick < PULL_DELAY) return;

            Entity owner = getOwner();
            if (!(owner instanceof ServerPlayer player) || !player.isAlive()) {
                discard();
                return;
            }

            Vec3 target = (hookedEntity != null && hookedEntity.isAlive())
                    ? hookedEntity.position().add(0, hookedEntity.getBbHeight() * 0.5, 0)
                    : hookPos;

            Vec3 toHook = target.subtract(player.position());
            double dist = toHook.length();

            if (dist < 1.5) {
                player.setDeltaMovement(0, 0.3, 0);
                player.connection.send(new ClientboundSetEntityMotionPacket(
                        player.getId(), player.getDeltaMovement()));
                discard();
                return;
            }

            Vec3 pull = toHook.normalize().scale(PULL_SPEED);
            player.setDeltaMovement(pull.x, pull.y + 0.1, pull.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(
                    player.getId(), player.getDeltaMovement()));

        } else {

            if (orbitSparkA == null || !orbitSparkA.isAlive()) {
                orbitSparkA = createOrbitSpark((ServerLevel) level(), 0);
            }
            if (orbitSparkB == null || !orbitSparkB.isAlive()) {
                orbitSparkB = createOrbitSpark((ServerLevel) level(), (float) Math.PI);
            }

            float angle = (float) Math.toRadians(tickCount * 54f);
            updateOrbitSpark(orbitSparkA, angle);
            updateOrbitSpark(orbitSparkB, angle + (float) Math.PI);

            if (tickCount >= MAX_LIFETIME) {
                discardOrbitSparks();
                discard();
            }
        }
    }

    private SparkEntity createOrbitSpark(ServerLevel level, float angleOffset) {
        float angle = (float) Math.toRadians(tickCount * 54f) + angleOffset;
        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                getX() + Math.cos(angle) * ORBIT_RADIUS,
                getY(),
                getZ() + Math.sin(angle) * ORBIT_RADIUS);
        spark.applyPreset(SparkPresets.SHADOWGUARD_GRAPPLE_TRAIL);
        spark.setNoGravity(true);
        spark.forcedVelocity = Vec3.ZERO;
        spark.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(spark);
        return spark;
    }

    private void updateOrbitSpark(SparkEntity spark, float angle) {
        if (spark == null || !spark.isAlive()) return;
        spark.setPos(
                getX() + Math.cos(angle) * ORBIT_RADIUS,
                getY(),
                getZ() + Math.sin(angle) * ORBIT_RADIUS
        );
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;
    }

    private void discardOrbitSparks() {
        if (orbitSparkA != null && orbitSparkA.isAlive()) orbitSparkA.discard();
        if (orbitSparkB != null && orbitSparkB.isAlive()) orbitSparkB.discard();
        orbitSparkA = null;
        orbitSparkB = null;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide() || hooked) return;

        hooked  = true;
        hookPos = result.getLocation();
        setDeltaMovement(Vec3.ZERO);
        setPos(hookPos.x, hookPos.y, hookPos.z);

        discardOrbitSparks();

        ServerLevel level = (ServerLevel) level();
        spawnImpactSparks(level, hookPos.x, hookPos.y, hookPos.z);

        level.addFreshEntity(new ScreenShakeEntity(level,
                hookPos, 18f, 0.45f, 12));

        level.playSound(null, hookPos.x, hookPos.y, hookPos.z,
                SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS,
                1.2f, 0.8f + random.nextFloat() * 0.2f);
        level.playSound(null, hookPos.x, hookPos.y, hookPos.z,
                SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.9f, 1.1f);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide() || hooked) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;
        if (target == getOwner()) return;

        Entity owner = getOwner();
        DamageSource source = (owner instanceof LivingEntity living)
                ? level().damageSources().indirectMagic(this, living)
                : level().damageSources().magic();
        target.hurt(source, DAMAGE);

        hooked       = true;
        hookedEntity = target;
        hookPos      = target.position().add(0, target.getBbHeight() * 0.5, 0);
        setDeltaMovement(Vec3.ZERO);
        setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());

        discardOrbitSparks();
        spawnImpactSparks((ServerLevel) level(),
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());

        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == getOwner()) return false;
        return target instanceof LivingEntity;
    }
    private void spawnImpactSparks(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 6; i++) {
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.IMPACT_SPARK_REGULAR);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi   = Math.acos(2.0 * random.nextDouble() - 1.0);
            double speed = 0.2 + random.nextDouble() * 0.35;
            spark.forcedVelocity = new Vec3(
                    Math.sin(phi) * Math.cos(theta) * speed,
                    Math.cos(phi) * speed,
                    Math.sin(phi) * Math.sin(theta) * speed);
            level.addFreshEntity(spark);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) discardOrbitSparks();
        super.remove(reason);
    }

    public boolean isHooked()   { return hooked; }
    public Vec3    getHookPos() { return hookPos; }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override protected void readAdditionalSaveData(ValueInput input) {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}

    @Override public boolean hurtServer(ServerLevel level, DamageSource src, float amount) { return false; }
    @Override public boolean isPickable()                 { return false; }
    @Override public boolean isPushable()                 { return false; }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
}