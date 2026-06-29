package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CinderArrowEntity extends Arrow {

    public static final int TYPE_FIRE    = 0;
    public static final int TYPE_PIERCE  = 1;
    public static final int TYPE_EXPLODE = 2;

    private static final EntityDataAccessor<Integer> ARROW_TYPE =
            SynchedEntityData.defineId(CinderArrowEntity.class, EntityDataSerializers.INT);

    private final Set<UUID> pierceHits = new HashSet<>();
    private static final int MAX_PIERCE_HITS = 3;
    private double explodeDamage = 6.0;
    private double pierceDamage = 6.0;

    public CinderArrowEntity(EntityType<? extends CinderArrowEntity> type, Level level) {
        super(type, level);
    }

    public CinderArrowEntity(Level level, LivingEntity shooter, ItemStack weapon) {
        this(ModEntities.CINDER_ARROW, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ARROW_TYPE, TYPE_FIRE);
    }

    public int getArrowType() { return entityData.get(ARROW_TYPE); }

    public void setArrowType(int type) {
        entityData.set(ARROW_TYPE, type);
        if (type == TYPE_EXPLODE) {
            explodeDamage = 10.0;
        }
    }

    public static final int TRAIL_LENGTH = 14;
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
    protected void onHitEntity(EntityHitResult result) {
        int type = getArrowType();

        if (type == TYPE_PIERCE) {
            Entity hit = result.getEntity();
            if (pierceHits.contains(hit.getUUID())) return;
            pierceHits.add(hit.getUUID());

            if (hit instanceof LivingEntity target && !level().isClientSide()) {
                target.hurt(
                        level().damageSources().arrow(this, getOwner()),
                        (float) pierceDamage
                );
                target.igniteForSeconds(4);

                level().playSound(null, hit.getX(), hit.getY(), hit.getZ(),
                        SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.6f);
            }

            if (pierceHits.size() >= MAX_PIERCE_HITS) discard();
            return;
        }

        super.onHitEntity(result);

        if (level().isClientSide()) return;

        if (result.getEntity() instanceof LivingEntity target) {
            switch (type) {
                case TYPE_FIRE -> {
                    target.igniteForSeconds(4);
                    level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
                }
                case TYPE_EXPLODE -> {
                    target.igniteForSeconds(4);
                    Vec3 pos = target.position();

                    level().explode(this, pos.x, pos.y, pos.z,
                            2.5f, Level.ExplosionInteraction.NONE);

                    level().playSound(null, pos.x, pos.y, pos.z,
                            SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0f, 0.5f);

                    ServerLevel sv = (ServerLevel) level();

                    ScreenShakeEntity shake = new ScreenShakeEntity(level(),
                            pos, 16f, 0.4f, 12);
                    sv.addFreshEntity(shake);

                    for (int i = 0; i < 8; i++) {
                        double angle  = Math.random() * 2.0 * Math.PI;
                        double upward = Math.random() * 0.5 + 0.2;
                        double speed  = Math.random() * 0.5 + 0.3;
                        SparkEntity spark = new SparkEntity(ModEntities.SPARK, sv,
                                pos.x, pos.y + 0.5, pos.z);
                        spark.applyPreset(SparkPresets.CINDERBOLT_EXPLOSION);
                        spark.forcedVelocity = new Vec3(
                                Math.cos(angle) * speed,
                                upward,
                                Math.sin(angle) * speed
                        );
                        sv.addFreshEntity(spark);
                    }

                    int upCount = 5;
                    for (int i = 0; i < upCount; i++) {
                        double angle  = (2.0 * Math.PI / upCount) * i;
                        double spread = Math.random() * 0.15;
                        double rise   = Math.random() * 0.4 + 0.8;
                        SparkEntity spark = new SparkEntity(ModEntities.SPARK, sv,
                                pos.x, pos.y + 0.5, pos.z);
                        spark.applyPreset(SparkPresets.CINDERBOLT_EXPLOSION);
                        spark.forcedVelocity = new Vec3(
                                Math.cos(angle) * spread,
                                rise,
                                Math.sin(angle) * spread
                        );
                        sv.addFreshEntity(spark);
                    }
                }
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (getArrowType() == TYPE_PIERCE && pierceHits.contains(target.getUUID())) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}