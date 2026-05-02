package net.filipes.rituals.entity.custom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class LightningStrikeEntity extends Entity {

    // ── Synced data ───────────────────────────────────────────────────────────

    private static final EntityDataAccessor<Float>   STRIKE_HEIGHT = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION      = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    // Outer glow color
    private static final EntityDataAccessor<Integer> GLOW_R        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GLOW_G        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GLOW_B        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    // Thin inner core color
    private static final EntityDataAccessor<Integer> CORE_R        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORE_G        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CORE_B        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.INT);
    // Damage
    private static final EntityDataAccessor<Float>   DAMAGE        = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float>   DAMAGE_RADIUS = SynchedEntityData.defineId(LightningStrikeEntity.class, EntityDataSerializers.FLOAT);

    // ── State ─────────────────────────────────────────────────────────────────

    /** Ticks the bolt takes to reach full brightness before dealing damage. */
    public static final int APPEAR_TICKS = 6;

    /** Increases 0 → APPEAR_TICKS on appear, decreases on disappear. Drives alpha. */
    public int appearTimer;
    public int prevAppearTimer;

    /** Set to false to begin the fade-out / discard sequence. */
    public boolean on = true;

    // ── Constructors ──────────────────────────────────────────────────────────

    public LightningStrikeEntity(EntityType<? extends LightningStrikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * Full convenience constructor.
     *
     * @param x / y / z      Impact point (bottom of the bolt, usually ground level).
     * @param height         How many blocks upward the bolt reaches.
     * @param duration       Ticks to stay fully visible after appearing.
     * @param glowR/G/B      Outer glow RGBA (0-255). Default: blue-white lightning.
     * @param coreR/G/B      Thin inner core color (0-255). Should be bright/white.
     * @param damage         AoE flat damage applied at the impact tick (0 = none).
     * @param damageRadius   Sphere radius around the impact point for AoE damage.
     */
    public LightningStrikeEntity(EntityType<? extends LightningStrikeEntity> type, Level level,
                                 double x, double y, double z,
                                 float height, int duration,
                                 int glowR, int glowG, int glowB,
                                 int coreR, int coreG, int coreB,
                                 float damage, float damageRadius) {
        this(type, level);
        this.setPos(x, y, z);
        this.setStrikeHeight(height);
        this.setDuration(duration);
        this.setGlowR(glowR); this.setGlowG(glowG); this.setGlowB(glowB);
        this.setCoreR(coreR); this.setCoreG(coreG); this.setCoreB(coreB);
        this.setDamage(damage);
        this.setDamageRadius(damageRadius);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // Save previous state for partial-tick interpolation
        prevAppearTimer = appearTimer;
        xo = getX();
        yo = getY();
        zo = getZ();

        // Advance the appear / disappear animation counter
        if (!on) {
            if (appearTimer > 0) {
                appearTimer--;
            } else {
                discard();
                return;
            }
        } else {
            // Ramp up until fully bright
            if (appearTimer < APPEAR_TICKS) appearTimer++;
        }

        // Expire after the configured duration
        if (tickCount > APPEAR_TICKS + getDuration()) {
            on = false;
        }

        // Deal AoE damage exactly once — on the first fully-lit tick
        if (!level().isClientSide()
                && tickCount == APPEAR_TICKS + 1
                && level() instanceof ServerLevel serverLevel) {
            applyImpactDamage(serverLevel);
        }
    }

    private void applyImpactDamage(ServerLevel server) {
        float dmg    = getDamage();
        float radius = getDamageRadius();
        if (dmg <= 0f || radius <= 0f) return;

        // Use lightningBolt damage source — swap for a custom DamageSource if needed
        DamageSource src = server.damageSources().lightningBolt();

        double r2 = radius * radius;
        AABB box = new AABB(
                getX() - radius, getY() - radius, getZ() - radius,
                getX() + radius, getY() + radius, getZ() + radius);

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = target.getX() - getX();
            double dy = target.getY() - getY();
            double dz = target.getZ() - getZ();
            if (dx * dx + dy * dy + dz * dz <= r2) {
                target.hurtServer(server, src, dmg);
            }
        }
    }

    // ── SynchedEntityData ─────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Defaults: a blue-white lightning bolt, 16 blocks tall, 20 tick lifetime
        builder.define(STRIKE_HEIGHT,  16f);
        builder.define(DURATION,        20);
        builder.define(GLOW_R,          80);
        builder.define(GLOW_G,         120);
        builder.define(GLOW_B,         255);
        builder.define(CORE_R,         220);
        builder.define(CORE_G,         235);
        builder.define(CORE_B,         255);
        builder.define(DAMAGE,           0f);
        builder.define(DAMAGE_RADIUS,    3f);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public float getStrikeHeight()         { return entityData.get(STRIKE_HEIGHT); }
    public void  setStrikeHeight(float v)  { entityData.set(STRIKE_HEIGHT, v); }

    public int   getDuration()             { return entityData.get(DURATION); }
    public void  setDuration(int v)        { entityData.set(DURATION, v); }

    public int   getGlowR()               { return entityData.get(GLOW_R); }
    public void  setGlowR(int v)          { entityData.set(GLOW_R, v); }
    public int   getGlowG()               { return entityData.get(GLOW_G); }
    public void  setGlowG(int v)          { entityData.set(GLOW_G, v); }
    public int   getGlowB()               { return entityData.get(GLOW_B); }
    public void  setGlowB(int v)          { entityData.set(GLOW_B, v); }

    public int   getCoreR()               { return entityData.get(CORE_R); }
    public void  setCoreR(int v)          { entityData.set(CORE_R, v); }
    public int   getCoreG()               { return entityData.get(CORE_G); }
    public void  setCoreG(int v)          { entityData.set(CORE_G, v); }
    public int   getCoreB()               { return entityData.get(CORE_B); }
    public void  setCoreB(int v)          { entityData.set(CORE_B, v); }

    public float getDamage()              { return entityData.get(DAMAGE); }
    public void  setDamage(float v)       { entityData.set(DAMAGE, v); }

    public float getDamageRadius()        { return entityData.get(DAMAGE_RADIUS); }
    public void  setDamageRadius(float v) { entityData.set(DAMAGE_RADIUS, v); }

    // ── Entity boilerplate ────────────────────────────────────────────────────

    @Override public boolean      shouldBeSaved()                              { return false; }
    @Override protected void      readAdditionalSaveData(ValueInput in)        {}
    @Override protected void      addAdditionalSaveData(ValueOutput out)       {}
    @Override public PushReaction  getPistonPushReaction()                     { return PushReaction.IGNORE; }
    @Override public boolean       isPickable()                                { return false; }
    @Override public boolean       isPushable()                                { return false; }
    @Override public boolean       shouldRenderAtSqrDistance(double d)        { return d < (512.0 * 512.0); }
    @Override public boolean       hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean       canCollideWith(Entity e)                    { return false; }
    @Override public boolean       canBeCollidedWith(Entity e)                 { return false; }
}