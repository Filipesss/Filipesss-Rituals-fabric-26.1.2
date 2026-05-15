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

    private static final int TICKS_PER_ARMOR_POINT = 20; // 1 armor per second
    private static final int MAX_ARMOR_BONUS = 20;

    private static final Map<UUID, Integer> stillTicks = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tick(player);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                stillTicks.remove(handler.player.getUUID())
        );
    }

    private static void tick(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        boolean holdingPharathorn = mainHand.getItem() instanceof PharathornItem;

        if (!holdingPharathorn || ModDataComponents.getStage(mainHand) < 4) {
            removeModifier(player);
            stillTicks.remove(player.getUUID());
            return;
        }

        var delta = player.getDeltaMovement();
        boolean isStill = Math.abs(delta.x) < 0.001
                && Math.abs(delta.z) < 0.001
                && player.onGround();

        UUID id = player.getUUID();

        if (isStill) {
            int ticks = stillTicks.getOrDefault(id, 0) + 1;
            stillTicks.put(id, ticks);

            int armorBonus = Math.min(ticks / TICKS_PER_ARMOR_POINT, MAX_ARMOR_BONUS);
            applyModifier(player, armorBonus);
        } else {
            stillTicks.put(id, 0);
            removeModifier(player);
        }
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
        int ticks = stillTicks.getOrDefault(id, 0);
        return Math.min(ticks / TICKS_PER_ARMOR_POINT, MAX_ARMOR_BONUS);
    }
}
