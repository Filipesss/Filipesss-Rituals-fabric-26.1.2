package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class LightningTrailParticle extends SingleQuadParticle {

    private static final int FRAME_COUNT = 8;
    private final SpriteSet spriteSet;

    protected LightningTrailParticle(ClientLevel level, double x, double y, double z,
                                     SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet.get(0, FRAME_COUNT));
        this.spriteSet  = spriteSet;
        this.lifetime   = FRAME_COUNT;
        this.quadSize   = 2.0f;
        this.gravity    = 0.0f;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
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
            return new LightningTrailParticle(level, x, y, z, spriteSet);
        }
    }
}