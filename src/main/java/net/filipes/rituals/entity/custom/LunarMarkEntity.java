package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.Scalable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.Optional;
import java.util.UUID;

public class LunarMarkEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT  = 5;
    public static final int   LIFETIME     = 60;
    public static final float QUAD_SIZE    = 1.6f;

    public static final float FRAME_SPEED_START = 1.0f / 6.0f;
    public static final float FRAME_SPEED_END   = 1.0f;

    private static final String GLOW_TEAM = "rituals_lunar_marked";

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(LunarMarkEntity.class, EntityDataSerializers.FLOAT);

    private UUID    targetUUID;
    private boolean glowingApplied = false;
    private UUID ownerUUID;
    private String appliedGlowTeamName = null;
    private String targetScoreboardName = null;
    private boolean isChainMark = false;
    public void setChainMark(boolean v) { this.isChainMark = v; }
    private static final int MAX_CHAIN_DEPTH = 5;
    private int chainDepth = 0;

    public void setChainDepth(int d) { this.chainDepth = d; }
    public int  getChainDepth()      { return chainDepth; }

    public LunarMarkEntity(EntityType<? extends LunarMarkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LunarMarkEntity(EntityType<? extends LunarMarkEntity> type, Level level,
                           double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }
    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }


    public static float computeFrameAccumulator(int tick) {
        float t = tick;
        return FRAME_SPEED_START * t
                + (FRAME_SPEED_END - FRAME_SPEED_START) / LIFETIME * t * t * 0.5f;
    }

    public int getCurrentFrame() {
        return (int) computeFrameAccumulator(tickCount) % FRAME_COUNT;
    }


    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && targetUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUUID);

            if (target == null || !target.isAlive()) {
                cleanupGlow(serverLevel);
                discard();
                return;
            }

            if (!glowingApplied) {
                applyLunarGlow(target, serverLevel);
                glowingApplied = true;
            }

            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();
            setPos(x, y, z);
        }

        xo = getX();
        yo = getY();
        zo = getZ();

        if (tickCount >= LIFETIME) {
            if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                cleanupGlow(serverLevel);
                spawnExplosionSparks(serverLevel);
                if (chainDepth < MAX_CHAIN_DEPTH) {
                    spawnExplosionFragments(serverLevel);
                }
                serverLevel.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.RESPAWN_ANCHOR_DEPLETE,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        1.0f, 0.4f);
            }
            discard();
        }
    }
    private void spawnExplosionSparks(ServerLevel level) {
        double x = getX();
        double y = getY();
        double z = getZ();

        for (int i = 0; i < 10; i++) {

            Vec3 velocity = randomSphere(0.25 + Math.random() * 0.25);

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.LUNAR_MARK_START);

            spark.forcedVelocity = velocity;
            spark.setDeltaMovement(velocity);

            level.addFreshEntity(spark);
        }
    }

    private static Vec3 randomSphere(double speed) {
        double theta = Math.random() * Math.PI * 2.0;
        double phi = Math.acos(2.0 * Math.random() - 1.0);
        return new Vec3(
                Math.sin(phi) * Math.cos(theta) * speed,
                Math.cos(phi) * speed,
                Math.sin(phi) * Math.sin(theta) * speed
        );
    }
    private static Vec3 randomSpread(double amount) {
        return new Vec3(
                (Math.random() - 0.5) * amount,
                (Math.random() - 0.5) * amount,
                (Math.random() - 0.5) * amount);
    }

    private void applyLunarGlow(Entity target, ServerLevel serverLevel) {
        Scoreboard scoreboard = serverLevel.getServer().getScoreboard();
        String targetName = target.getScoreboardName();
        targetScoreboardName = targetName;

        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM);
            team.setColor(Optional.of(TeamColor.AQUA));
            team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.NEVER);
        }

        PlayerTeam currentTeam = scoreboard.getPlayersTeam(targetName);
        if (currentTeam != null && currentTeam != team) {
            scoreboard.removePlayerFromTeam(targetName, currentTeam);
        }

        scoreboard.addPlayerToTeam(targetName, team);
        target.setGlowingTag(true);

        appliedGlowTeamName = team.getName();
        glowingApplied = true;
    }

    private void cleanupGlow(ServerLevel serverLevel) {
        if (!glowingApplied) return;

        Scoreboard scoreboard = serverLevel.getServer().getScoreboard();
        Entity target = targetUUID != null ? serverLevel.getEntity(targetUUID) : null;

        String targetName = targetScoreboardName;
        if (targetName == null && target != null) {
            targetName = target.getScoreboardName();
        }

        if (targetName != null && appliedGlowTeamName != null) {
            PlayerTeam currentTeam = scoreboard.getPlayersTeam(targetName);

            if (currentTeam != null && appliedGlowTeamName.equals(currentTeam.getName())) {
                scoreboard.removePlayerFromTeam(targetName, currentTeam);
            }

            if (target != null && scoreboard.getPlayersTeam(targetName) == null) {
                target.setGlowingTag(false);
            }
        } else if (target != null) {
            target.setGlowingTag(false);
        }

        glowingApplied = false;
        appliedGlowTeamName = null;
        targetScoreboardName = null;
    }
    private void spawnExplosionFragments(ServerLevel level) {
        double x = getX(), y = getY(), z = getZ();

        Entity markedEnt = targetUUID != null ? level.getEntity(targetUUID) : null;
        if (markedEnt instanceof LivingEntity markedLiving && markedLiving.isAlive()) {
            double ex = markedLiving.getX();
            double ey = markedLiving.getY();
            double ez = markedLiving.getZ();
            double height = markedLiving.getBbHeight();

            for (int i = 0; i < 2; i++) {
                double ox = (Math.random() - 0.5) * 1.8;
                double oz = (Math.random() - 0.5) * 1.8;
                double oy = height + 1.4 + Math.random() * 0.8;

                Vec3 awayFromEnemy = new Vec3(ox, 0.4, oz).normalize().scale(0.18);

                LunarFragmentEntity shard = new LunarFragmentEntity(ModEntities.LUNAR_FRAGMENT, level);
                shard.setPos(ex + ox, ey + oy, ez + oz);
                shard.initAsShard(false, awayFromEnemy, targetUUID, ownerUUID);
                shard.setShardInwardTarget(markedLiving);
                shard.setShardChainDepth(this.chainDepth);
                level.addFreshEntity(shard);
            }
        }

        java.util.concurrent.atomic.AtomicBoolean chainClaim = new java.util.concurrent.atomic.AtomicBoolean(false);

        for (int i = 0; i < 2; i++) {
            Vec3 vel = randomSphere(0.30 + Math.random() * 0.16);
            LunarFragmentEntity shard = new LunarFragmentEntity(ModEntities.LUNAR_FRAGMENT, level);
            shard.setPos(x, y, z);
            shard.initAsShard(true, vel, targetUUID, ownerUUID);
            shard.setShardChainDepth(this.chainDepth);
            shard.setChainClaim(chainClaim);
            level.addFreshEntity(shard);
        }
    }


    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(DATA_SCALE, 1.0f); }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)  {}
    @Override protected void addAdditionalSaveData(ValueOutput out) {}
    @Override public PushReaction getPistonPushReaction()           { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                           { return false; }
    @Override public boolean isPushable()                           { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)    { return d < (256.0 * 256.0); }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)               { return false; }
    @Override public boolean canBeCollidedWith(Entity e)            { return false; }

    @Override public float getEntityScale()        { return entityData.get(DATA_SCALE); }
    @Override public void  setEntityScale(float s) { entityData.set(DATA_SCALE, s); }
}