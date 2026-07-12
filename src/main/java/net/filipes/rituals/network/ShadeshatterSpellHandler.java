package net.filipes.rituals.network;

import net.filipes.rituals.entity.custom.ShadeshatterSpellEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ShadeshatterSpellHandler {

    private record PendingSpawn(UUID playerUuid, int fireAtTick) {}
    private record PendingSound(UUID playerUuid, int playAtTick) {}

    private static final List<PendingSpawn> PENDING = new ArrayList<>();
    private static final List<PendingSound> PENDING_SOUNDS = new ArrayList<>();

    public static void schedule(UUID playerUuid, int fireAtTick) {
        PENDING.add(new PendingSpawn(playerUuid, fireAtTick));
    }

    public static void scheduleSecondCharge(UUID playerUuid, int playAtTick) {
        PENDING_SOUNDS.add(new PendingSound(playerUuid, playAtTick));
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTickCount();

        if (!PENDING_SOUNDS.isEmpty()) {
            Iterator<PendingSound> soundIt = PENDING_SOUNDS.iterator();
            while (soundIt.hasNext()) {
                PendingSound sound = soundIt.next();
                if (now < sound.playAtTick()) continue;

                soundIt.remove();

                ServerPlayer player = server.getPlayerList().getPlayer(sound.playerUuid());
                if (player == null || !player.isAlive()) continue;

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BREEZE_CHARGE, SoundSource.PLAYERS, 1.0f, 1.3f + player.getRandom().nextFloat() * 0.1f);
            }
        }

        if (!PENDING.isEmpty()) {
            Iterator<PendingSpawn> it = PENDING.iterator();
            while (it.hasNext()) {
                PendingSpawn spawn = it.next();
                if (now < spawn.fireAtTick()) continue;

                it.remove();

                ServerPlayer player = server.getPlayerList().getPlayer(spawn.playerUuid());
                if (player == null || !player.isAlive()) continue;

                ServerLevel level = player.level();
                level.addFreshEntity(new ShadeshatterSpellEntity(level, player));

                level.playSound(null,
                        player.getX(), player.getEyeY(), player.getZ(),
                        SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS,
                        0.8f, 0.85f + player.getRandom().nextFloat() * 0.2f);
            }
        }
    }

    public static void onPlayerDisconnect(UUID uuid) {
        PENDING.removeIf(s -> s.playerUuid().equals(uuid));
        PENDING_SOUNDS.removeIf(s -> s.playerUuid().equals(uuid));
    }
}