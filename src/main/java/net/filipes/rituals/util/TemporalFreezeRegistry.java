package net.filipes.rituals.util;

import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class TemporalFreezeRegistry {
    private TemporalFreezeRegistry() {}

    public static final double SLOW_FACTOR            = 1.0 / 3.0;
    public static final double PROJECTILE_SLOW_FACTOR = 1.0 / 8.0;

    private static Set<Integer> slowedThisTick            = Collections.emptySet();
    private static Set<Integer> slowedProjectilesThisTick = Collections.emptySet();
    private static final Map<Integer, Vec3> PRE_SLOW_VELOCITY = new HashMap<>();
    private static final Map<Integer, Integer> STORED_TNT_FUSE   = new HashMap<>();
    private static final Map<Integer, Integer> TNT_TICK_COUNTER  = new HashMap<>();


    public static void setSlowedThisTick(Set<Integer> ids) {
        slowedThisTick = ids.isEmpty() ? Collections.emptySet() : new HashSet<>(ids);
    }

    public static void setSlowedProjectilesThisTick(Set<Integer> ids) {
        slowedProjectilesThisTick = ids.isEmpty() ? Collections.emptySet() : new HashSet<>(ids);
    }

    public static boolean isSlowed(int id)           { return slowedThisTick.contains(id); }
    public static boolean isSlowedProjectile(int id) { return slowedProjectilesThisTick.contains(id); }

    public static void storePreVelocity(int id, Vec3 v) { PRE_SLOW_VELOCITY.put(id, v); }
    public static Vec3 takePreVelocity(int id)           { return PRE_SLOW_VELOCITY.remove(id); }

    /**
     * slowFactor is passed explicitly so arrows (1/8) and entities (1/3) share the same math.
     * output = v0 + (v1 - v0*sf) * sf  →  constant forces (gravity) scale by sf, not sf².
     */
    public static Vec3 computeNextTrueVelocity(Vec3 v0, Vec3 v1, double slowFactor) {
        Vec3 delta = v1.subtract(v0.scale(slowFactor));
        return v0.add(delta.scale(slowFactor));
    }
    public static void storeTntFuse(int id, int fuse) { STORED_TNT_FUSE.put(id, fuse); }

    public static int takeTntFuse(int id) {
        Integer v = STORED_TNT_FUSE.remove(id);
        return v != null ? v : -1;
    }

    /** Returns true only every 3rd call — fuse ticks down once per 3 game ticks. */
    public static boolean shouldTntDecrement(int id) {
        int count = TNT_TICK_COUNTER.merge(id, 1, Integer::sum);
        if (count >= 3) { TNT_TICK_COUNTER.put(id, 0); return true; }
        return false;
    }

    public static void clearTntCounter(int id) {
        TNT_TICK_COUNTER.remove(id);
        STORED_TNT_FUSE.remove(id);
    }
}