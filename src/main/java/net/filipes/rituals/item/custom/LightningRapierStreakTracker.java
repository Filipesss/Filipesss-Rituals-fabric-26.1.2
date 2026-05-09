package net.filipes.rituals.item.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side only. Tracks consecutive hits on the same target per player.
 * Streak resets if:
 *   - The player hits a different entity
 *   - More than STREAK_TIMEOUT_TICKS pass since the last hit
 * Tick cleanup is registered in Rituals.java via ServerTickEvents.
 */
public class LightningRapierStreakTracker {

    private static final int STREAK_TIMEOUT_TICKS = 80; // 4 seconds

    private record StreakEntry(UUID targetId, int count, long lastTick) {}

    private static final Map<UUID, StreakEntry> STREAKS = new HashMap<>();

    /**
     * Called on every successful hit.
     * @return the new streak count for this player (1-6, capped)
     */
    public static int onHit(UUID playerId, UUID targetId, long gameTick) {
        StreakEntry entry = STREAKS.get(playerId);
        int newCount;

        if (entry != null
                && entry.targetId().equals(targetId)
                && gameTick - entry.lastTick() <= STREAK_TIMEOUT_TICKS) {
            newCount = Math.min(entry.count() + 1, 6);
        } else {
            // Different target or timed out — restart streak
            newCount = 1;
        }

        STREAKS.put(playerId, new StreakEntry(targetId, newCount, gameTick));
        return newCount;
    }

    /**
     * Forcibly clear the streak for a player (called after supercharge triggers).
     */
    public static void reset(UUID playerId) {
        STREAKS.remove(playerId);
    }

    /**
     * Called each server tick to evict stale entries.
     * Register in Rituals.java: ServerTickEvents.END_SERVER_TICK.register(...)
     */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        long currentTick = server.overworld().getGameTime();
        STREAKS.entrySet().removeIf(e ->
                currentTick - e.getValue().lastTick() > STREAK_TIMEOUT_TICKS);
    }
}