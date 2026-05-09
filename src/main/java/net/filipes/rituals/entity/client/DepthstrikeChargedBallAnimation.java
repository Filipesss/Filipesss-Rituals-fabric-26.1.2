package net.filipes.rituals.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class DepthstrikeChargedBallAnimation {

    public static final AnimationDefinition IDLE =
            AnimationDefinition.Builder.withLength(2.25F).looping()
                    .addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F,  KeyframeAnimations.degreeVec( 12.6258F, -26.7426F,  -32.7515F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(-243.797F,  -1.5275F,   78.252F ), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,  KeyframeAnimations.degreeVec( -81.4491F, -74.2087F,   1.2756F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec( -70.6F,    42.3599F,  -13.1276F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,  KeyframeAnimations.degreeVec(  41.3205F, -35.1969F,   4.5099F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.25F, KeyframeAnimations.degreeVec( -19.2082F,  27.8622F,  -59.7288F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5F,  KeyframeAnimations.degreeVec(  18.1151F, -44.3719F,  84.5223F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.75F, KeyframeAnimations.degreeVec( -80.8405F, -30.8844F, 223.8676F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F,  KeyframeAnimations.degreeVec(   4.7105F, -40.5621F,  84.6448F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.25F, KeyframeAnimations.degreeVec(  12.6258F, -26.7426F,  -32.7515F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .addAnimation("bone2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F,  KeyframeAnimations.degreeVec( 54.2063F,  12.0174F,  16.1065F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(114.24F,     3.4519F, -46.4924F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,  KeyframeAnimations.degreeVec( -41.37F,  -20.3887F,  55.256F ), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(150.4223F, -24.8693F, -18.6209F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,  KeyframeAnimations.degreeVec( -48.3825F, -45.3994F,  79.0098F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.25F, KeyframeAnimations.degreeVec(107.9348F, -10.0056F,-117.3174F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5F,  KeyframeAnimations.degreeVec( 98.3688F,  39.8141F, -46.1086F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.75F, KeyframeAnimations.degreeVec( 46.8311F,  10.4996F,  67.6271F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F,  KeyframeAnimations.degreeVec( -93.4082F, 32.7472F, -26.6419F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.25F, KeyframeAnimations.degreeVec( 54.2063F,  12.0174F,  16.1065F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .addAnimation("bone3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0.0F,  KeyframeAnimations.degreeVec( -38.0758F,  41.6411F,  27.2231F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(  43.1191F, -22.5383F,  -5.6358F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F,  KeyframeAnimations.degreeVec(  86.3355F,   5.4412F, -94.0463F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec( -57.8521F, -28.1317F, -28.0272F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F,  KeyframeAnimations.degreeVec(  12.84F,   -36.6985F, -13.5346F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.25F, KeyframeAnimations.degreeVec(-157.7706F, -14.7817F,  -6.6478F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5F,  KeyframeAnimations.degreeVec(   4.8085F,  41.1137F, -29.0351F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.75F, KeyframeAnimations.degreeVec(-167.4431F, -12.1622F,-100.0183F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F,  KeyframeAnimations.degreeVec(-107.9015F, -50.7752F,-153.7815F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.25F, KeyframeAnimations.degreeVec( -38.0758F,  41.6411F,  27.2231F), AnimationChannel.Interpolations.LINEAR)
                    ))
                    .build();
}