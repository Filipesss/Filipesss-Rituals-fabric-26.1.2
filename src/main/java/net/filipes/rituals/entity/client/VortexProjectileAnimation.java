package net.filipes.rituals.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class VortexProjectileAnimation {

    // One-shot: starts at scale 1.0, pulses, then shrinks to 0.2 as the projectile dies.
    // Duration 1.0 s = 20 ticks.  Not looping — matches the projectile lifetime.
    // VortexProjectileModel.computeScale() mirrors these exact keyframe values at runtime.
    public static final AnimationDefinition SCALE_PULSE =
            AnimationDefinition.Builder.withLength(1.0F)
                    .addAnimation("main", new AnimationChannel(AnimationChannel.Targets.SCALE,
                            new Keyframe(0.0F,    KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F,   KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F,  KeyframeAnimations.scaleVec(0.7F, 0.7F, 0.7F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,    KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.6667F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,    KeyframeAnimations.scaleVec(0.2F, 0.2F, 0.2F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .addAnimation("outline", new AnimationChannel(AnimationChannel.Targets.SCALE,
                            new Keyframe(0.0F,    KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F,   KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F,  KeyframeAnimations.scaleVec(0.7F, 0.7F, 0.7F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,    KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.6667F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,    KeyframeAnimations.scaleVec(0.2F, 0.2F, 0.2F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .build();
}