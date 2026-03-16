package net.filipes.rituals.anim;

import org.joml.Vector3f;

public record BonePose(Vector3f translation, Vector3f rotationRad, Vector3f scale) {
    public static final BonePose IDENTITY =
            new BonePose(new Vector3f(), new Vector3f(), new Vector3f(1f, 1f, 1f));
}
