package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PolarityTornadoBlueEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(PolarityTornadoBlueEntity.class, EntityDataSerializers.FLOAT);

    private static final double PUSH_RADIUS       = 6.0;
    private static final double PUSH_STRENGTH     = 0.75;
    private static final double MAX_PUSH_PER_TICK = 0.4;

    private int lifetime = -1;

    public PolarityTornadoBlueEntity(EntityType<? extends PolarityTornadoBlueEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position) {
        this(ModEntities.POLARITY_TORNADO_BLUE, level);
        this.setPos(position);
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position, int lifetime) {
        this(level, position);
        this.lifetime = lifetime;
    }

    public PolarityTornadoBlueEntity(Level level, Vec3 position, int lifetime, float visualScale) {
        this(level, position, lifetime);
        this.setVisualScale(visualScale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VISUAL_SCALE, 1.0f);
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_VISUAL_SCALE);
    }

    public void setVisualScale(float scale) {
        this.entityData.set(DATA_VISUAL_SCALE, scale);
    }

    @Override
    public void tick() {
        super.tick();

        if (lifetime > 0 && this.tickCount >= lifetime) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            applyPush();
        }
    }

    private void applyPush() {
        Vec3 center = this.position();

        AABB searchBox = new AABB(
                center.x - PUSH_RADIUS, center.y - PUSH_RADIUS, center.z - PUSH_RADIUS,
                center.x + PUSH_RADIUS, center.y + PUSH_RADIUS, center.z + PUSH_RADIUS
        );

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e.isAlive()
        );

        for (LivingEntity target : nearby) {
            Vec3 fromTornado = target.position().subtract(center);
            double distSq    = fromTornado.lengthSqr();

            if (distSq > PUSH_RADIUS * PUSH_RADIUS || distSq < 1e-4) continue;

            double rawForce     = PUSH_STRENGTH / distSq;
            double clampedForce = Math.min(rawForce, MAX_PUSH_PER_TICK);

            Vec3 pushDelta = fromTornado.normalize().scale(clampedForce);

            target.setDeltaMovement(target.getDeltaMovement().add(pushDelta));
            target.fallDistance = 0;

            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifetime  = input.getIntOr("Lifetime", this.lifetime);
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
        this.setVisualScale(input.getFloatOr("VisualScale", 1.0f));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Lifetime", this.lifetime);
        output.putInt("Age", this.tickCount);
        output.putFloat("VisualScale", this.getVisualScale());
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean isPickable()                         { return false; }
    @Override public boolean canCollideWith(Entity entity)        { return false; }
    @Override public boolean canBeCollidedWith(Entity entity)     { return false; }
    @Override public boolean isPushable()                         { return false; }
    @Override public PushReaction getPistonPushReaction()         { return PushReaction.IGNORE; }
}