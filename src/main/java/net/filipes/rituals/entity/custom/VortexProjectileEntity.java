package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class VortexProjectileEntity extends Entity {

    // Lifetime in ticks — matches the 1.0 s (20 tick) scale animation.
    // Raise this if you want the projectile to travel further before it starts shrinking.
    // Note: the renderer uses tickCount directly, so the animation always starts at spawn.
    public static final int LIFETIME = 20;

    // ── Constructors ──────────────────────────────────────────────────────────

    public VortexProjectileEntity(EntityType<? extends VortexProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // straight-line flight; set false for a dropping arc
    }

    // Convenience constructor — use this when spawning from a spell / weapon.
    // Pass a pre-scaled direction vec, e.g. direction.normalize().scale(speed).
    public VortexProjectileEntity(Level level, double x, double y, double z, Vec3 direction) {
        this(ModEntities.VORTEX_PROJECTILE, level);
        this.setPos(x, y, z);
        this.setDeltaMovement(direction);
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Nothing to sync for a basic projectile.
        // Add fields here later when you wire up color, charge level, etc.
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // Apply velocity each tick (respects block collision).
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Despawn once the shrink animation finishes.
        if (this.tickCount >= LIFETIME) {
            this.discard();
        }
    }

    // ── Interaction guards (same pattern as DepthstrikeGroundEntity) ──────────

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean isPickable()                        { return false; }
    @Override public boolean canCollideWith(Entity entity)       { return false; }
    @Override public boolean canBeCollidedWith(Entity entity)    { return false; }
    @Override public boolean isPushable()                        { return false; }
    @Override public PushReaction getPistonPushReaction()        { return PushReaction.IGNORE; }

    // ── Save / load ───────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Extend when you add persistent state (owner UUID, charge level, etc.)
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Extend when you add persistent state.
    }
}