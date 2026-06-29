package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.BlightedPuddleEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlightDashPacket implements CustomPacketPayload {

    public static final Type<BlightDashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_dash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightDashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new BlightDashPacket());

    private static class DashState {
        int count = 0;
        long lastDashTime = 0;
    }

    private static final Map<UUID, DashState> PLAYER_STATES = new HashMap<>();
    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();

    public static final long COOLDOWN_MS = 15_000L;
    public static final long WINDOW_MS = 10_000L;
    public static final float DASH_DISTANCE = 7.0f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightDashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof BlightspearItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 3) return;

            UUID uuid = player.getUUID();
            long now = System.currentTimeMillis();

            DashState state = PLAYER_STATES.computeIfAbsent(uuid, k -> new DashState());

            if (state.count == 1) {
                long elapsed = now - state.lastDashTime;
                if (elapsed > WINDOW_MS) {
                    long cooldownEndTime = state.lastDashTime + WINDOW_MS + COOLDOWN_MS;
                    if (now < cooldownEndTime) return;
                    else state.count = 0;
                }
            }

            Long lastGlobal = SERVER_COOLDOWNS.get(uuid);
            if (lastGlobal != null && now - lastGlobal < COOLDOWN_MS) return;

            Vec3 look = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 dashDir;

            if (state.count == 0) {
                dashDir = look.scale(-1);
                state.count = 1;
                state.lastDashTime = now;
            } else {
                dashDir = look;
                state.count = 0;
                SERVER_COOLDOWNS.put(uuid, now);
            }

            Vec3 start = player.position();
            Vec3 end = start.add(dashDir.scale(DASH_DISTANCE));
            ServerLevel level = player.level();

            AABB baseBox = player.getBoundingBox();
            Vec3 actualEnd = start;
            double stepSize = 0.25;
            double maxDist = start.distanceTo(end);

            for (double d = stepSize; d <= maxDist; d += stepSize) {
                Vec3 nextPos = start.add(dashDir.scale(d));
                AABB checkBox = baseBox.move(nextPos.subtract(start));
                if (level.getBlockCollisions(player, checkBox).iterator().hasNext()) {
                    break;
                }
                actualEnd = nextPos;
            }

            double actualDist = start.distanceTo(actualEnd);

            for (int i = 0; i < 2; i++) {
                double speed = 1.6 + (i * 0.15);
                SparkEntity trailSpark = new SparkEntity(ModEntities.SPARK, level, start.x, start.y + 0.8, start.z);
                trailSpark.applyPreset(SparkPresets.BLIGHT_TRIPLE);
                trailSpark.forcedVelocity = dashDir.scale(speed);
                trailSpark.setNoGravity(true);
                trailSpark.maxLifetime = (int) Math.ceil(actualDist / speed) + 1;
                level.addFreshEntity(trailSpark);
            }

            int puddleCount = 4;
            for (int i = 0; i <= puddleCount; i++) {
                double pct = (double) i / puddleCount;
                Vec3 puddlePos = start.lerp(actualEnd, pct);

                BlightedPuddleEntity puddle = new BlightedPuddleEntity(ModEntities.BLIGHTED_PUDDLE, level); 
                puddle = new BlightedPuddleEntity(ModEntities.BLIGHTED_PUDDLE, level);
                puddle.setPos(puddlePos.x, puddlePos.y, puddlePos.z);
                puddle.setOwnerUUID(uuid);
                level.addFreshEntity(puddle);

                SparkEntity puddleSpark = new SparkEntity(ModEntities.SPARK, level, puddlePos.x, puddlePos.y + 0.1, puddlePos.z);
                puddleSpark.applyPreset(SparkPresets.BLIGHT_SINGLE);
                puddleSpark.forcedVelocity = new Vec3(
                        (level.getRandom().nextDouble() - 0.5) * 0.08,
                        0.18 + level.getRandom().nextDouble() * 0.1,
                        (level.getRandom().nextDouble() - 0.5) * 0.08
                );
                level.addFreshEntity(puddleSpark);
            }

            for (int i = 0; i < 6; i++) {
                double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
                double horizSpd = 0.15 + level.getRandom().nextDouble() * 0.2;
                double vertSpd = 0.10 + level.getRandom().nextDouble() * 0.3;

                SparkEntity burstSpark = new SparkEntity(ModEntities.SPARK, level, actualEnd.x, actualEnd.y + 0.4, actualEnd.z);
                burstSpark.applyPreset(SparkPresets.BLIGHT_SINGLE);
                burstSpark.forcedVelocity = new Vec3(
                        Math.cos(angle) * horizSpd,
                        vertSpd,
                        Math.sin(angle) * horizSpd
                );
                level.addFreshEntity(burstSpark);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), SoundSource.PLAYERS, 0.4f, 1.8f);

            player.setDeltaMovement(dashDir.scale(1.6));
            player.hurtMarked = true;
        });
    }
}