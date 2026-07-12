package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ShadeshatterWormholeHandler {

    public static final long WORMHOLE_TICKS = 40L;

    private static final double PULL_RADIUS   = 8.0;
    private static final double PULL_STRENGTH = 0.12;

    private static final int    ORBIT_COUNT         = 2;
    private static final double TORNADO_MIN_RADIUS  = 0.25;
    private static final double TORNADO_MAX_RADIUS  = 1.4;
    private static final double TORNADO_MAX_HEIGHT  = 1.8;
    private static final double TORNADO_ORBIT_SPEED = 0.35;

    private static final double INWARD_DISCARD_DIST_SQ = 0.5 * 0.5;


    private static class WormholeEntry {
        final ServerPlayer      player;
        final Vec3              center;
        final long              expiryTick;
        final List<SparkEntity> orbitSparks  = new ArrayList<>();
        final List<SparkEntity> inwardSparks = new ArrayList<>();

        WormholeEntry(ServerPlayer player, Vec3 center, long expiryTick) {
            this.player     = player;
            this.center     = center;
            this.expiryTick = expiryTick;
        }
    }

    private static final Map<UUID, WormholeEntry> ACTIVE = new HashMap<>();


    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            WormholeEntry e = ACTIVE.remove(handler.player.getUUID());
            if (e != null) discardAllSparks(e);
        });
    }

    public static void beginWormhole(ServerPlayer player, Vec3 center, long durationTicks) {
        long expiry = player.level().getGameTime() + durationTicks;
        WormholeEntry entry = new WormholeEntry(player, center, expiry);
        ACTIVE.put(player.getUUID(), entry);

        ServerLevel level = player.level();
        MinecraftServer server = player.level().getServer();

        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.4f);

        for (int i = 0; i < ORBIT_COUNT; i++) {
            double startAngle = (2.0 * Math.PI / ORBIT_COUNT) * i;
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                    center.x + Math.cos(startAngle) * TORNADO_MIN_RADIUS,
                    center.y,
                    center.z + Math.sin(startAngle) * TORNADO_MIN_RADIUS);
            spark.applyPreset(SparkPresets.SHADESHATTER_WORMHOLE_ORBIT);
            spark.setNoGravity(true);
            spark.setDeltaMovement(Vec3.ZERO);
            spark.forcedVelocity = Vec3.ZERO;
            level.addFreshEntity(spark);
            entry.orbitSparks.add(spark);
            runTornadoTicker(center, spark, startAngle, 0, server);
        }
    }

    public static boolean isActive(UUID uuid) {
        return ACTIVE.containsKey(uuid);
    }

    public static void tick(MinecraftServer server) {
        ACTIVE.entrySet().removeIf(entry -> {
            WormholeEntry w = entry.getValue();
            ServerPlayer player = w.player;

            if (player.isRemoved()) {
                discardAllSparks(w);
                return true;
            }

            ServerLevel level = player.level();
            long gameTime = level.getGameTime();
            long ticksAlive = WORMHOLE_TICKS - (w.expiryTick - gameTime);

            level.sendParticles(ParticleTypes.PORTAL,
                    w.center.x, w.center.y, w.center.z, 8, 0.3, 0.3, 0.3, 0.1);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    w.center.x, w.center.y + 0.5, w.center.z, 2, 0.4, 0.4, 0.4, 0.04);

            if (ticksAlive % 2 == 0) {
                spawnInwardSparks(level, w);
            }

            w.inwardSparks.removeIf(s -> {
                if (!s.isAlive()) return true;
                if (s.position().distanceToSqr(w.center) < INWARD_DISCARD_DIST_SQ) {
                    s.discard();
                    return true;
                }
                return false;
            });

            AABB box = AABB.ofSize(w.center, PULL_RADIUS * 2, PULL_RADIUS * 2, PULL_RADIUS * 2);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> !e.getUUID().equals(player.getUUID()));
            for (LivingEntity entity : nearby) {
                Vec3 dir = w.center.subtract(entity.position());
                double dist = dir.length();
                if (dist > 0 && dist < PULL_RADIUS) {
                    double strength = PULL_STRENGTH * (1.0 - dist / PULL_RADIUS);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(dir.normalize().scale(strength)));

                    syncPlayerVelocity(entity);
                }
            }

            if (gameTime >= w.expiryTick) {
                discardAllSparks(w);

                for (LivingEntity entity : nearby) {
                    Vec3 outDir = entity.position().subtract(w.center);
                    Vec3 burst = outDir.lengthSqr() > 0.01
                            ? outDir.normalize().scale(1.2)
                            : new Vec3(level.getRandom().nextDouble() - 0.5, 0.3,
                            level.getRandom().nextDouble() - 0.5).normalize().scale(1.2);
                    entity.setDeltaMovement(burst);
                    syncPlayerVelocity(entity);
                }

                spawnOutburstSparks(level, w.center, 16);
                level.sendParticles(ParticleTypes.PORTAL,
                        w.center.x, w.center.y, w.center.z, 40, 0.1, 0.1, 0.1, 0.5);

                player.teleportTo(level,
                        w.center.x, w.center.y, w.center.z,
                        Set.of(), player.getYRot(), player.getXRot(), true);
                level.playSound(null, w.center.x, w.center.y, w.center.z,
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }

            return false;
        });
    }

    private static void syncPlayerVelocity(LivingEntity entity) {
        if (entity instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
        }
    }


    private static void runTornadoTicker(Vec3 center, SparkEntity spark,
                                         double startAngle, int tick, MinecraftServer server) {
        if (!spark.isAlive()) return;

        int cycleTick = tick % (int) WORMHOLE_TICKS;
        double progress = (double) cycleTick / WORMHOLE_TICKS;
        double radius = TORNADO_MIN_RADIUS + progress * (TORNADO_MAX_RADIUS - TORNADO_MIN_RADIUS);
        double height = progress * TORNADO_MAX_HEIGHT;
        double angle  = startAngle + cycleTick * TORNADO_ORBIT_SPEED;

        spark.setPos(
                center.x + Math.cos(angle) * radius,
                center.y + height,
                center.z + Math.sin(angle) * radius
        );
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() ->
                server.execute(() -> runTornadoTicker(center, spark, startAngle, tick + 1, server))
        );
    }


    private static void spawnInwardSparks(ServerLevel level, WormholeEntry w) {
        for (int i = 0; i < 2; i++) {
            double angle  = level.getRandom().nextDouble() * 2 * Math.PI;
            double dist   = 1.0 + level.getRandom().nextDouble() * 1.5; // 1.0–2.5 blocks
            double spawnY = w.center.y + level.getRandom().nextDouble() * TORNADO_MAX_HEIGHT;
            double spawnX = w.center.x + Math.cos(angle) * dist;
            double spawnZ = w.center.z + Math.sin(angle) * dist;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
            spark.applyPreset(SparkPresets.SHADESHATTER_WORMHOLE_SIDE);

            Vec3 dir = new Vec3(w.center.x - spawnX, (w.center.y + 0.5) - spawnY, w.center.z - spawnZ).normalize();
            spark.forcedVelocity = dir.scale(dist / 18.0);

            level.addFreshEntity(spark);
            w.inwardSparks.add(spark);
        }
    }


    private static void spawnOutburstSparks(ServerLevel level, Vec3 center, int count) {
        for (int i = 0; i < count; i++) {
            double yaw   = level.getRandom().nextDouble() * 2 * Math.PI;
            double pitch = (level.getRandom().nextDouble() - 0.5) * Math.PI;
            double speed = 0.3 + level.getRandom().nextDouble() * 0.5;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                    center.x, center.y + 0.5, center.z);
            spark.applyPreset(SparkPresets.SHADESHATTER_WORMHOLE_ORBIT);
            spark.forcedVelocity = new Vec3(
                    Math.cos(pitch) * Math.cos(yaw) * speed,
                    Math.sin(pitch) * speed,
                    Math.cos(pitch) * Math.sin(yaw) * speed
            );
            level.addFreshEntity(spark);
        }
    }


    private static void discardAllSparks(WormholeEntry e) {
        e.orbitSparks.forEach(s  -> { if (s.isAlive())  s.discard(); });
        e.inwardSparks.forEach(s -> { if (s.isAlive()) s.discard(); });
    }
}