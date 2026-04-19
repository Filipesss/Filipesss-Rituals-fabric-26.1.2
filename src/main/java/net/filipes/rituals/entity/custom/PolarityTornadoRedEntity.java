package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PolarityTornadoRedEntity extends Entity {

    private int lifetime = -1;

    public PolarityTornadoRedEntity(EntityType<? extends PolarityTornadoRedEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PolarityTornadoRedEntity(Level level, Vec3 position) {
        this(ModEntities.POLARITY_TORNADO_RED, level);
        this.setPos(position);
    }

    public PolarityTornadoRedEntity(Level level, Vec3 position, int lifetime) {
        this(level, position);
        this.lifetime = lifetime;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {

    }

    @Override
    public void tick() {
        super.tick();
        if (lifetime > 0 && this.tickCount >= lifetime) {
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifetime = input.getIntOr("Lifetime", this.lifetime);
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Lifetime", this.lifetime);
        output.putInt("Age", this.tickCount);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}