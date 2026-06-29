package net.filipes.rituals.client.cooldown;

import java.util.LinkedHashMap;
import java.util.Map;

public class CooldownManager {

    public record AbilityDefinition(String displayName, long durationMs, int barColor) {}

    private static final Map<String, AbilityDefinition> definitions    = new LinkedHashMap<>();
    private static final Map<String, Integer>           remainingTicks = new LinkedHashMap<>();

    /**
     * Fractional drain rate per game tick.
     *   1.0f = normal  (1 tick drained per game tick)
     *   2.0f = haste   (2 ticks drained per game tick — recharges twice as fast)
     *   0.5f = slow    (1 tick drained every 2 game ticks — recharges half as fast)
     * Set by ShadeshatterHastePacket on the client; always reset to 1.0f when morph ends.
     */
    private static float tickRate        = 1.0f;
    private static float tickAccumulator = 0.0f;

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register(String id, String displayName, long durationMs, int barColor) {
        definitions.put(id, new AbilityDefinition(displayName, durationMs, barColor));
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void tick() {
        tickAccumulator += tickRate;
        int steps = (int) tickAccumulator;
        tickAccumulator -= steps;
        if (steps <= 0) return;

        final int decrement = steps;
        remainingTicks.entrySet().removeIf(e -> {
            e.setValue(e.getValue() - decrement);
            return e.getValue() <= 0;
        });
    }

    // ── Control ───────────────────────────────────────────────────────────────

    public static void trigger(String id) {
        AbilityDefinition def = definitions.get(id);
        if (def == null) throw new IllegalArgumentException("Unknown ability id: " + id);
        remainingTicks.put(id, msToTicks(def.durationMs()));
    }

    /** Instantly expires all tracked cooldowns (called by RAPID_RESET). */
    public static void clearAll() {
        remainingTicks.clear();
        tickAccumulator = 0.0f;
    }


    public static void setTickRate(float rate) {
        tickRate        = rate;
        tickAccumulator = 0.0f;   // reset accumulator so the new rate takes clean effect
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public static boolean isOnCooldown(String id) {
        Integer t = remainingTicks.get(id);
        return t != null && t > 0;
    }

    public static float getProgress(String id) {
        Integer remaining = remainingTicks.get(id);
        if (remaining == null || remaining <= 0) return 1.0f;
        AbilityDefinition def = definitions.get(id);
        if (def == null) return 1.0f;
        return 1.0f - (float) remaining / msToTicks(def.durationMs());
    }

    public static long getRemainingMs(String id) {
        Integer t = remainingTicks.get(id);
        if (t == null || t <= 0) return 0;
        return (long) t * 50;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int msToTicks(long ms) { return (int)(ms / 50); }

    public static Map<String, AbilityDefinition> getDefinitions() { return definitions; }
}