package net.filipes.rituals.entity.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PolarityArrowBlueEntity extends AbstractArrow {

    public static final double BASE_DAMAGE       = 1.6;
    public static final float  SPEED_MULTIPLIER  = 1.4f;

    private boolean comboEnabled = false;
    private boolean hitSomething = false;

    public PolarityArrowBlueEntity(EntityType<? extends PolarityArrowBlueEntity> type, Level level) {
        super(type, level);
    }

    public PolarityArrowBlueEntity(Level level, LivingEntity shooter, ItemStack arrowStack, ItemStack weapon) {
        super(ModEntities.POLARITY_ARROW_BLUE, shooter, level, arrowStack.copyWithCount(1), weapon);
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

        if (this.comboEnabled && !this.level().isClientSide() && !this.hitSomething) {
            if (this.getY() < this.level().getMinY() - 32) {
                this.hitSomething = true;
                if (this.getOwner() instanceof Player player) {
                    PolarityBowItem.resetCombo(player);
                }
            }
        }

        trailPositions[trailHead] = position();
        trailHead = (trailHead + 1) % TRAIL_LENGTH;
        if (trailSize < TRAIL_LENGTH) trailSize++;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {

        boolean comboRelevant = this.comboEnabled && !this.level().isClientSide()
                && this.getOwner() instanceof Player;
        if (comboRelevant) {
            this.hitSomething = true;
        }

        if (comboRelevant && entityHitResult.getEntity() instanceof LivingEntity) {
            Player player = (Player) this.getOwner();
            int prospectiveCombo = PolarityBowItem.getCombo(player.getUUID()) + 1;
            double bonus = PolarityBowItem.getComboBonusDamage(prospectiveCombo);
            if (bonus > 0) {
                this.setBaseDamage(this.baseDamageValue + bonus);
            }
        }

        super.onHitEntity(entityHitResult);

        if (comboRelevant) {
            Player player = (Player) this.getOwner();
            if (entityHitResult.getEntity() instanceof LivingEntity) {
                PolarityBowItem.incrementCombo(player);
            } else {
                PolarityBowItem.resetCombo(player);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.comboEnabled && !this.level().isClientSide() && this.getOwner() instanceof Player player) {
            this.hitSomething = true;
            PolarityBowItem.resetCombo(player);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.comboEnabled && !this.level().isClientSide() && !this.hitSomething) {
            if (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED) {
                if (this.getOwner() instanceof Player player) {
                    PolarityBowItem.resetCombo(player);
                }
            }
        }
        super.remove(reason);
    }
    private double baseDamageValue;

    public void setTrackedBaseDamage(double value) {
        this.baseDamageValue = value;
        this.setBaseDamage(value);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ComboEnabled", this.comboEnabled);
        output.putBoolean("HitSomething", this.hitSomething);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.comboEnabled = input.getBooleanOr("ComboEnabled", false);
        this.hitSomething = input.getBooleanOr("HitSomething", false);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}