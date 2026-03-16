package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class PulseBlasterBeamEntity extends ProjectileEntity {

    private static final float DAMAGE     = 8.0f;
    private static final int   FIRE_TICKS = 5;
    private static final float BEAM_SPEED = 1.5f;
    private static final int   MAX_AGE    = 80;

    private final World storedWorld;

    public PulseBlasterBeamEntity(
            EntityType<? extends PulseBlasterBeamEntity> type,
            World world
    ) {
        super(type, world);
        this.storedWorld = world;
        this.setNoGravity(true);
    }

    public PulseBlasterBeamEntity(World world, LivingEntity owner) {
        this(ModEntities.PULSE_BLASTER_BEAM, world);

        this.setOwner(owner);

        this.setPosition(
                owner.getX(),
                owner.getEyeY() - 0.1,
                owner.getZ()
        );

        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0f, BEAM_SPEED, 0.0f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) { }



    @Override
    public void tick() {
        super.tick();

        Vec3d currentPos = this.getEntityPos();
        Vec3d velocity = this.getVelocity();
        Vec3d nextPos = currentPos.add(velocity);

        // Check entity collisions first
        EntityHitResult entityHit = ProjectileUtil.getEntityCollision(
                storedWorld,
                this,
                currentPos,
                nextPos,
                this.getBoundingBox().stretch(velocity).expand(1.0),
                entity -> !entity.isSpectator() && entity != this.getOwner()
        );

        if (entityHit != null && !storedWorld.isClient()) {
            Entity target = entityHit.getEntity();
            ServerWorld serverWorld = (ServerWorld) storedWorld;

            target.damage(
                    serverWorld,
                    serverWorld.getDamageSources().thrown(this, this.getOwner()),
                    DAMAGE
            );
            target.setOnFireFor(FIRE_TICKS);
            this.discard();
            return;
        }

        // Check block collisions
        BlockHitResult blockHit = storedWorld.raycast(new RaycastContext(
                currentPos,
                nextPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                this
        ));

        if (blockHit.getType() != HitResult.Type.MISS && !storedWorld.isClient()) {
            BlockPos firePos = blockHit.getBlockPos().offset(blockHit.getSide());

            if (storedWorld.isAir(firePos)) {
                storedWorld.setBlockState(
                        firePos,
                        AbstractFireBlock.getState(storedWorld, firePos)
                );
            }
            this.discard();
            return;
        }

        // No collision — move the entity
        this.setPosition(nextPos.x, nextPos.y, nextPos.z);

        if (this.age > MAX_AGE) {
            this.discard();
        }
    }
}