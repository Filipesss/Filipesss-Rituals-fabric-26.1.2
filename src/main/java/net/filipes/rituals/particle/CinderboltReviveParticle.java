package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class CinderboltReviveParticle extends SingleQuadParticle {

    protected CinderboltReviveParticle(ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz,
                                       FabricSpriteSet spriteSet, RandomSource random) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, spriteSet.get(random));

        this.friction = 0.91F;
        this.gravity = 0.6F;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.quadSize *= 0.9F;
        this.lifetime = 50 + random.nextInt(20);

        this.hasPhysics = true;
        this.roll = 0.0F;
        this.oRoll = 0.0F;

        if (random.nextInt(4) == 0) {
            this.setColor(1.0F, 0.55F + random.nextFloat() * 0.2F, 0.05F + random.nextFloat() * 0.1F);
        } else {
            this.setColor(1.0F, 0.35F + random.nextFloat() * 0.25F, 0.02F);
        }

        this.tick();
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float ageF = (float) this.age + partialTick;
        float bloomIn = Mth.clamp(ageF / 6.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((this.lifetime - ageF) / 12.0F, 0.0F, 1.0F);
        return this.quadSize * bloomIn * fadeOut;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
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
            return new CinderboltReviveParticle(level, x, y, z, vx, vy, vz, spriteSet, random);
        }
    }
}