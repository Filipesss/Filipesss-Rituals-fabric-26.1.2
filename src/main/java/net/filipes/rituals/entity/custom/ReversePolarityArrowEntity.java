package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.network.ReverseControlsPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ReversePolarityArrowEntity extends Arrow {

    public static final double BASE_DAMAGE      = 2.0;
    public static final float  SPEED_MULTIPLIER = 1.2f;

    public static final int TRAIL_LENGTH = 10;
    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int trailHead = 0;
    public int trailSize = 0;

    private record OrbitParams(double angleOffset, double speed, double radius,
                               double vertPhase, double vertFreq) {}

    public ReversePolarityArrowEntity(EntityType<? extends ReversePolarityArrowEntity> type, Level level) {
        super(type, level);
    }

    public ReversePolarityArrowEntity(Level level, LivingEntity shooter, ItemStack weapon) {
        this(ModEntities.REVERSE_POLARITY_ARROW, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    public void tick() {
        this.setCritArrow(false);
        super.tick();
        trailPositions[trailHead] = position();
        trailHead = (trailHead + 1) % TRAIL_LENGTH;
        if (trailSize < TRAIL_LENGTH) trailSize++;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;

        // Send controls reversal to the hit player if applicable
        if (target instanceof ServerPlayer targetPlayer) {
            ServerPlayNetworking.send(targetPlayer, new ReverseControlsPacket(120));
        }

        // Spawn chaotic orbit around the target for 6 seconds (120 ticks × 50ms)
        ServerLevel level = (ServerLevel) this.level();
        spawnChaoticOrbit(target, level, level.getServer(), 120);
    }

    private static void spawnChaoticOrbit(LivingEntity target, ServerLevel level,
                                          MinecraftServer server, int durationTicks) {
        var random = level.getRandom();
        Vec3 pos = target.position();
        int count = 9;

        List<SparkEntity> sparks = new ArrayList<>();
        List<OrbitParams> params = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double radius      = 0.45 + random.nextDouble() * 1.0;
            double speed       = (0.06 + random.nextDouble() * 0.18) * (random.nextBoolean() ? 1.0 : -1.0);
            double angleOffset = random.nextDouble() * Math.PI * 2.0;
            double vertPhase   = random.nextDouble() * Math.PI * 2.0;
            double vertFreq    = 0.08 + random.nextDouble() * 0.28;

            double sx = pos.x + Math.cos(angleOffset) * radius;
            double sz = pos.z + Math.sin(angleOffset) * radius;
            double sy = pos.y + 0.8 + Math.sin(vertPhase) * 0.4;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, sx, sy, sz);
            spark.applyPreset(SparkPresets.POLARITY_REVERSE);
            spark.setNoGravity(true);
            spark.setDeltaMovement(Vec3.ZERO);
            spark.forcedVelocity = Vec3.ZERO;
            level.addFreshEntity(spark);

            sparks.add(spark);
            params.add(new OrbitParams(angleOffset, speed, radius, vertPhase, vertFreq));
        }

        runOrbitTicker(target, sparks, params, 0, durationTicks, server);
    }

    private static void runOrbitTicker(LivingEntity target, List<SparkEntity> sparks,
                                       List<OrbitParams> params, int tick, int maxTicks,
                                       MinecraftServer server) {
        if (tick >= maxTicks || !target.isAlive()) {
            for (SparkEntity spark : sparks) {
                if (spark.isAlive()) spark.discard();
            }
            return;
        }

        Vec3 pos = target.position();

        for (int i = 0; i < sparks.size(); i++) {
            SparkEntity spark = sparks.get(i);
            if (!spark.isAlive()) continue;
            OrbitParams p = params.get(i);

            double angle = p.angleOffset() + tick * p.speed();
            double sx = pos.x + Math.cos(angle) * p.radius();
            double sz = pos.z + Math.sin(angle) * p.radius();
            double sy = pos.y + 0.8 + Math.sin(p.vertPhase() + tick * p.vertFreq()) * 0.55;

            spark.setPos(sx, sy, sz);
            spark.setDeltaMovement(Vec3.ZERO);
            spark.forcedVelocity = Vec3.ZERO;
        }

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS)
                .execute(() -> server.execute(() ->
                        runOrbitTicker(target, sparks, params, tick + 1, maxTicks, server)));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.LIGHTNING_BOLT;
    }
}