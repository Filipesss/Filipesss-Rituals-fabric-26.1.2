package net.filipes.rituals.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.filipes.rituals.component.ModDataComponents;

import java.util.*;

public class TemporalGlassreaverHandler {


    private static final Map<UUID, Integer> critCounts = new HashMap<>();
    private static final Set<UUID> critModeSet = new HashSet<>();
    static final Set<UUID> swungThisTick = new HashSet<>();
    static final Set<UUID> attackedThisTick = new HashSet<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (UUID id : swungThisTick) {
                if (!attackedThisTick.contains(id)) {
                    reset(id);
                }
            }
            swungThisTick.clear();
            attackedThisTick.clear();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID id = player.getUUID();
                net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                boolean isGlassreaver = stack.getItem() instanceof TemporalGlassreaverItem;

                if ((critCounts.containsKey(id) || critModeSet.contains(id)) && !isGlassreaver) {
                    reset(id);
                }

                if (isGlassreaver) {
                    boolean inCritMode = critModeSet.contains(id);
                    var currentData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
                    boolean hasCritTag = currentData != null && currentData.strings().contains("crit");

                    if (inCritMode && !hasCritTag) {
                        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                                new net.minecraft.world.item.component.CustomModelData(
                                        java.util.List.of(), java.util.List.of(), java.util.List.of("crit"), java.util.List.of()
                                )
                        );
                    } else if (!inCritMode && hasCritTag) {
                        stack.remove(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA);
                    }
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                reset(handler.player.getUUID())
        );
    }

    public static void recordSwing(UUID id) {
        if (critCounts.getOrDefault(id, 0) > 0 || critModeSet.contains(id)) {
            swungThisTick.add(id);
        }
    }

    public static void recordAttack(UUID id) {
        attackedThisTick.add(id);
    }

    public static void onHit(ServerPlayer player, boolean naturalCrit) {
        UUID id = player.getUUID();

        if (critModeSet.contains(id)) {
            return;
        }

        net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof TemporalGlassreaverItem)) {
            return;
        }

        int stage = ModDataComponents.getStage(stack);
        int critsNeeded = (stage >= 4) ? 3 : 4;

        if (naturalCrit) {
            int count = critCounts.getOrDefault(id, 0) + 1;
            if (count >= critsNeeded) {
                critModeSet.add(id);
                critCounts.remove(id);

                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                critCounts.put(id, count);
            }
        } else {
            critCounts.put(id, 0);
        }
    }

    public static boolean isInCritMode(UUID id) {
        return critModeSet.contains(id);
    }

    public static int getCritCount(UUID id) {
        return critCounts.getOrDefault(id, 0);
    }

    private static void reset(UUID id) {
        critCounts.remove(id);
        critModeSet.remove(id);
    }
}