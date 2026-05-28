package net.filipes.rituals.network;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.CinderboltItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CinderboltDeathSaveHandler {

    public static final long  COOLDOWN_MS       = 1_200_000L; // 1200s
    public static final long  IMMUNITY_DURATION_MS = 5_000L;  // 5s

    private static final Map<UUID, Long> lastSave       = new HashMap<>();
    private static final Map<UUID, Long> immunityExpiry = new HashMap<>();
    private static final Set<UUID>       processing     = new HashSet<>();

    public static void register() {

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;

            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof CinderboltItem)) return true;
            if (ModDataComponents.getStage(held) < 7) return true;

            UUID uuid = player.getUUID();

            if (isImmune(uuid)) return false;

            if (player.getHealth() - amount <= 0f) {
                long now = System.currentTimeMillis();
                Long last = lastSave.get(uuid);
                if (last != null && now - last < COOLDOWN_MS) return true;

                lastSave.put(uuid, now);
                immunityExpiry.put(uuid, now + IMMUNITY_DURATION_MS);
                CinderboltSaveTriggeredPacket.send(player);
                ServerLevel sv = (ServerLevel) player.level();
                net.minecraft.world.phys.Vec3 pos = player.position();

                net.filipes.rituals.entity.custom.ScreenShakeEntity shake =
                        new net.filipes.rituals.entity.custom.ScreenShakeEntity(
                                sv, pos, 24f, 0.6f, 20);
                sv.addFreshEntity(shake);
                double speed = 0.6;
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI / 4.0 * i;
                    net.filipes.rituals.entity.custom.SparkEntity spark =
                            new net.filipes.rituals.entity.custom.SparkEntity(
                                    net.filipes.rituals.entity.ModEntities.SPARK, sv,
                                    pos.x, pos.y + 1.0, pos.z);
                    spark.applyPreset(SparkPresets.CINDERBOLT_SHIELD_SPARK);
                    spark.forcedVelocity = new net.minecraft.world.phys.Vec3(
                            Math.cos(angle) * speed, 0.4, Math.sin(angle) * speed);
                    sv.addFreshEntity(spark);
                }
                double radius = 6.0;
                net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                        pos.x - radius, pos.y - 2, pos.z - radius,
                        pos.x + radius, pos.y + 4, pos.z + radius);

                for (net.minecraft.world.entity.LivingEntity target :
                        sv.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
                    if (target == player) continue;

                    net.minecraft.world.phys.Vec3 diff = target.position().subtract(pos);
                    double dist = diff.length();
                    if (dist < 0.1) continue;

                    double strength = 2.4 * (1.0 - dist / radius);
                    net.minecraft.world.phys.Vec3 impulse = diff.normalize().scale(strength).add(0, 0.5, 0);
                    target.setDeltaMovement(target.getDeltaMovement().add(impulse));

                    net.filipes.rituals.entity.custom.SparkEntity spark =
                            new net.filipes.rituals.entity.custom.SparkEntity(
                                    net.filipes.rituals.entity.ModEntities.SPARK, sv,
                                    pos.x, pos.y + 1.0, pos.z);
                    spark.applyPreset(net.filipes.rituals.entity.custom.SparkPresets.CINDERBOLT_SHIELD_TRAIL);
                    spark.forcedVelocity = diff.normalize().scale(1.5).add(0, 0.1, 0);
                    sv.addFreshEntity(spark);
                }

                if (!processing.contains(uuid)) {
                    processing.add(uuid);
                    player.setHealth(1.0f);
                    processing.remove(uuid);
                }

                return false; // cancel the killing blow
            }

            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            immunityExpiry.entrySet().removeIf(e -> now >= e.getValue());
        });
    }

    public static boolean isImmune(UUID uuid) {
        Long exp = immunityExpiry.get(uuid);
        return exp != null && System.currentTimeMillis() < exp;
    }

    public static boolean isOnCooldown(UUID uuid) {
        Long last = lastSave.get(uuid);
        return last != null && System.currentTimeMillis() - last < COOLDOWN_MS;
    }

    public static long getImmunityExpiryMs(UUID uuid) {
        return immunityExpiry.getOrDefault(uuid, -1L);
    }
}