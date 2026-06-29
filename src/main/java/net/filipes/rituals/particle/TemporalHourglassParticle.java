package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class TemporalHourglassParticle extends SingleQuadParticle {

    private final FabricSpriteSet spriteSet;
    private final float rotationSpeed;

    protected TemporalHourglassParticle(ClientLevel level, double x, double y, double z,
                                        double vx, double vy, double vz,
                                        FabricSpriteSet spriteSet, RandomSource random) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, spriteSet.get(random));
        this.spriteSet = spriteSet;

        this.friction = 0.98F;
        this.gravity = 0.0F;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.quadSize *= 1.4F;
        this.lifetime = 45 + random.nextInt(15);
        this.hasPhysics = false;

        this.rotationSpeed = (random.nextFloat() - 0.5F) * 0.05F;
        this.roll = random.nextFloat() * ((float) Math.PI * 2F);
        this.oRoll = this.roll;
        this.setColor(0.75F + random.nextFloat() * 0.25F, 0.85F + random.nextFloat() * 0.15F, 1.0F);

        this.tick();
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += rotationSpeed;
        this.setSprite(spriteSet.get(this.age, this.lifetime));
    }

    @Override
    public float getQuadSize(float partialTick) {
        float ageF = (float) this.age + partialTick;
        float bloomIn = Mth.clamp(ageF / 8.0F, 0.0F, 1.0F);              // slower bloom-in (was /4.0F)
        float fadeOut = Mth.clamp((this.lifetime - ageF) / 12.0F, 0.0F, 1.0F); // slower fade-out (was /8.0F)
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
            return new TemporalHourglassParticle(level, x, y, z, vx, vy, vz, spriteSet, random);
        }
    }
}