package net.filipes.rituals.anim;

import org.joml.Vector3f;

import java.util.List;

public final class AnimationClips {
    private AnimationClips() {}

    public static final AnimationClip PULSE_BEAM_IDLE = new AnimationClip(
            "pulse_idle", 2.0f, AnimationClip.LoopMode.LOOP, List.of(
            new BoneTrack("beam", List.of(
                    new Keyframe(0f, new BonePose(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f))),
                    new Keyframe(1f, new BonePose(new Vector3f(0f, 0.05f, 0f), new Vector3f(0f, 0.35f, 0f), new Vector3f(1f, 1f, 1f))),
                    new Keyframe(2f, new BonePose(new Vector3f(0f, 0f, 0f), new Vector3f(0f,-0.35f, 0f), new Vector3f(1f, 1f, 1f)))
            ))
    )
    );
}
