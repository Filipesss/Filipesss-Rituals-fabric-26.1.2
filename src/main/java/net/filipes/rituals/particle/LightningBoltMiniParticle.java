package net.filipes.rituals.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class LightningBoltMiniParticle extends SingleQuadParticle {

    private static final int FRAME_COUNT = 8;
    private final SpriteSet spriteSet;

    protected LightningBoltMiniParticle(ClientLevel level, double x, double y, double z,
                                        SpriteSet spriteSet) {

        super(level, x, y, z, spriteSet.get(0, FRAME_COUNT));
        this.spriteSet  = spriteSet;
        this.lifetime   = FRAME_COUNT;
        this.quadSize   = 0.55f;
        this.gravity    = 0.0f;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
    }

    public void setFacingAngle(float radians) {
        this.roll  = radians;
        this.oRoll = radians;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
        this.alpha = 1.0f - ((float) this.age / (float) this.lifetime);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float lifeProgress = ((float) this.age + partialTicks) / (float) this.lifetime;
        return this.quadSize * (1.0f - lifeProgress * 0.35f);
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
            LightningBoltMiniParticle p = new LightningBoltMiniParticle(level, x, y, z, spriteSet);

            p.setFacingAngle((float) (random.nextFloat() * Math.PI));
            return p;
        }
    }
}