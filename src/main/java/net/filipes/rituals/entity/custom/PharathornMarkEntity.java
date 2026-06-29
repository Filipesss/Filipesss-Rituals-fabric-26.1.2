package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.Scalable;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PharathornMarkEntity extends Entity implements Scalable {

    public static final int   FRAME_COUNT    = 5;
    public static final int   FRAME_DURATION = 2;
    public static final float QUAD_SIZE      = 2.0f;

    private static final String GLOW_TEAM   = "rituals_pharathorn_marked";
    private static final double CHECK_RANGE = 32.0;
    private String appliedGlowTeamName = null;
    private String targetScoreboardName = null;

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(PharathornMarkEntity.class, EntityDataSerializers.FLOAT);

    public UUID targetUUID;
    private boolean glowingApplied  = false;

    private int forcedTicksLeft = 0;

    public PharathornMarkEntity(EntityType<? extends PharathornMarkEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public PharathornMarkEntity(EntityType<? extends PharathornMarkEntity> type, Level level,
                                double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    public void setTargetUUID(UUID uuid)   { this.targetUUID = uuid; }

    public void setForced(int ticks)       { this.forcedTicksLeft = ticks; }
    public boolean isForced()              { return forcedTicksLeft > 0; }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && targetUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUUID);

            if (target == null || !target.isAlive()) {
                cleanupGlow(serverLevel);
                PharathornMarkTracker.unmark(targetUUID);
                discard();
                return;
            }

            if (forcedTicksLeft > 0) {
                forcedTicksLeft--;
                if (forcedTicksLeft == 0) {
                    if (!conditionsMet(target, serverLevel)) {
                        cleanupGlow(serverLevel);
                        PharathornMarkTracker.unmark(targetUUID);
                        discard();
                        return;
                    }
                }
            } else {
                if (!conditionsMet(target, serverLevel)) {
                    cleanupGlow(serverLevel);
                    PharathornMarkTracker.unmark(targetUUID);
                    discard();
                    return;
                }
            }

            if (!glowingApplied) {
                applyRedGlow(target, serverLevel);
                glowingApplied = true;
            }

            setPos(target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ());
        }

        xo = getX();
        yo = getY();
        zo = getZ();
    }

    private boolean conditionsMet(Entity target, ServerLevel serverLevel) {
        if (!(target instanceof LivingEntity living)) return false;
        if (living.getHealth() > living.getMaxHealth() * 0.5f) return false;
        AABB searchBox = target.getBoundingBox().inflate(CHECK_RANGE);
        List<ServerPlayer> holders = serverLevel.getEntitiesOfClass(
                ServerPlayer.class, searchBox,
                p -> p.getMainHandItem().getItem() instanceof PharathornItem);
        return !holders.isEmpty();
    }

    private void applyRedGlow(Entity target, ServerLevel serverLevel) {
        Scoreboard scoreboard = serverLevel.getServer().getScoreboard();
        String targetName = target.getScoreboardName();
        targetScoreboardName = targetName;

        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM);
            team.setColor(Optional.of(TeamColor.RED));
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

    public int getCurrentFrame() { return (tickCount / FRAME_DURATION) % FRAME_COUNT; }
    public UUID getTargetUUID() { return targetUUID; }

    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { b.define(DATA_SCALE, 1.0f); }
    @Override public boolean shouldBeSaved()                                 { return false; }
    @Override protected void readAdditionalSaveData(ValueInput in)           {}
    @Override protected void addAdditionalSaveData(ValueOutput out)          {}
    @Override public PushReaction getPistonPushReaction()                    { return PushReaction.IGNORE; }
    @Override public boolean isPickable()                                    { return false; }
    @Override public boolean isPushable()                                    { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d)             { return d < (256.0 * 256.0); }
    @Override public boolean hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean canCollideWith(Entity e)                        { return false; }
    @Override public boolean canBeCollidedWith(Entity e)                     { return false; }


    @Override public float getEntityScale()        { return entityData.get(DATA_SCALE); }
    @Override public void  setEntityScale(float s) { entityData.set(DATA_SCALE, s); }
}