package net.filipes.rituals.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks Shadeshatter's transient (event-triggered) animation state PER ENTITY,
 * instead of the old static fields which only ever described the local player.
 *
 * Idle looping frames (the default 1-4 cycle) are NOT tracked here — they're
 * purely a function of client tick count, so every client computes them
 * identically without any sync. This class only holds state for animations
 * that start on an action: mimic, spell, wormhole.
 */
public class ShadeshatterAnimTracker {

    private static final int TICKS_PER_FRAME        = 3;
    private static final int MIMIC_TICKS_PER_FRAME   = 3;
    private static final int SPELL_TICKS_PER_FRAME   = 3;
    private static final int WORMHOLE_TICKS_PER_FRAME = 3;

    private static final class State {
        int mimicFrame     = -1;
        int mimicTicker    = 0;
        int spellFrame     = -1;
        int spellTicker    = 0;
        int wormholeFrame  = -1;
        int wormholeTicker = 0;
        boolean mimicJustCompleted    = false;
        boolean wormholeJustCompleted = false;
    }

    private static final Map<Integer, State> STATES = new HashMap<>();

    private static State stateFor(int entityId) {
        return STATES.computeIfAbsent(entityId, id -> new State());
    }

    // ── Start triggers (called when a sync packet arrives for any entity id) ──

    public static void startMimic(int entityId) {
        State s = stateFor(entityId);
        s.mimicFrame  = 0;
        s.mimicTicker = 0;
    }

    public static void startSpell(int entityId) {
        State s = stateFor(entityId);
        s.spellFrame  = 0;
        s.spellTicker = 0;
    }

    public static void startWormhole(int entityId) {
        State s = stateFor(entityId);
        s.wormholeFrame  = 0;
        s.wormholeTicker = 0;
    }

    // ── Tick — advances every tracked entity's animation state ────────────────

    public static void tick() {
        for (State s : STATES.values()) {
            if (s.mimicFrame >= 0) {
                s.mimicTicker++;
                if (s.mimicTicker >= MIMIC_TICKS_PER_FRAME) {
                    s.mimicTicker = 0;
                    s.mimicFrame++;
                    if (s.mimicFrame >= 14) {
                        s.mimicFrame = -1;
                        s.mimicJustCompleted = true;
                    }
                }
            }
            if (s.spellFrame >= 0) {
                s.spellTicker++;
                if (s.spellTicker >= SPELL_TICKS_PER_FRAME) {
                    s.spellTicker = 0;
                    s.spellFrame++;
                    if (s.spellFrame >= 14) s.spellFrame = -1;
                }
            }
            if (s.wormholeFrame >= 0) {
                s.wormholeTicker++;
                if (s.wormholeTicker >= WORMHOLE_TICKS_PER_FRAME) {
                    s.wormholeTicker = 0;
                    s.wormholeFrame++;
                    if (s.wormholeFrame >= 14) {
                        s.wormholeFrame = -1;
                        s.wormholeJustCompleted = true;
                    }
                }
            }
        }
    }

    /**
     * Returns true exactly once — the first poll after this entity's mimic
     * animation finished — then clears the flag. Used by RitualsClient to
     * know when to actually send ShadeshatterMorphPacket for the local player.
     */
    public static boolean consumeMimicCompletion(int entityId) {
        State s = STATES.get(entityId);
        if (s == null || !s.mimicJustCompleted) return false;
        s.mimicJustCompleted = false;
        return true;
    }

    /** Same idea as consumeMimicCompletion, for the wormhole animation. */
    public static boolean consumeWormholeCompletion(int entityId) {
        State s = STATES.get(entityId);
        if (s == null || !s.wormholeJustCompleted) return false;
        s.wormholeJustCompleted = false;
        return true;
    }

    /** Call periodically (e.g. every few seconds) to drop entries for entities no longer relevant. */
    public static void forgetIdle() {
        STATES.values().removeIf(s -> s.mimicFrame < 0 && s.spellFrame < 0 && s.wormholeFrame < 0);
    }

    /**
     * Computes the CustomModelData frame value for the given entity's held
     * Shadeshatter, using the same idle-cycle math as before (now derived
     * purely from `globalTick`, so it's identical across all clients) plus
     * this entity's own transient animation state if one is active.
     */
    public static float computeFrame(int entityId, long globalTick) {
        State s = STATES.get(entityId);

        if (s != null && s.mimicFrame >= 0)     return s.mimicFrame + 19f;
        if (s != null && s.spellFrame >= 0)     return s.spellFrame + 5f;
        if (s != null && s.wormholeFrame >= 0)  return s.wormholeFrame + 33f;

        // Idle 1-4 loop, purely time-based — deterministic across all clients.
        int idleFrame = (int) ((globalTick / TICKS_PER_FRAME) % 4) + 1;
        return idleFrame;
    }

    /** True while this entity has any transient (non-idle) animation playing. */
    public static boolean isAnimating(int entityId) {
        State s = STATES.get(entityId);
        return s != null && (s.mimicFrame >= 0 || s.spellFrame >= 0 || s.wormholeFrame >= 0);
    }
}