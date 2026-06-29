package net.filipes.rituals.entity.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.particle.ModParticles;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class BlightedPuddleEntity extends Entity {

    public static final int   LIFETIME   = 200;
    public static final int   FADE_TICKS = 20;
    private static final float CONTACT_RADIUS = 1.3f;

    private UUID ownerUUID;

    public BlightedPuddleEntity(EntityType<? extends BlightedPuddleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (this.random.nextInt(4) == 0) {
                double angle = this.random.nextDouble() * 2.0 * Math.PI;
                double radius = this.random.nextDouble() * CONTACT_RADIUS;

                double pX = this.getX() + Math.cos(angle) * radius;
                double pY = this.getY() + 0.05;
                double pZ = this.getZ() + Math.sin(angle) * radius;

                level().addParticle(ModParticles.BLIGHTED, pX, pY, pZ, 0.0, 0.02, 0.0);
            }
            return;
        }
        if (!(level() instanceof ServerLevel sl)) return;

        if (tickCount >= LIFETIME) { discard(); return; }

        AABB box = AABB.ofSize(position(), CONTACT_RADIUS * 2, 2.0, CONTACT_RADIUS * 2);
        List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity entity : nearby) {
            if (ownerUUID != null && entity.getUUID().equals(ownerUUID)) continue;
            entity.addEffect(new MobEffectInstance(ModStatusEffects.BLIGHTED, 40, 0, false, true, true));
        }
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public boolean shouldBeSaved()                                     { return false; }
    @Override public PushReaction getPistonPushReaction()                        { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                        { return false; }
    @Override public boolean isPushable()                                        { return false; }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                            { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                         { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)                 { return d < 48.0 * 48.0; }
}