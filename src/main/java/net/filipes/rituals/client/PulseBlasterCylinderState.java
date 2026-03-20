package net.filipes.rituals.client;


public final class PulseBlasterCylinderState {

    private PulseBlasterCylinderState() {}

    private static final float SPIN_PER_SHOT = (float)(Math.PI / 1.2);   // 60°

    /** How much velocity survives each tick (0 = instant stop, 1 = never stops). */
    private static final float FRICTION = 0.80f;

    private static float angle    = 0f;   // accumulated angle (radians)
    private static float velocity = 0f;   // angular velocity  (rad/tick)

    public static void onShot() {
        velocity += SPIN_PER_SHOT;
    }


    public static void tick() {
        angle    += velocity;
        velocity *= FRICTION;
        if (Math.abs(velocity) < 0.0002f) velocity = 0f;
    }

    public static float getAngle() {
        return angle;
    }
}