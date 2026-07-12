package net.filipes.rituals.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class ConductivityHelper {

    public static final int MAX_AMPLIFIER = 4;
    private static final int[] DURATIONS = { 300, 240, 180, 120, 60 };
    private static final float[] DAMAGE_BONUS = { 1.0f, 2.0f, 3.5f, 5.0f, 7.0f };

    public static void applyOrStack(LivingEntity target) {
        MobEffectInstance current = target.getEffect(ModStatusEffects.CONDUCTIVITY);

        int nextAmplifier;
        if (current == null) {
            nextAmplifier = 0;
        } else {
            nextAmplifier = Math.min(current.getAmplifier() + 1, MAX_AMPLIFIER);
        }
        target.removeEffect(ModStatusEffects.CONDUCTIVITY);

        target.addEffect(new MobEffectInstance(
                ModStatusEffects.CONDUCTIVITY,
                DURATIONS[nextAmplifier],
                nextAmplifier,
                false,
                true,
                true
        ));
    }

    public static void applyLevelOne(LivingEntity target) {
        MobEffectInstance current = target.getEffect(ModStatusEffects.CONDUCTIVITY);
        if (current != null) return;
        target.addEffect(new MobEffectInstance(
                ModStatusEffects.CONDUCTIVITY,
                DURATIONS[0],
                0,
                false, true, true
        ));
    }

    public static float getDamageBonus(LivingEntity target) {
        MobEffectInstance effect = target.getEffect(ModStatusEffects.CONDUCTIVITY);
        if (effect == null) return 0f;
        return DAMAGE_BONUS[effect.getAmplifier()];
    }

    public static int getLevel(LivingEntity target) {
        MobEffectInstance effect = target.getEffect(ModStatusEffects.CONDUCTIVITY);
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }
}