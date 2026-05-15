package net.filipes.rituals.entity.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpiralStabEntity extends Entity {

    public static final int   STAB_COUNT        = 28;
    public static final float SPIRAL_RADIUS      = 5.0f;
    public static final int   EMERGE_TICKS       = 2;
    public static final int   HOLD_TICKS         = 40;
    public static final int   RETRACT_TICKS      = 3;
    public static final int   STAGGER_TICKS      = 1;
    public static final float MAX_CURVE          = 0.18f;
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(SpiralStabEntity.class, EntityDataSerializers.INT);
    private static final float STAB_HIT_RADIUS = 0.9f;
    private final Set<Integer> damagedStabs = new HashSet<>();
    private java.util.UUID ownerUUID;

    public int totalLifetime;

    public static class StabData {
        public float angle;
        public float radius;
        public float curveX;
        public float curveZ;
        public int   spawnTick;
        public float originX, originY, originZ;
    }

    public List<StabData> stabs = new ArrayList<>();
    private boolean stabsBuilt = false;

    private LivingEntity owner;

    public SpiralStabEntity(EntityType<? extends SpiralStabEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        totalLifetime = STAGGER_TICKS * STAB_COUNT + EMERGE_TICKS + HOLD_TICKS + RETRACT_TICKS + 10;
    }

    public SpiralStabEntity(EntityType<? extends SpiralStabEntity> type, Level level,
                            LivingEntity owner, double x, double y, double z) {
        this(type, level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.setPos(x, y, z);
        if (!level.isClientSide()) {
            this.setOwnerId(owner.getId());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1) {
            if (level().isClientSide()) {
                Entity e = level().getEntity(getOwnerId());
                if (e instanceof LivingEntity le) owner = le;
            }
            buildStabs();
        }

        if (!level().isClientSide()) {
            tickDamage();
        }

        if (tickCount >= totalLifetime) {
            discard();
        }
    }

    private void tickDamage() {
        if (stabs.isEmpty()) return;

        if (owner == null) {
            Entity e = level().getEntity(getOwnerId());
            if (e instanceof LivingEntity le) owner = le;
        }
        if (owner == null) return;

        float baseDamage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.62f; // damage

        for (int i = 0; i < stabs.size(); i++) {
            StabData s = stabs.get(i);

            if (tickCount - 1 != s.spawnTick) continue;
            if (damagedStabs.contains(i)) continue;
            damagedStabs.add(i);

            AABB box = new AABB(
                    s.originX - STAB_HIT_RADIUS, s.originY - 0.5, s.originZ - STAB_HIT_RADIUS,
                    s.originX + STAB_HIT_RADIUS, s.originY + 2.5, s.originZ + STAB_HIT_RADIUS
            );

            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != owner && !e.getUUID().equals(ownerUUID));

            for (LivingEntity target : targets) {
                target.hurtServer(
                        (ServerLevel) level(),
                        level().damageSources().indirectMagic(this, owner),
                        baseDamage
                );
            }
        }
    }

    private void buildStabs() {
        if (stabsBuilt) return;
        stabsBuilt = true;
        stabs.clear();

        long seed = (long)(getX() * 1000) ^ (long)(getZ() * 1000) ^ 0xCAFEBABEL;

        int arms = 2;
        for (int i = 0; i < STAB_COUNT; i++) {
            StabData s = new StabData();

            float t = i / (float)(STAB_COUNT - 1); // 0..1
            s.radius = 0.4f + t * (SPIRAL_RADIUS - 0.4f);

            float baseAngle = (float)(i * Math.PI * 2.0 / (STAB_COUNT / (double)arms));
            baseAngle += (i % arms) * (float)Math.PI;
            seed = lcg(seed);
            float angleJitter = ((seed & 0xFF) / 255f - 0.5f) * 0.35f;
            s.angle = baseAngle + angleJitter;

            seed = lcg(seed);
            s.curveX = ((seed & 0xFF) / 255f - 0.5f) * 2f * MAX_CURVE;
            seed = lcg(seed);
            s.curveZ = ((seed & 0xFF) / 255f - 0.5f) * 2f * MAX_CURVE;

            s.spawnTick = i * STAGGER_TICKS;

            s.originX = (float)(getX() + Math.cos(s.angle) * s.radius + s.curveX * s.radius);
            s.originY = (float) getY();
            s.originZ = (float)(getZ() + Math.sin(s.angle) * s.radius + s.curveZ * s.radius);

            stabs.add(s);
        }
    }

    public float getStabProgress(StabData s, float pt) {
        float age = tickCount - 1 + pt - s.spawnTick;
        if (age < 0) return -1f;
        if (age < EMERGE_TICKS) {
            float raw = age / (float) EMERGE_TICKS;
            return 1f - (1f - raw) * (1f - raw);
        }
        if (age < EMERGE_TICKS + HOLD_TICKS) return 1f;
        float retractAge = age - EMERGE_TICKS - HOLD_TICKS;
        if (retractAge < RETRACT_TICKS) {
            float raw = retractAge / (float) RETRACT_TICKS;
            return 1f - raw * raw;
        }
        return -1f;
    }

    private static long lcg(long seed) {
        return seed * 6364136223846793005L + 1442695040888963407L;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
    }

    public int  getOwnerId()    { return entityData.get(OWNER_ID); }
    public void setOwnerId(int v) { entityData.set(OWNER_ID, v); }

    @Override public boolean shouldBeSaved()                            { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)      {}
    @Override protected void addAdditionalSaveData(ValueOutput out)     {}
    @Override public PushReaction getPistonPushReaction()               { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                               { return false; }
    @Override public boolean isPushable()                               { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)        { return d < (128.0 * 128.0); }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                   { return false; }
    @Override public boolean canBeCollidedWith(Entity e)               { return false; }
}