package net.filipes.rituals.entity.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LunarMarkTracker {

    private static final int MARK_DURATION_TICKS = 120;

    private static final Map<UUID, Long> MARKS = new HashMap<>();

    public static void mark(UUID targetUUID) {
        MARKS.put(targetUUID, System.currentTimeMillis() + (MARK_DURATION_TICKS * 50L));
    }

    public static boolean isMarked(UUID targetUUID) {
        Long expiryMs = MARKS.get(targetUUID);
        if (expiryMs == null) return false;

        if (System.currentTimeMillis() > expiryMs) {
            MARKS.remove(targetUUID);
            return false;
        }

        return true;
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        MARKS.entrySet().removeIf(e -> now > e.getValue());
    }
}