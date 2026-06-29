package net.filipes.rituals.item.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightningRapierStreakTracker {

    private static final int STREAK_TIMEOUT_TICKS = 80; // 4 seconds

    private record StreakEntry(UUID targetId, int count, long lastTick) {}

    private static final Map<UUID, StreakEntry> STREAKS = new HashMap<>();

    public static int onHit(UUID playerId, UUID targetId, long gameTick) {
        StreakEntry entry = STREAKS.get(playerId);
        int newCount;

        if (entry != null
                && entry.targetId().equals(targetId)
                && gameTick - entry.lastTick() <= STREAK_TIMEOUT_TICKS) {
            newCount = Math.min(entry.count() + 1, 6);
        } else {
            newCount = 1;
        }

        STREAKS.put(playerId, new StreakEntry(targetId, newCount, gameTick));
        return newCount;
    }

    public static void reset(UUID playerId) {
        STREAKS.remove(playerId);
    }

    public static void tick(net.minecraft.server.MinecraftServer server) {
        long currentTick = server.overworld().getGameTime();
        STREAKS.entrySet().removeIf(e ->
                currentTick - e.getValue().lastTick() > STREAK_TIMEOUT_TICKS);
    }
}