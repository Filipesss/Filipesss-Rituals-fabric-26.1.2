package net.filipes.rituals.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MoonParticle extends SingleQuadParticle {

    private final float rotationSpeed;

    protected MoonParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz,
                           FabricSpriteSet spriteSet, RandomSource random) {
        // Grab the first/only available sprite in the set
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, spriteSet.get(random));

        this.friction = 0.98F;
        this.gravity = 0.0F;

        // Making it a slow particle by cutting the initial velocity significantly
        this.xd = vx * 0.15D;
        this.yd = vy * 0.15D;
        this.zd = vz * 0.15D;

        this.quadSize *= 1.2F;
        this.lifetime = 60 + random.nextInt(20); // Lasts slightly longer since it's slow
        this.hasPhysics = false;

        // Zeroed out entirely so it locks to the raw PNG orientation
        this.rotationSpeed = 0.0F;
        this.roll = 0.0F;
        this.oRoll = 0.0F;

        // Pure white/silver tint for a moon vibe
        this.setColor(0.9F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.1F, 1.0F);

        this.tick();
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += rotationSpeed;
        // Removed setSprite() so it stays on moon_particle_0 permanently
    }

    @Override
    public float getQuadSize(float partialTick) {
        float ageF = (float) this.age + partialTick;
        float bloomIn = Mth.clamp(ageF / 10.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((this.lifetime - ageF) / 15.0F, 0.0F, 1.0F);
        return this.quadSize * bloomIn * fadeOut;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880; // Full bright glow
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
            return new MoonParticle(level, x, y, z, vx, vy, vz, spriteSet, random);
        }
    }
}