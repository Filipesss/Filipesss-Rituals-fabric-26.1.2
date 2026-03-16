package net.filipes.rituals.client;

/**
 * Client-side spin state for the Pulse Blaster's cylinder drum.
 * Updated every client tick; read by PulseBlasterSpecialRenderer.
 */
public final class PulseBlasterCylinderState {

    private PulseBlasterCylinderState() {}

    /** 1/6 of a full turn — advances one chamber per shot on a 6-chamber drum. */
    private static final float SPIN_PER_SHOT = (float)(Math.PI / 3.0);   // 60°

    /** How much velocity survives each tick (0 = instant stop, 1 = never stops). */
    private static final float FRICTION = 0.70f;

    private static float angle    = 0f;   // accumulated angle (radians)
    private static float velocity = 0f;   // angular velocity  (rad/tick)

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called by the HUD overlay whenever the server confirms a shot fired
     * (i.e. the live-ammo count just decreased by 1).
     */
    public static void onShot() {
        velocity += SPIN_PER_SHOT;
    }

    /**
     * Advance the spin simulation by one tick.
     * Register this via {@code ClientTickEvents.END_CLIENT_TICK}.
     */
    public static void tick() {
        angle    += velocity;
        velocity *= FRICTION;
        if (Math.abs(velocity) < 0.0002f) velocity = 0f;
    }

    /** Current cylinder angle in radians (no interpolation needed at 60+ fps). */
    public static float getAngle() {
        return angle;
    }
}