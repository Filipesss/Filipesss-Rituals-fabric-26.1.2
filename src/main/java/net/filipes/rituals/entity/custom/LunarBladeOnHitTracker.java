package net.filipes.rituals.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.network.LunarBladeActivePacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LunarBladeOnHitTracker {

    public static final int   DURATION_TICKS      = 100;
    private static final float FRAGMENT_DAMAGE_MULT = 0.5f;

    private static final int   RISING_TICKS        = 20;
    private static final float RISING_ORBIT_RADIUS = 0.6f;
    private static final float RISING_ORBIT_SPEED  = 0.22f;
    private static final float RISING_SPEED        = 0.06f;

    private record RisingState(int spark1Id, int spark2Id, int ticksLeft, float startY) {}
    private static final Map<UUID, RisingState> risingSparks = new HashMap<>();

    private record ChargeState(long expiry, int starEntityId) {}
    private static final Map<UUID, ChargeState> activeCharges = new HashMap<>();

    public static void activate(ServerPlayer player) {
        deactivate(player);

        Level level = player.level();
        long expiry = level.getGameTime() + DURATION_TICKS;

        LunarStarEntity star = new LunarStarEntity(ModEntities.LUNAR_STAR, level);
        star.setOwnerUUID(player.getUUID());
        star.setOwnerEntityId(player.getId());
        star.setPos(player.getX(), player.getY() + 0.02, player.getZ());
        level.addFreshEntity(star);

        activeCharges.put(player.getUUID(), new ChargeState(expiry, star.getId()));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.6f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f, 0.8f);

        LunarBladeActivePacket packet = new LunarBladeActivePacket(player.getUUID());
        for (ServerPlayer other : ((ServerLevel) level).players()) {
            ServerPlayNetworking.send(other, packet);
        }
    }

    private static void deactivate(ServerPlayer player) {
        ChargeState state = activeCharges.remove(player.getUUID());
        if (state == null) return;
        Entity star = player.level().getEntity(state.starEntityId());
        if (star != null && star.isAlive()) star.discard();

        RisingState rs = risingSparks.remove(player.getUUID());
        if (rs != null) {
            Entity s1 = player.level().getEntity(rs.spark1Id());
            Entity s2 = player.level().getEntity(rs.spark2Id());
            if (s1 != null && s1.isAlive()) s1.discard();
            if (s2 != null && s2.isAlive()) s2.discard();
        }
    }

    private static void shatterStar(Level level, int starEntityId,
                                    double x, double y, double z) {
        Entity star = level.getEntity(starEntityId);
        if (star != null && star.isAlive()) star.discard();

        int count = 10;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 / count) * i;
            double speed = 0.25 + Math.random() * 0.2;
            Vec3 vel = new Vec3(
                    Math.cos(angle) * speed,
                    0.05 + Math.random() * 0.1,
                    Math.sin(angle) * speed);
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.LUNAR_FRAGMENT_SINGLE);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);
    }

    public static boolean isActive(UUID uuid) {
        return activeCharges.containsKey(uuid);
    }

    public static void tickServer(MinecraftServer server) {
        if (activeCharges.isEmpty()) return;

        activeCharges.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            ChargeState state = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {

                for (ServerLevel sl : server.getAllLevels()) {
                    Entity e = sl.getEntity(state.starEntityId());
                    if (e != null) { e.discard(); break; }
                }
                RisingState rs = risingSparks.remove(uuid);
                if (rs != null) {
                    for (ServerLevel sl : server.getAllLevels()) {
                        Entity s1 = sl.getEntity(rs.spark1Id());
                        Entity s2 = sl.getEntity(rs.spark2Id());
                        if (s1 != null) s1.discard();
                        if (s2 != null) s2.discard();
                    }
                }
                return true;
            }

            Level level = player.level();
            long gameTime = level.getGameTime();

            if (gameTime >= state.expiry()) {
                Entity star = level.getEntity(state.starEntityId());
                double sx = star != null ? star.getX() : player.getX();
                double sy = star != null ? star.getY() : player.getY();
                double sz = star != null ? star.getZ() : player.getZ();
                shatterStar(level, state.starEntityId(), sx, sy, sz);

                RisingState rs = risingSparks.remove(uuid);
                if (rs != null) {
                    Entity s1 = level.getEntity(rs.spark1Id());
                    Entity s2 = level.getEntity(rs.spark2Id());
                    if (s1 != null && s1.isAlive()) s1.discard();
                    if (s2 != null && s2.isAlive()) s2.discard();
                }
                return true;
            }

            RisingState rs = risingSparks.get(uuid);
            if (rs != null) {
                if (rs.ticksLeft() <= 0) {
                    Entity s1 = level.getEntity(rs.spark1Id());
                    Entity s2 = level.getEntity(rs.spark2Id());
                    if (s1 != null && s1.isAlive()) s1.discard();
                    if (s2 != null && s2.isAlive()) s2.discard();
                    risingSparks.remove(uuid);
                } else {
                    int elapsed = RISING_TICKS - rs.ticksLeft();
                    float angle1 = elapsed * RISING_ORBIT_SPEED;
                    float angle2 = angle1 + (float) Math.PI;
                    float rise = rs.startY() + 0.1f + elapsed * RISING_SPEED;

                    Entity s1 = level.getEntity(rs.spark1Id());
                    Entity s2 = level.getEntity(rs.spark2Id());

                    if (s1 instanceof SparkEntity sp1 && sp1.isAlive()) {
                        sp1.setPos(
                                player.getX() + Math.cos(angle1) * RISING_ORBIT_RADIUS,
                                rise,
                                player.getZ() + Math.sin(angle1) * RISING_ORBIT_RADIUS);
                        sp1.setDeltaMovement(Vec3.ZERO);
                        sp1.forcedVelocity = Vec3.ZERO;
                    }
                    if (s2 instanceof SparkEntity sp2 && sp2.isAlive()) {
                        sp2.setPos(
                                player.getX() + Math.cos(angle2) * RISING_ORBIT_RADIUS,
                                rise,
                                player.getZ() + Math.sin(angle2) * RISING_ORBIT_RADIUS);
                        sp2.setDeltaMovement(Vec3.ZERO);
                        sp2.forcedVelocity = Vec3.ZERO;
                    }

                    risingSparks.put(uuid, new RisingState(
                            rs.spark1Id(), rs.spark2Id(), rs.ticksLeft() - 1, rs.startY()));
                }
            }

            return false;
        });
    }

    public static void onHit(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel sl)) return;

        ChargeState state = activeCharges.get(player.getUUID());
        if (state != null) {
            Entity starEnt = sl.getEntity(state.starEntityId());
            if (starEnt instanceof LunarStarEntity star) {
                star.triggerFlare();
            }
        }

        sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.5f, 1.6f);

        sl.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.4, 0.5, 0.4, 1.8);

        sl.sendParticles(
                new DustParticleOptions(0xC8D2FF, 2f),
                player.getX(), player.getY() + 1.0, player.getZ(),
                6, 0.35, 0.35, 0.35, 0.05);

        var random = sl.getRandom();
        double tx = target.getX();
        double ty = target.getY() + target.getBbHeight() * 0.5;
        double tz = target.getZ();

        for (int i = 0; i < 10; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
            double speed = 0.15 + random.nextDouble() * 0.25;

            double vx = Math.sin(phi) * Math.cos(theta) * speed;
            double vy = Math.cos(phi) * speed + 0.05;
            double vz = Math.sin(phi) * Math.sin(theta) * speed;

            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    tx, ty, tz, 0, vx, vy, vz, 1.0);
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetY = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;

            sl.sendParticles(net.filipes.rituals.particle.ModParticles.MOON,
                    tx + offsetX, ty + offsetY, tz + offsetZ, 0, 0.03, 0.03, 0.03, 0.01);
        }

        net.filipes.rituals.network.LunarBladeFlashPacket flashPacket =
                new net.filipes.rituals.network.LunarBladeFlashPacket(player.getUUID());
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, flashPacket);

        double spawnAngle  = Math.random() * Math.PI * 2.0;
        double spawnRadius = 0.5 + Math.random() * 0.8;
        double spawnY      = 0.6 + Math.random() * 1.2;

        LunarFragmentEntity fragment = new LunarFragmentEntity(ModEntities.LUNAR_FRAGMENT, sl);
        fragment.setOwner(player);
        fragment.setCustomSpawnPos(true);
        fragment.setPos(
                player.getX() + Math.cos(spawnAngle) * spawnRadius,
                player.getY() + spawnY,
                player.getZ() + Math.sin(spawnAngle) * spawnRadius);
        fragment.setSlot(0);
        fragment.setDamageMult(FRAGMENT_DAMAGE_MULT);
        fragment.launch(target, 0);
        sl.addFreshEntity(fragment);
    }
}