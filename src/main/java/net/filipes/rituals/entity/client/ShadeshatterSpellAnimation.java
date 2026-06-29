package net.filipes.rituals.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class ShadeshatterSpellAnimation {

    public static final AnimationDefinition SPELL =
            AnimationDefinition.Builder.withLength(6.0F)
                    .addAnimation("main", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F, KeyframeAnimations.degreeVec(  0.0F,     0.0F,   0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(  0.0F,  -360.0F,   5.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.0F, KeyframeAnimations.degreeVec(  0.0F,  -720.0F,  10.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(6.0F, KeyframeAnimations.degreeVec(  0.0F, -1080.0F,  15.0F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .addAnimation("ring", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F, KeyframeAnimations.degreeVec(  0.0F,     0.0F,   0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(  0.0F,  -720.0F, -17.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.0F, KeyframeAnimations.degreeVec(  0.0F, -1440.0F, -35.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(6.0F, KeyframeAnimations.degreeVec(  0.0F, -2160.0F, -52.5F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .build();
}