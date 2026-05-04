package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.particle.ModParticles;
import net.minecraft.server.level.ServerLevel;
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

    public static final int   TRAIL_LENGTH    = 10;
    public static final float HEX_LINE_LEN    = 2.5f;
    public static final int   HEX_REVEAL_TICK = 12;

    public final Vec3[] trailPositions = new Vec3[TRAIL_LENGTH];
    public int   trailHead  = 0;
    public int   trailSize  = 0;
    public int   landingTick = -1;
    private boolean tipsSpawned = false;

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
            return;
        }

        if (landingTick < 0 || level().isClientSide()) return;

        ServerLevel sv = (ServerLevel) level();
        int ticksSinceLanding = tickCount - landingTick;

        if (!tipsSpawned && ticksSinceLanding >= HEX_REVEAL_TICK) {
            tipsSpawned = true;
            double cx = getX(), cy = getY(), cz = getZ();
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3.0 * i;
                double ex    = cx + Math.cos(angle) * HEX_LINE_LEN;
                double ez    = cz + Math.sin(angle) * HEX_LINE_LEN;

                sv.sendParticles(ModParticles.LIGHTNING_TRAIL,
                        ex, cy + 0.5, ez, 1, 0, 0, 0, 0);

                int sparks = 2 + random.nextInt(2);
                for (int j = 0; j < sparks; j++) {
                    double ox = ex + (random.nextDouble() - 0.5) * 1.2;
                    double oz = ez + (random.nextDouble() - 0.5) * 1.2;
                    sv.sendParticles(ModParticles.LIGHTNING_SPARK,
                            ox, cy + 0.15, oz, 1, 0, 0, 0, 0);
                }
            }
        }

        if (tipsSpawned && (ticksSinceLanding - HEX_REVEAL_TICK) % 5 == 0) {
            double cx = getX(), cy = getY(), cz = getZ();
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3.0 * i;
                double ex    = cx + Math.cos(angle) * HEX_LINE_LEN;
                double ez    = cz + Math.sin(angle) * HEX_LINE_LEN;
                int sparks = 2 + random.nextInt(2);
                for (int j = 0; j < sparks; j++) {
                    double ox = ex + (random.nextDouble() - 0.5) * 1.2;
                    double oz = ez + (random.nextDouble() - 0.5) * 1.2;
                    sv.sendParticles(ModParticles.LIGHTNING_SPARK,
                            ox, cy + 0.15, oz, 1, 0, 0, 0, 0);
                }
            }
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

        if (landingTick >= 0) return;
        landingTick = tickCount;

        if (level().isClientSide()) return;

        ServerLevel sv = (ServerLevel) level();
        double cx = getX(), cy = getY(), cz = getZ();

        sv.sendParticles(ModParticles.LIGHTNING_EXPLOSION, cx, cy, cz, 1, 0, 0, 0, 0);

        ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                new Vec3(cx, cy, cz), 16f, 0.35f, 14);
        level().addFreshEntity(shake);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DEPTHSTRIKE);
    }
}