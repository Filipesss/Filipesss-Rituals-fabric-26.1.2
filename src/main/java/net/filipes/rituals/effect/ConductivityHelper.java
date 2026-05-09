package net.filipes.rituals.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class ConductivityHelper {

    public static final int MAX_AMPLIFIER = 4; // amplifier 0–4 = level 1–5

    // Duration in ticks per amplifier: lvl1=15s, lvl2=12s, lvl3=9s, lvl4=6s, lvl5=3s
    private static final int[] DURATIONS = { 300, 240, 180, 120, 60 };

    // Bonus damage per level added on top of base hit damage
    private static final float[] DAMAGE_BONUS = { 1.0f, 2.0f, 3.5f, 5.0f, 7.0f };

    /**
     * Called on every hit (melee or ranged). If the target already has
     * Conductivity, stacks up by one level with a fresh duration.
     * Once level 5 expires it ends completely — vanilla will NOT downgrade
     * because we never re-apply a lower amplifier while a higher one is active.
     */
    public static void applyOrStack(LivingEntity target) {
        MobEffectInstance current = target.getEffect(ModStatusEffects.CONDUCTIVITY);

        int nextAmplifier;
        if (current == null) {
            nextAmplifier = 0;
        } else {
            nextAmplifier = Math.min(current.getAmplifier() + 1, MAX_AMPLIFIER);
        }

        // showParticles=true so the player can see the sparkling electricity
        target.addEffect(new MobEffectInstance(
                ModStatusEffects.CONDUCTIVITY,
                DURATIONS[nextAmplifier],
                nextAmplifier,
                false,  // ambient
                true,   // visible particles
                true    // show icon
        ));
    }

    /**
     * Returns bonus damage to add to a hit based on the target's current
     * Conductivity level. Returns 0 if the target has no Conductivity.
     */
    public static float getDamageBonus(LivingEntity target) {
        MobEffectInstance effect = target.getEffect(ModStatusEffects.CONDUCTIVITY);
        if (effect == null) return 0f;
        return DAMAGE_BONUS[effect.getAmplifier()];
    }

    /** Convenience: current level (1–5), or 0 if not present. */
    public static int getLevel(LivingEntity target) {
        MobEffectInstance effect = target.getEffect(ModStatusEffects.CONDUCTIVITY);
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }
}