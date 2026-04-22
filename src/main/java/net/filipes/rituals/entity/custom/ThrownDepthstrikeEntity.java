package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownDepthstrikeEntity extends AbstractArrow {

    public static final int TRAIL_LENGTH = 10;
    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int trailHead = 0;
    public int trailSize = 0;

    public ThrownDepthstrikeEntity(EntityType<? extends ThrownDepthstrikeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownDepthstrikeEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_DEPTHSTRIKE, owner, level, stack, null);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isInGround()) {
            trailPositions[trailHead] = new Vec3(getX(), getY(), getZ());
            trailHead = (trailHead + 1) % TRAIL_LENGTH;
            if (trailSize < TRAIL_LENGTH) trailSize++;
        }
    }

    public boolean isThrownInGround() {
        return this.isInGround();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DEPTHSTRIKE);
    }
}