package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PolarityArrowBlueEntity extends Arrow {

    public static final double BASE_DAMAGE       = 1.8;
    public static final float  SPEED_MULTIPLIER  = 1.4f;

    public PolarityArrowBlueEntity(EntityType<? extends PolarityArrowBlueEntity> type, Level level) {
        super(type, level);
    }

    public PolarityArrowBlueEntity(Level level, LivingEntity shooter, ItemStack weapon) {
        this(ModEntities.POLARITY_ARROW_BLUE, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.setBaseDamage(BASE_DAMAGE);
    }

    public static final int TRAIL_LENGTH = 10;
    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int trailHead = 0;
    public int trailSize = 0;

    @Override
    public void tick() {
        this.setCritArrow(false);
        super.tick();
        trailPositions[trailHead] = position();
        trailHead = (trailHead + 1) % TRAIL_LENGTH;
        if (trailSize < TRAIL_LENGTH) trailSize++;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}