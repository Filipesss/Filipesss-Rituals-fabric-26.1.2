package net.filipes.rituals.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.network.TemporalRecallStartCooldownPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class TemporalRecallTracker {

    public enum Phase { NONE, CLONE_ACTIVE, RECALLING }

    private static class RecallData {
        Phase phase     = Phase.NONE;
        UUID  cloneUUID;
        int   cloneAliveTicks = 0;
        int   recallTicks     = 0;
        int   lastSoundIndex  = -1;
    }

    private static final int CLONE_WINDOW_TICKS  = 120;
    private static final int RECALL_TICKS        = 80;
    private static final int SOUND_INTERVAL      = 20; // 1 s per step

    private static final Map<UUID, RecallData> DATA = new HashMap<>();

    public static void onClonePlaced(UUID playerUUID, UUID cloneUUID) {
        RecallData d = new RecallData();
        d.phase     = Phase.CLONE_ACTIVE;
        d.cloneUUID = cloneUUID;
        DATA.put(playerUUID, d);
    }

    public static Phase getPhase(UUID playerUUID) {
        RecallData d = DATA.get(playerUUID);
        return d == null ? Phase.NONE : d.phase;
    }

    public static void triggerEarlyRecall(UUID playerUUID, ServerLevel level) {
        RecallData d = DATA.get(playerUUID);
        if (d != null && d.phase == Phase.CLONE_ACTIVE) {
            d.phase          = Phase.RECALLING;
            d.recallTicks    = 0;
            d.lastSoundIndex = -1;
            Entity clone = level.getEntity(d.cloneUUID);
            if (clone instanceof TemporalRecallEntity recallClone) {
                recallClone.setRecalling(true);
            }
        }
    }

    public static void clear(UUID playerUUID) {
        DATA.remove(playerUUID);
    }

    public static void tick(MinecraftServer server) {
        for (UUID uuid : new HashSet<>(DATA.keySet())) {
            RecallData d = DATA.get(uuid);
            if (d == null) continue;

            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) { DATA.remove(uuid); continue; }

            ServerLevel level = (ServerLevel) player.level();
            Entity      clone = level.getEntity(d.cloneUUID);

            if (clone == null || !clone.isAlive()) {
                DATA.remove(uuid);
                continue;
            }

            if (d.phase == Phase.CLONE_ACTIVE) {
                d.cloneAliveTicks++;

                if (d.cloneAliveTicks >= CLONE_WINDOW_TICKS) {
                    d.phase          = Phase.RECALLING;
                    d.recallTicks    = 0;
                    d.lastSoundIndex = -1;
                    if (clone instanceof TemporalRecallEntity recallClone) {
                        recallClone.setRecalling(true);
                    }
                }

            } else if (d.phase == Phase.RECALLING) {

                int soundIndex = d.recallTicks / SOUND_INTERVAL;
                if (d.recallTicks % SOUND_INTERVAL == 0
                        && soundIndex != d.lastSoundIndex
                        && d.recallTicks < RECALL_TICKS) {
                    d.lastSoundIndex = soundIndex;
                    float pitch = 0.6f + soundIndex * 0.15f;
                    level.playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE,
                            SoundSource.PLAYERS, 1.0f, pitch);
                }

                d.recallTicks++;

                if (d.recallTicks >= RECALL_TICKS) {
                    Vec3 dest = clone.position();
                    TemporalRecallEntity.spawnBurstSparks(level, dest);
                    clone.discard();

                    player.teleportTo(dest.x, dest.y, dest.z);
                    level.playSound(null, dest.x, dest.y, dest.z,
                            SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.PLAYERS, 1.0f, 1.3f);

                    ServerPlayNetworking.send(player, new TemporalRecallStartCooldownPacket());
                    DATA.remove(uuid);
                }
            }
        }
    }
}