package net.filipes.rituals.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PharathornStillHandler {

    private static final Identifier ARMOR_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "pharathorn_standing_armor");

    private static final int DELAY_TICKS        = 60;
    private static final int TICKS_PER_ARMOR_POINT = 20;
    private static final int MAX_ARMOR_BONUS     = 20;

    private static final Map<UUID, Integer> ticksSinceAttack = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ticksSinceAttack.remove(handler.player.getUUID())
        );
    }

    private static void tick(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        boolean holdingPharathorn = mainHand.getItem() instanceof PharathornItem;

        if (!holdingPharathorn || ModDataComponents.getStage(mainHand) < 4) {
            removeModifier(player);
            ticksSinceAttack.remove(player.getUUID());
            return;
        }

        UUID id = player.getUUID();
        int ticks = ticksSinceAttack.getOrDefault(id, 0) + 1;
        ticksSinceAttack.put(id, ticks);

        int ticksPastDelay = Math.max(0, ticks - DELAY_TICKS);
        int armorBonus = Math.min(ticksPastDelay / TICKS_PER_ARMOR_POINT, MAX_ARMOR_BONUS);

        if (armorBonus > 0) {
            applyModifier(player, armorBonus);
        } else {
            removeModifier(player);
        }
    }

    public static void onAttack(ServerPlayer player) {
        ticksSinceAttack.put(player.getUUID(), 0);
        removeModifier(player);
    }

    private static void applyModifier(ServerPlayer player, int bonus) {
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;
        armorAttr.addOrUpdateTransientModifier(
                new AttributeModifier(ARMOR_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    private static void removeModifier(ServerPlayer player) {
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;
        armorAttr.removeModifier(ARMOR_MODIFIER_ID);
    }

    public static int getArmorBonus(ServerPlayer player) {
        UUID id = player.getUUID();
        int ticks = ticksSinceAttack.getOrDefault(id, 0);
        int ticksPastDelay = Math.max(0, ticks - DELAY_TICKS);
        return Math.min(ticksPastDelay / TICKS_PER_ARMOR_POINT, MAX_ARMOR_BONUS);
    }
}