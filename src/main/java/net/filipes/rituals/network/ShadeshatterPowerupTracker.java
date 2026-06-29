package net.filipes.rituals.network;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShadeshatterPowerupTracker {

    private static final Map<UUID, ShadeshatterPowerup> ACTIVE         = new HashMap<>();
    private static final Set<UUID>                       FIRST_STRIKE   = new HashSet<>();
    private static final Set<UUID>                       RESET_AVAILABLE = new HashSet<>();
    private static final Set<UUID>                       GUARD          = new HashSet<>();

    // ── Powerup lifecycle ─────────────────────────────────────────────────────

    public static void applyPowerup(ServerPlayer player, ShadeshatterPowerup powerup) {
        ACTIVE.put(player.getUUID(), powerup);
        powerup.apply(player);
    }

    public static void removePowerup(ServerPlayer player) {
        ShadeshatterPowerup powerup = ACTIVE.remove(player.getUUID());
        if (powerup != null) {
            powerup.remove(player);
        }
        FIRST_STRIKE.remove(player.getUUID());
        RESET_AVAILABLE.remove(player.getUUID());
    }

    public static @Nullable ShadeshatterPowerup getActivePowerup(UUID uuid) {
        return ACTIVE.get(uuid);
    }

    // ── First-strike ──────────────────────────────────────────────────────────

    public static void resetFirstStrike(UUID uuid)      { FIRST_STRIKE.add(uuid); }
    public static void clearFirstStrike(UUID uuid)      { FIRST_STRIKE.remove(uuid); }
    public static boolean consumeFirstStrike(UUID uuid) { return FIRST_STRIKE.remove(uuid); }

    // ── Rapid-reset ───────────────────────────────────────────────────────────

    public static void setResetAvailable(UUID uuid, boolean available) {
        if (available) RESET_AVAILABLE.add(uuid);
        else           RESET_AVAILABLE.remove(uuid);
    }

    /** Returns true and consumes the flag so it can only trigger once per morph. */
    public static boolean consumeReset(UUID uuid) { return RESET_AVAILABLE.remove(uuid); }

    // ── Recursion guard ───────────────────────────────────────────────────────

    public static boolean isGuarded(UUID uuid) { return GUARD.contains(uuid); }
    public static void guard(UUID uuid)        { GUARD.add(uuid); }
    public static void unguard(UUID uuid)      { GUARD.remove(uuid); }
}