package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.entity.custom.TemporalMuteMarkEntity;
import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class TemporalMutePacket implements CustomPacketPayload {

    public static final Type<TemporalMutePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_mute"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalMutePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalMutePacket());

    private static final double RAY_LENGTH = 20.0;
    private static final long MUTE_DURATION_MS = 6_000L;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TemporalMutePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();

        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof TemporalGlassreaverItem)) return;

            int stage = ModDataComponents.getStage(stack);
            if (stage < 3) return;

            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().scale(RAY_LENGTH));

            ServerLevel level = (ServerLevel) player.level();
            ServerPlayer target = findTargetPlayer(player, level, start, end);

            if (target == null) {
                player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 1.5f);
                return;
            }

            MuteTracker.mute(target.getUUID(), MUTE_DURATION_MS);
            TemporalMuteActivePacket activePacket = new TemporalMuteActivePacket(target.getUUID());
            for (ServerPlayer other : level.players()) {
                ServerPlayNetworking.send(other, activePacket);
            }

            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();

            TemporalMuteMarkEntity mark = new TemporalMuteMarkEntity(ModEntities.TEMPORAL_MUTE_MARK, level, x, y, z);
            mark.setTargetUUID(target.getUUID());
            mark.setOwnerUUID(player.getUUID());
            mark.setEntityScale(1.0f);
            if (stage >= 6) {
                mark.setMovementLocked(true);
            }
            level.addFreshEntity(mark);

            spawnMuteSparks(level, x, y, z);

            level.playSound(null, x, y, z,
                    SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                    SoundSource.PLAYERS, 1.0f, 0.6f);
        });
    }

    private static void spawnMuteSparks(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 8; i++) {
            Vec3 velocity = randomSphere(0.3 + Math.random() * 0.35);

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.TEMPORAL_MARK_START);
            spark.forcedVelocity = velocity;
            spark.setDeltaMovement(velocity);
            level.addFreshEntity(spark);
        }
    }

    private static Vec3 randomSphere(double speed) {
        double theta = Math.random() * Math.PI * 2.0;
        double phi = Math.acos(2.0 * Math.random() - 1.0);
        return new Vec3(
                Math.sin(phi) * Math.cos(theta) * speed,
                Math.cos(phi) * speed,
                Math.sin(phi) * Math.sin(theta) * speed
        );
    }

    private static ServerPlayer findTargetPlayer(ServerPlayer player, ServerLevel level, Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0);
        List<ServerPlayer> candidates = level.getEntitiesOfClass(
                ServerPlayer.class, searchBox,
                e -> e != player && e.isAlive()
        );

        ServerPlayer closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ServerPlayer candidate : candidates) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = candidate;
                }
            }
        }

        return closest;
    }
}