package net.filipes.rituals.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteTracker {

    private static final Map<UUID, Long> MUTED_UNTIL = new ConcurrentHashMap<>();

    public static void mute(UUID uuid, long durationMs) {
        MUTED_UNTIL.put(uuid, System.currentTimeMillis() + durationMs);
    }

    public static boolean isMuted(UUID uuid) {
        Long until = MUTED_UNTIL.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            MUTED_UNTIL.remove(uuid);
            return false;
        }
        return true;
    }

    public static void clear(UUID uuid) {
        MUTED_UNTIL.remove(uuid);
    }
}