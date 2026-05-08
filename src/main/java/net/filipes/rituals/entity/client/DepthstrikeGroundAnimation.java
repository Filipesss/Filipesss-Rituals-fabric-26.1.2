package net.filipes.rituals.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class DepthstrikeGroundAnimation {

    public static final AnimationDefinition DEPTHSTRIKE_ANIMATION =
            AnimationDefinition.Builder.withLength(2.0F)
                    .addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F,    KeyframeAnimations.degreeVec(-20.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,    KeyframeAnimations.degreeVec(-20.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8333F, KeyframeAnimations.degreeVec(  0.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,    KeyframeAnimations.degreeVec(  0.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1667F, KeyframeAnimations.degreeVec(  2.5F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F,  KeyframeAnimations.degreeVec(-22.5F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .addAnimation("bone2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F,    KeyframeAnimations.degreeVec( 30.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,    KeyframeAnimations.degreeVec( 30.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8333F, KeyframeAnimations.degreeVec(  0.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,    KeyframeAnimations.degreeVec(  0.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1667F, KeyframeAnimations.degreeVec( -2.5F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F,  KeyframeAnimations.degreeVec( 25.0F,  0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .build();
}