package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.Scalable;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer; // Added import
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

public class TemporalMuteMarkEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT = 5;
    public static final int   LIFETIME    = 120;
    public static final float QUAD_SIZE   = 1.4f;

    public static final float FRAME_SPEED_START = 1.0f / 6.0f;
    public static final float FRAME_SPEED_END   = 1.0f;

    private static final String GLOW_TEAM = "rituals_temporal_mute_marked";

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(TemporalMuteMarkEntity.class, EntityDataSerializers.FLOAT);

    private UUID targetUUID;
    private UUID ownerUUID;
    private boolean glowingApplied = false;
    private String appliedGlowTeamName = null;
    private String targetScoreboardName = null;

    private boolean lockMovement = false;
    private Vec3 lockPosition = null;

    public TemporalMuteMarkEntity(EntityType<? extends TemporalMuteMarkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public TemporalMuteMarkEntity(EntityType<? extends TemporalMuteMarkEntity> type, Level level,
                                  double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    public void setTargetUUID(UUID targetUUID) { this.targetUUID = targetUUID; }
    public void setOwnerUUID(UUID ownerUUID)   { this.ownerUUID = ownerUUID; }

    public void setMovementLocked(boolean lockMovement) { this.lockMovement = lockMovement; }

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
            boolean stillMuted = MuteTracker.isMuted(targetUUID);

            if (target == null || !target.isAlive() || !stillMuted) {
                cleanupGlow(serverLevel);
                discard();
                return;
            }

            if (!glowingApplied) {
                applyMuteGlow(target, serverLevel);
            }

            if (lockMovement && target instanceof ServerPlayer targetPlayer) {
                if (this.lockPosition == null) {
                    this.lockPosition = targetPlayer.position();
                }

                targetPlayer.teleportTo(lockPosition.x, lockPosition.y, lockPosition.z);
                targetPlayer.setDeltaMovement(Vec3.ZERO);
                targetPlayer.hurtMarked = true;

                setPos(lockPosition.x, lockPosition.y + targetPlayer.getBbHeight() * 0.5, lockPosition.z);
            } else {

                double x = target.getX();
                double y = target.getY() + target.getBbHeight() * 0.5;
                double z = target.getZ();
                setPos(x, y, z);
            }
        }

        xo = getX();
        yo = getY();
        zo = getZ();

        if (tickCount >= LIFETIME) {
            if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                cleanupGlow(serverLevel);
                spawnFizzleSparks(serverLevel);

                serverLevel.playSound(null,
                        getX(), getY(), getZ(),
                        SoundEvents.BEACON_DEACTIVATE,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        0.8f, 1.4f);
            }
            discard();
        }
    }

    private void spawnFizzleSparks(ServerLevel level) {
        double x = getX();
        double y = getY();
        double z = getZ();

        for (int i = 0; i < 8; i++) {
            Vec3 velocity = randomSphere(0.2 + Math.random() * 0.25);

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.TEMPORAL_MARK_END);
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

    private void applyMuteGlow(Entity target, ServerLevel serverLevel) {
        Scoreboard scoreboard = serverLevel.getServer().getScoreboard();
        String targetName = target.getScoreboardName();
        targetScoreboardName = targetName;

        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM);
            team.setColor(Optional.of(TeamColor.RED));
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