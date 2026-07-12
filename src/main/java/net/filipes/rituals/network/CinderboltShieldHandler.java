package net.filipes.rituals.network;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.CinderboltShieldEntity;
import net.filipes.rituals.item.custom.CinderboltItem;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CinderboltShieldHandler {

    private static final Map<UUID, Integer> shieldEntityIds = new HashMap<>();
    private static final Set<UUID>          processingDamage = new HashSet<>();

    public static void register() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                boolean active = isShieldActive(player);
                UUID uuid = player.getUUID();

                if (active) {
                    Integer existingId = shieldEntityIds.get(uuid);
                    ServerLevel level = (ServerLevel) player.level();

                    boolean needsSpawn = true;
                    if (existingId != null) {
                        Entity existing = level.getEntity(existingId);
                        if (existing instanceof CinderboltShieldEntity shield && shield.isAlive()) {
                            shield.setPos(player.getX(), player.getY(), player.getZ());
                            needsSpawn = false;
                        }
                    }

                    if (needsSpawn) {
                        CinderboltShieldEntity shield = new CinderboltShieldEntity(
                                ModEntities.CINDERBOLT_SHIELD, level, player);
                        level.addFreshEntity(shield);
                        shieldEntityIds.put(uuid, shield.getId());
                        level.playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                ModSounds.CINDER_SHIELD_EQUIP,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.4f,
                                1.0f
                        );

                        double radius = 2.0;
                        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                                player.getX() - radius, player.getY() - 2, player.getZ() - radius,
                                player.getX() + radius, player.getY() + 4, player.getZ() + radius);

                        for (net.minecraft.world.entity.LivingEntity target :
                                level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
                            if (target == player) continue;

                            net.minecraft.world.phys.Vec3 diff = target.position().subtract(player.position());
                            double dist = diff.length();
                            if (dist < 0.1) continue;

                            double strength = 0.9 * (1.0 - dist / radius);
                            net.minecraft.world.phys.Vec3 impulse = diff.normalize().scale(strength).add(0, 0.4, 0);
                            target.setDeltaMovement(target.getDeltaMovement().add(impulse));

                            if (target instanceof ServerPlayer targetPlayer) {
                                targetPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(
                                        targetPlayer.getId(), targetPlayer.getDeltaMovement()));
                            }

                            net.filipes.rituals.entity.custom.SparkEntity spark =
                                    new net.filipes.rituals.entity.custom.SparkEntity(
                                            net.filipes.rituals.entity.ModEntities.SPARK, level,
                                            player.getX(), player.getY() + 1.0, player.getZ());
                            spark.applyPreset(net.filipes.rituals.entity.custom.SparkPresets.CINDERBOLT_SHIELD_TRAIL);
                            spark.forcedVelocity = diff.normalize().scale(1.2).add(0, 0.1, 0);
                            level.addFreshEntity(spark);
                        }
                    }
                } else {
                    removeShield(player);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            removeShield(handler.player);
            shieldEntityIds.remove(handler.player.getUUID());
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;

            UUID uuid = player.getUUID();
            if (CinderboltDeathSaveHandler.isImmune(uuid)) return false;
            if (!isShieldActive(player)) return true;

            if (processingDamage.contains(uuid)) return true;

            processingDamage.add(uuid);
            player.hurtServer((ServerLevel) player.level(), source, amount * 0.3f);
            processingDamage.remove(uuid);

            return false;
        });
    }
    public static void applySpinBoost(ServerPlayer player, int durationTicks) {
        Integer id = shieldEntityIds.get(player.getUUID());
        if (id == null) return;
        Entity e = ((ServerLevel) player.level()).getEntity(id);
        if (e instanceof CinderboltShieldEntity shield) {
            shield.applySpinBoost(durationTicks);
        }
    }

    private static boolean isShieldActive(ServerPlayer player) {
        var held = player.getMainHandItem();
        return held.getItem() instanceof CinderboltItem
                && ModDataComponents.getStage(held) >= 6;
    }

    private static void removeShield(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Integer id = shieldEntityIds.remove(uuid);
        if (id != null) {
            Entity e = ((ServerLevel) player.level()).getEntity(id);
            if (e instanceof CinderboltShieldEntity shield) shield.startContracting();
            else if (e != null) e.discard();
        }
    }
}