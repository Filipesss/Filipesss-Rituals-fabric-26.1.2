package net.filipes.rituals.entity.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PolarityArrowRedEntity extends Arrow {

    public static final double BASE_DAMAGE       = 5.5;
    public static final float  SPEED_MULTIPLIER  = 0.65f;

    private boolean comboEnabled = false;

    public PolarityArrowRedEntity(EntityType<? extends PolarityArrowRedEntity> type, Level level) {
        super(type, level);
    }

    public PolarityArrowRedEntity(Level level, LivingEntity shooter, ItemStack weapon) {
        this(ModEntities.POLARITY_ARROW_RED, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.setBaseDamage(BASE_DAMAGE);
        this.comboEnabled = ModDataComponents.getStage(weapon) >= 3;
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
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.comboEnabled && !this.level().isClientSide() && this.getOwner() instanceof Player player) {
            if (entityHitResult.getEntity() instanceof LivingEntity) {
                PolarityBowItem.incrementCombo(player);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.comboEnabled && !this.level().isClientSide() && this.getOwner() instanceof Player player) {
            PolarityBowItem.resetCombo(player);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ComboEnabled", this.comboEnabled);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.comboEnabled = input.getBooleanOr("ComboEnabled", false);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}