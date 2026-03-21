package net.filipes.rituals.client;


public final class PulseBlasterCylinderState {
    private PulseBlasterCylinderState() {}

    private static final float SPIN_PER_SHOT = (float)(Math.PI / 1.2);
    private static final float FRICTION = 0.80f;

    private static final int GLOW_TICKS_MAX = 6;
    private static int glowTicks = 0;

    private static float angle = 0f;
    private static float velocity = 0f;

    public static void onShot() {
        velocity += SPIN_PER_SHOT;
        glowTicks = GLOW_TICKS_MAX;
    }

    public static void tick() {
        angle += velocity;
        velocity *= FRICTION;
        if (Math.abs(velocity) < 0.0002f) velocity = 0f;

        if (glowTicks > 0) glowTicks--;
    }

    public static float getAngle() {
        return angle;
    }

    public static boolean isGlowing() {
        return glowTicks > 0;
    }
}