package net.filipes.rituals.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.item.custom.LunarBladeItem;
import net.filipes.rituals.item.custom.SolarBladeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TwinBladesHandler {

    private static final Map<UUID, Boolean> lastWasSolar = new HashMap<>();
    private static final Set<UUID> alternatePending = new HashSet<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();
                ItemStack off  = player.getOffhandItem();

                boolean hasSolar = main.getItem() instanceof SolarBladeItem || off.getItem() instanceof SolarBladeItem;
                boolean hasLunar = main.getItem() instanceof LunarBladeItem || off.getItem() instanceof LunarBladeItem;

                ItemStack bladeStack =
                        main.getItem() instanceof SolarBladeItem || main.getItem() instanceof LunarBladeItem
                                ? main
                                : off;

                int stage = ModDataComponents.getStage(bladeStack);
                if (stage < 5) continue;

                ServerLevel level = (ServerLevel) player.level();
                long timeOfDay = level.getDefaultClockTime() % 24000L;
                boolean isDay = timeOfDay < 13000L;
                boolean isNight = timeOfDay >= 13000L;

                if (hasSolar && isDay) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModStatusEffects.SUNBLESSED, 60, 0, false, false, true));
                }

                if (hasLunar && isNight) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModStatusEffects.MOONSHINE, 60, 0, false, false, true));
                }
            }
        });
    }

    public static float getDamageMultiplier(Player player) {
        if (!isTwinPairEquipped(player)) return 1.0f;

        ItemStack main = player.getMainHandItem();
        boolean currentIsSolar = main.getItem() instanceof SolarBladeItem;
        int stage = ModDataComponents.getStage(main);

        float multiplier = 1.5f;

        if (stage >= 6) {
            UUID uuid = player.getUUID();
            Boolean lastSolar = lastWasSolar.get(uuid);
            boolean isAlternate = lastSolar != null && lastSolar != currentIsSolar;

            if (alternatePending.contains(uuid) && isAlternate) {
                multiplier = 2.0f;
                alternatePending.remove(uuid);
            } else {
                alternatePending.add(uuid);
            }

            lastWasSolar.put(uuid, currentIsSolar);
        }

        return multiplier;
    }

    public static boolean isTwinPairEquipped(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();

        return (main.getItem() instanceof SolarBladeItem && off.getItem() instanceof LunarBladeItem)
                || (main.getItem() instanceof LunarBladeItem && off.getItem() instanceof SolarBladeItem);
    }
}