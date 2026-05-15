package net.filipes.rituals.entity.custom;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifestealMarkTracker {

    private static final int MARK_DURATION_TICKS = 120; // 6 seconds

    private static final Map<UUID, MarkedEntry> MARKS = new HashMap<>();

    private record MarkedEntry(UUID markerUUID, long expiryMs) {}

    public static void mark(UUID targetUUID, UUID markerUUID) {
        MARKS.put(targetUUID, new MarkedEntry(
                markerUUID,
                System.currentTimeMillis() + (MARK_DURATION_TICKS * 50L)
        ));
    }

    public static boolean isMarkedBy(UUID targetUUID, UUID markerUUID) {
        MarkedEntry entry = MARKS.get(targetUUID);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiryMs()) {
            MARKS.remove(targetUUID);
            return false;
        }
        return entry.markerUUID().equals(markerUUID);
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        MARKS.entrySet().removeIf(e -> now > e.getValue().expiryMs());
    }
}