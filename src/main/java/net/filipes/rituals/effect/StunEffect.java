package net.filipes.rituals.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

public class StunEffect extends StatusEffect {

    public StunEffect() {
        super(StatusEffectCategory.HARMFUL, 0x9B6DFF); // purple particle color
        // Directly bolt -90% speed onto the movement attribute.
        // Because this is our own effect, undead inversion never applies.
        addAttributeModifier(
                EntityAttributes.MOVEMENT_SPEED,
                Identifier.of("rituals", "stun_slowness"),
                -0.9,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}