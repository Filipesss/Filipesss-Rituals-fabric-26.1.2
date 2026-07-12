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
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CinderArrowEntity extends AbstractArrow {

    public static final int TYPE_FIRE    = 0;
    public static final int TYPE_PIERCE  = 1;
    public static final int TYPE_EXPLODE = 2;

    public static final double FIRE_DAMAGE    = 7.0;
    public static final double PIERCE_DAMAGE  = 10.0;
    public static final double EXPLODE_DAMAGE = 12.0;

    private static final EntityDataAccessor<Integer> ARROW_TYPE =
            SynchedEntityData.defineId(CinderArrowEntity.class, EntityDataSerializers.INT);

    private final Set<UUID> pierceHits = new HashSet<>();
    private static final int MAX_PIERCE_HITS = 3;
    private double explodeDamage = EXPLODE_DAMAGE;
    private double pierceDamage  = PIERCE_DAMAGE;

    public CinderArrowEntity(EntityType<? extends CinderArrowEntity> type, Level level) {
        super(type, level);
    }

    public CinderArrowEntity(Level level, LivingEntity shooter, ItemStack weapon) {
        super(ModEntities.CINDER_ARROW, shooter, level, weapon, null);
        this.setBaseDamage(FIRE_DAMAGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ARROW_TYPE, TYPE_FIRE);
    }

    public int getArrowType() { return entityData.get(ARROW_TYPE); }

    public void setArrowType(int type) {
        entityData.set(ARROW_TYPE, type);
        switch (type) {
            case TYPE_FIRE -> {
                this.setBaseDamage(FIRE_DAMAGE);
            }
            case TYPE_PIERCE -> {
                pierceDamage = PIERCE_DAMAGE;
            }
            case TYPE_EXPLODE -> {
                explodeDamage = EXPLODE_DAMAGE;
            }
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

                double speedMultiplier = this.getDeltaMovement().length();

                float finalDamage = (float) Math.ceil(speedMultiplier * pierceDamage);

                target.hurt(
                        level().damageSources().arrow(this, getOwner()),
                        finalDamage
                );
                target.igniteForSeconds(4);

                level().playSound(null, hit.getX(), hit.getY(), hit.getZ(),
                        SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.6f);
            }

            if (pierceHits.size() >= MAX_PIERCE_HITS) discard();
            return;
        }

        if (type == TYPE_EXPLODE) {
            if (level().isClientSide()) return;

            if (result.getEntity() instanceof LivingEntity target) {

                double speedMultiplier = this.getDeltaMovement().length();

                float finalExplodeDamage = (float) Math.ceil(speedMultiplier * explodeDamage);

                target.hurt(
                        level().damageSources().arrow(this, getOwner()),
                        finalExplodeDamage
                );
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

            discard();
            return;
        }

        super.onHitEntity(result);

        if (level().isClientSide()) return;

        if (result.getEntity() instanceof LivingEntity target) {
            target.igniteForSeconds(4);
            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (getArrowType() == TYPE_PIERCE && pierceHits.contains(target.getUUID())) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putDouble("ExplodeDamage", this.explodeDamage);
        output.putDouble("PierceDamage", this.pierceDamage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.explodeDamage = input.getDoubleOr("ExplodeDamage", EXPLODE_DAMAGE);
        this.pierceDamage  = input.getDoubleOr("PierceDamage", PIERCE_DAMAGE);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}