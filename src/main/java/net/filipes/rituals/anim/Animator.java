package net.filipes.rituals.anim;

import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Animator {
    private AnimationClip clip;
    private float time;
    private boolean playing = true;
    private boolean reversed;

    public void play(AnimationClip clip, boolean resetTime) {
        this.clip = clip;
        if (resetTime) this.time = 0f;
        this.playing = true;
    }

    public Map<String, BonePose> sample(float tickDelta, float partialTick) {
        if (clip == null || !playing) return Map.of();
        float dt = tickDelta + partialTick;
        float t = advance(dt);
        Map<String, BonePose> out = new HashMap<>();
        for (var track : clip.tracks.values()) out.put(track.bone(), lerp(track.keys(), t));
        return out;
    }

    private float advance(float dt) {
        float t = time + dt;
        switch (clip.loop) {
            case LOOP -> t = t % clip.lengthSec;
            case PINGPONG -> {
                float span = clip.lengthSec * 2f;
                float mod = t % span;
                reversed = mod > clip.lengthSec;
                t = reversed ? span - mod : mod;
            }
            case ONCE -> {
                if (t > clip.lengthSec) { t = clip.lengthSec; playing = false; }
            }
        }
        return time = t;
    }

    private static BonePose lerp(List<Keyframe> keys, float t) {
        if (keys.isEmpty()) return BonePose.IDENTITY;
        if (t <= keys.getFirst().timeSec()) return keys.getFirst().pose();
        if (t >= keys.getLast().timeSec())  return keys.getLast().pose();
        for (int i = 0; i < keys.size() - 1; i++) {
            Keyframe a = keys.get(i), b = keys.get(i + 1);
            if (t >= a.timeSec() && t <= b.timeSec()) {
                float f = (t - a.timeSec()) / (b.timeSec() - a.timeSec());
                return new BonePose(
                        lerpVec(a.pose().translation(), b.pose().translation(), f),
                        lerpVec(a.pose().rotationRad(), b.pose().rotationRad(), f),
                        lerpVec(a.pose().scale(), b.pose().scale(), f)
                );
            }
        }
        return keys.getLast().pose();
    }

    private static Vector3f lerpVec(Vector3f a, Vector3f b, float f) {
        return new Vector3f(
                MathHelper.lerp(f, a.x(), b.x()),
                MathHelper.lerp(f, a.y(), b.y()),
                MathHelper.lerp(f, a.z(), b.z())
        );
    }
}
