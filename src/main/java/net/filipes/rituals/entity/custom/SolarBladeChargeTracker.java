package net.filipes.rituals.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.network.SolarBladeActivePacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolarBladeChargeTracker {

    public static final int   DURATION_TICKS = 100;
    private static final float DAMAGE_BONUS  = 4.0f;
    private static final float SPEED_BONUS   = 1.0f;

    private static final int SOLAR_COLOR = 0xFFC850;

    private static final Identifier DAMAGE_MOD_ID =
            Identifier.fromNamespaceAndPath("rituals", "solar_charge_damage");
    private static final Identifier SPEED_MOD_ID  =
            Identifier.fromNamespaceAndPath("rituals", "solar_charge_speed");

    private record ChargeState(long expiry, int starEntityId) {}
    private static final Map<UUID, ChargeState> activeCharges = new HashMap<>();

    public static void activate(ServerPlayer player) {
        deactivate(player);

        Level level = player.level();
        long expiry = level.getGameTime() + DURATION_TICKS;

        SolarStarEntity star = new SolarStarEntity(ModEntities.SOLAR_STAR, level);
        star.setOwnerUUID(player.getUUID());
        star.setOwnerEntityId(player.getId());
        star.setPos(player.getX(), player.getY() + 0.02, player.getZ());
        level.addFreshEntity(star);

        activeCharges.put(player.getUUID(), new ChargeState(expiry, star.getId()));
        applyModifiers(player);

        if (level instanceof ServerLevel sl) {
            double px = player.getX();
            double py = player.getY() + 1.0;
            double pz = player.getZ();

            sl.sendParticles(ParticleTypes.ENCHANT,
                    px, py, pz,
                    30, 0.6, 0.8, 0.6, 2.5);

            sl.sendParticles(new DustParticleOptions(SOLAR_COLOR, 2.5f),
                    px, py, pz,
                    20, 0.5, 0.6, 0.5, 0.6);

            sl.sendParticles(ParticleTypes.END_ROD,
                    px, py, pz,
                    12, 0.3, 0.4, 0.3, 0.15);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.4f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f, 1.2f);

        SolarBladeActivePacket packet = new SolarBladeActivePacket(player.getUUID());
        for (ServerPlayer other : ((ServerLevel) level).players()) {
            ServerPlayNetworking.send(other, packet);
        }
    }

    private static void deactivate(ServerPlayer player) {
        ChargeState state = activeCharges.remove(player.getUUID());
        if (state == null) return;
        removeModifiers(player);
        Entity star = player.level().getEntity(state.starEntityId());
        if (star != null && star.isAlive()) star.discard();
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
            spark.applyPreset(SparkPresets.SOLAR_MARK_END);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.6f);
    }

    public static boolean isActive(UUID uuid) {
        return activeCharges.containsKey(uuid);
    }

    public static void onHit(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel sl)) return;

        ChargeState state = activeCharges.get(player.getUUID());
        if (state != null) {
            Entity starEnt = sl.getEntity(state.starEntityId());
            if (starEnt instanceof SolarStarEntity star) {
                star.triggerFlare();
            }
        }

        sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.5f, 1.8f);

        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();

        sl.sendParticles(ParticleTypes.ENCHANT,
                px, py, pz,
                16, 0.5, 0.6, 0.5, 2.2);
        sl.sendParticles(new DustParticleOptions(SOLAR_COLOR, 2f),
                px, py, pz,
                8, 0.4, 0.5, 0.4, 0.5);

        double tx = target.getX();
        double ty = target.getY() + target.getBbHeight() * 0.5;
        double tz = target.getZ();

        double hw = target.getBbWidth()  * 0.5 + 0.3;
        double hh = target.getBbHeight() * 0.5 + 0.2;

        sl.sendParticles(ParticleTypes.ENCHANT,
                tx, ty, tz,
                20, hw, hh, hw, 2.0);
        sl.sendParticles(new DustParticleOptions(SOLAR_COLOR, 2f),
                tx, ty, tz,
                10, hw, hh, hw, 0.5);
        sl.sendParticles(ParticleTypes.END_ROD,
                tx, ty, tz,
                6, hw * 0.6, hh * 0.6, hw * 0.6, 0.2);
    }

    public static void tickServer(MinecraftServer server) {
        if (activeCharges.isEmpty()) return;

        activeCharges.entrySet().removeIf(entry -> {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ChargeState state   = entry.getValue();

            if (player == null) {
                for (ServerLevel sl : server.getAllLevels()) {
                    Entity e = sl.getEntity(state.starEntityId());
                    if (e != null) { e.discard(); break; }
                }
                return true;
            }

            Level level = player.level();
            if (level.getGameTime() >= state.expiry()) {
                removeModifiers(player);
                Entity star = level.getEntity(state.starEntityId());
                double sx = star != null ? star.getX() : player.getX();
                double sy = star != null ? star.getY() : player.getY();
                double sz = star != null ? star.getZ() : player.getZ();
                shatterStar(level, state.starEntityId(), sx, sy, sz);
                return true;
            }

            if (level.getGameTime() % 3 == 0 && level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        3, 0.4, 0.5, 0.4, 1.8);

                sl.sendParticles(new DustParticleOptions(SOLAR_COLOR, 2f),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        2, 0.35, 0.45, 0.35, 0.6);
            }

            return false;
        });
    }

    private static void applyModifiers(ServerPlayer player) {
        var dmg   = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var speed = player.getAttribute(Attributes.ATTACK_SPEED);

        if (dmg != null && dmg.getModifier(DAMAGE_MOD_ID) == null)
            dmg.addTransientModifier(new AttributeModifier(DAMAGE_MOD_ID, DAMAGE_BONUS,
                    AttributeModifier.Operation.ADD_VALUE));

        if (speed != null && speed.getModifier(SPEED_MOD_ID) == null)
            speed.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, SPEED_BONUS,
                    AttributeModifier.Operation.ADD_VALUE));
    }

    public static void removeModifiers(ServerPlayer player) {
        var dmg   = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (dmg   != null) dmg.removeModifier(DAMAGE_MOD_ID);
        if (speed != null) speed.removeModifier(SPEED_MOD_ID);
    }
}