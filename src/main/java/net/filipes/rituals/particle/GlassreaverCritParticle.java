package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class GlassreaverCritParticle extends SingleQuadParticle {

    protected GlassreaverCritParticle(ClientLevel level, double x, double y, double z,
                                      double vx, double vy, double vz,
                                      FabricSpriteSet spriteSet, RandomSource random) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, spriteSet.get(random));
        this.friction = 0.7F;
        this.gravity = 0.5F;
        this.xd = (random.nextDouble() - 0.5) * 0.5;
        this.yd = random.nextDouble() * 0.3 + 0.1;
        this.zd = (random.nextDouble() - 0.5) * 0.5;
        this.xd += vx * 0.4;
        this.yd += vy * 0.4;
        this.zd += vz * 0.4;
        this.quadSize *= 0.75F;
        this.lifetime = Math.max((int) (6.0F / (random.nextFloat() * 0.8F + 0.6F)), 1);
        this.hasPhysics = false;

        this.tick();
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
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
            return new GlassreaverCritParticle(level, x, y, z, vx, vy, vz, spriteSet, random);
        }
    }
}