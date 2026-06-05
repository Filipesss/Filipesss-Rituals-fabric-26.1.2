package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class BlightedParticle extends SingleQuadParticle {

    protected BlightedParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz,
                               FabricSpriteSet spriteSet, RandomSource random) {
        super(level, x, y, z, spriteSet.get(random));

        // Slower, longer life to appreciate the chaotic jiggling
        this.lifetime = 50 + random.nextInt(40);

        // "A lot smaller" - scaling down from 0.5f to a tiny ember size
        this.quadSize = 0.12f + random.nextFloat() * 0.08f;

        this.hasPhysics = true;

        // Crucially slower initial velocities
        this.xd = (random.nextDouble() - 0.5) * 0.04;
        this.yd = random.nextDouble() * 0.03 + 0.02; // Gentle upward drift
        this.zd = (random.nextDouble() - 0.5) * 0.04;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < this.lifetime) {
            // 1. Organic Swaying: Use sine/cosine waves mapped to the particle's age for a smooth weaving motion
            double swayX = Math.sin(this.age * 0.3) * 0.012;
            double swayZ = Math.cos(this.age * 0.3) * 0.012;

            // 2. Chaotic Jiggling: Add pure random twitching per tick
            double jiggleX = (this.random.nextDouble() - 0.5) * 0.015;
            double jiggleY = (this.random.nextDouble() - 0.4) * 0.012; // Slight bias upward, but can bob downward
            double jiggleZ = (this.random.nextDouble() - 0.5) * 0.015;

            // Apply forces
            this.xd += swayX + jiggleX;
            this.yd += jiggleY;
            this.zd += swayZ + jiggleZ;

            // Dampen velocity slightly so it doesn't accelerate infinitely into outer space
            this.xd *= 0.92;
            this.yd *= 0.95;
            this.zd *= 0.92;
        }

        // Smooth fade out as it dies
        this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final FabricSpriteSet spriteSet;

        public Factory(FabricSpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz,
                                       RandomSource random) {
            return new BlightedParticle(level, x, y, z, vx, vy, vz, spriteSet, random);
        }
    }
}