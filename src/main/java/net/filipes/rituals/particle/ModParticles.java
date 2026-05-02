package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModParticles {

    public static final SimpleParticleType LIGHTNING_BOLT_MINI =
            FabricParticleTypes.simple();

    public static final SimpleParticleType LIGHTNING_TRAIL =
            FabricParticleTypes.simple();

    public static final SimpleParticleType LIGHTNING_EXPLOSION =
            FabricParticleTypes.simple();

    public static final SimpleParticleType LIGHTNING_SPARK =
            FabricParticleTypes.simple();

    public static void register() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "lightning_bolt_mini"),
                LIGHTNING_BOLT_MINI
        );
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "lightning_trail"),
                LIGHTNING_TRAIL
        );
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "lightning_explosion"),
                LIGHTNING_EXPLOSION
        );
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "lightning_spark"),
                LIGHTNING_SPARK
        );
    }
}