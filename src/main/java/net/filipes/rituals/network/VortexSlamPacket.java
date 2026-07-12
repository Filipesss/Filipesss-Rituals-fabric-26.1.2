package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.VortexEdgeItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record VortexSlamPacket() implements CustomPacketPayload {

    public static final Type<VortexSlamPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "vortex_slam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VortexSlamPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new VortexSlamPacket());

    private static class SlamState {
        final double startY;
        int ticksExisted = 0;

        SlamState(double startY) {
            this.startY = startY;
        }
    }

    private static final Map<UUID, SlamState> ACTIVE_SLAMS = new HashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(VortexSlamPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            if (ACTIVE_SLAMS.containsKey(player.getUUID())) return;

            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof VortexEdgeItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 6) return;

            if (!player.onGround()) {
                ACTIVE_SLAMS.put(player.getUUID(), new SlamState(player.getY()));

                player.setDeltaMovement(new Vec3(0, -2.5, 0));
                player.hurtMarked = true;

                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        });
    }

    public static void tickServerSlams(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            if (!ACTIVE_SLAMS.containsKey(uuid)) continue;

            SlamState state = ACTIVE_SLAMS.get(uuid);
            state.ticksExisted++;

            player.fallDistance = 0.0f;

            if (state.ticksExisted > 2) {
                if (player.onGround() || player.isInWater() || player.horizontalCollision) {
                    ACTIVE_SLAMS.remove(uuid);
                    double totalFallDistance = state.startY - player.getY();
                    executeImpact(player, Math.max(totalFallDistance, 2.0));
                }
            }
        }
    }

    private static void executeImpact(ServerPlayer player, double distance) {
        ServerLevel level = player.level();
        var random = level.getRandom();

        double radius = 4.0 + (distance * 0.2);
        float damage = (float) (distance * 2.7);
        double knockbackStrength = 0.8 + (distance * 0.05);

        AABB boundingBox = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, boundingBox,
                e -> e != player && e.isAlive() && player.distanceToSqr(e) <= (radius * radius));

        Vec3 playerPos = player.position();

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().explosion(player, player), damage);

            Vec3 targetPos = target.position();
            Vec3 delta = targetPos.subtract(playerPos);
            double horizontalDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

            Vec3 launchDir = horizontalDist < 0.1
                    ? new Vec3(1, 0, 0)
                    : new Vec3(delta.x / horizontalDist, 0, delta.z / horizontalDist);
            double yVelocity = 0.35 + (knockbackStrength * 0.2);
            Vec3 finalVelocity = new Vec3(
                    launchDir.x * knockbackStrength, yVelocity, launchDir.z * knockbackStrength);

            target.setDeltaMovement(finalVelocity);
            target.hurtMarked = true;

            if (target instanceof ServerPlayer targetPlayer) {
                targetPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetPlayer));
            }
        }

        level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4f, 0.6f);

        float shakeIntensity  = Math.min(13f + (float)(distance * 2.2), 34f);
        float shakeAmplitude  = Math.min(0.28f + (float)(distance * 0.028), 0.68f);
        int   shakeDuration   = (int) Math.min(10 + distance * 1.5, 22);
        level.addFreshEntity(new ScreenShakeEntity(level, playerPos,
                shakeIntensity, shakeAmplitude, shakeDuration));

        double outerSpeed = 0.48 + distance * 0.035;
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 / 8) * i;
            double speed = outerSpeed + random.nextDouble() * 0.15;
            Vec3 vel = new Vec3(
                    Math.cos(angle) * speed,
                    0.08 + random.nextDouble() * 0.12,
                    Math.sin(angle) * speed);
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                    playerPos.x, playerPos.y + 0.1, playerPos.z);
            spark.applyPreset(SparkPresets.VORTEX_SPARK_THIN);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2.0 / 6) * i + (Math.PI / 6.0);
            double speed = 0.28 + random.nextDouble() * 0.22;
            Vec3 vel = new Vec3(
                    Math.cos(angle) * speed,
                    0.35 + random.nextDouble() * 0.3,
                    Math.sin(angle) * speed);
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                    playerPos.x, playerPos.y + 0.1, playerPos.z);
            spark.applyPreset(SparkPresets.VORTEX_SPARK_TRIPLE_BLACK);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        level.sendParticles(ParticleTypes.EXPLOSION,
                playerPos.x, playerPos.y, playerPos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CLOUD,
                playerPos.x, playerPos.y + 0.1, playerPos.z,
                (int)(radius * 5), radius / 2, 0.2, radius / 2, 0.1);

        level.sendParticles(ParticleTypes.PORTAL,
                playerPos.x, playerPos.y + 0.5, playerPos.z,
                (int)(10 + distance * 2.5), 0.4, 0.35, 0.4, 0.4);

        level.sendParticles(ParticleTypes.END_ROD,
                playerPos.x, playerPos.y + 0.1, playerPos.z,
                6, 0.2, 0.1, 0.2, 0.1);
    }
}