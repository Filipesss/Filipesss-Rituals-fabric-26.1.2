package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.Scalable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.UUID;

public class LifestealMarkEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT     = 5;
    public static final int   LOOP_COUNT      = 6;
    public static final int   FRAME_DURATION  = 2;
    public static final int   LIFETIME        = FRAME_COUNT * LOOP_COUNT * FRAME_DURATION;
    public static final float QUAD_SIZE       = 1.6f;

    private static final String GLOW_TEAM = "rituals_marked";

    private String appliedGlowTeamName = null;
    private String targetScoreboardName = null;



    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(LifestealMarkEntity.class, EntityDataSerializers.FLOAT);

    private UUID    targetUUID;
    private boolean glowingApplied = false;

    public LifestealMarkEntity(EntityType<? extends LifestealMarkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LifestealMarkEntity(EntityType<? extends LifestealMarkEntity> type, Level level,
                               double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
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
                applyGreenGlow(target, serverLevel);
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
            }
            discard();
        }
    }

    private void applyGreenGlow(Entity target, ServerLevel serverLevel) {
        Scoreboard scoreboard = serverLevel.getServer().getScoreboard();
        String targetName = target.getScoreboardName();
        targetScoreboardName = targetName;

        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM);
            team.setColor(ChatFormatting.AQUA);
            team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.NEVER);
        }

        // If the target is already on a different glow team, move it cleanly.
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

            // Only remove if the target is still on the same team this mark applied.
            if (currentTeam != null && appliedGlowTeamName.equals(currentTeam.getName())) {
                scoreboard.removePlayerFromTeam(targetName, currentTeam);
            }

            // Only clear the glow flag if there is no other team still owning it.
            if (target != null && scoreboard.getPlayersTeam(targetName) == null) {
                target.setGlowingTag(false);
            }
        } else if (target != null) {
            // Fallback: if we don't know the team anymore, don't crash; just clear glow safely.
            target.setGlowingTag(false);
        }

        glowingApplied = false;
        appliedGlowTeamName = null;
        targetScoreboardName = null;
    }
    public int getCurrentFrame() {
        return (tickCount / FRAME_DURATION) % FRAME_COUNT;
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

    @Override public float getEntityScale()       { return entityData.get(DATA_SCALE); }
    @Override public void  setEntityScale(float s){ entityData.set(DATA_SCALE, s); }
}