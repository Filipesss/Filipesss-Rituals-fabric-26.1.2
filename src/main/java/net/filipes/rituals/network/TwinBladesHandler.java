package net.filipes.rituals.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.item.custom.LunarBladeItem;
import net.filipes.rituals.item.custom.SolarBladeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents; // Added sound event import
import net.minecraft.sounds.SoundSource; // Added sound source import
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
                if (stage < 3) continue;

                ServerLevel level = (ServerLevel) player.level();
                long timeOfDay = level.getDefaultClockTime() % 24000L;
                boolean isDay = timeOfDay < 13000L;
                boolean isNight = timeOfDay >= 13000L;

                if (hasSolar && isDay) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModStatusEffects.SUNBLESSED, 120, 0, false, false, true));
                }

                if (hasLunar && isNight) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModStatusEffects.MOONSHINE, 120, 0, false, false, true));
                }
            }
        });
    }

    public static float getDamageMultiplier(Player player) {
        if (!isTwinPairEquipped(player)) return 1.0f;

        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();
        boolean currentIsSolar = main.getItem() instanceof SolarBladeItem;

        int mainStage = ModDataComponents.getStage(main);
        int offStage  = ModDataComponents.getStage(off);
        boolean bothStageSix = mainStage >= 6 && offStage >= 6;

        float multiplier = 1.5f;

        if (bothStageSix) {
            UUID uuid = player.getUUID();
            Boolean lastSolar = lastWasSolar.get(uuid);
            boolean isAlternate = lastSolar != null && lastSolar != currentIsSolar;

            if (isAlternate) {
                multiplier = 1.75f;
            }

            lastWasSolar.put(uuid, currentIsSolar);
        }

        if (!player.level().isClientSide()) {
            float pitchVariation = (player.getRandom().nextFloat() - 0.5f) * 0.13f;

            if (multiplier == 1.75f) {
                player.level().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.65f,
                        1.05f + pitchVariation
                );
            } else if (multiplier == 1.5f) {
                player.level().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_CLUSTER_HIT,
                        SoundSource.PLAYERS,
                        0.65f,
                        1.05f + pitchVariation
                );
            }
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