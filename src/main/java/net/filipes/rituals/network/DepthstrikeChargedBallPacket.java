package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.DepthstrikeChargedBallEntity;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.DepthstrikeItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DepthstrikeChargedBallPacket implements CustomPacketPayload {

    public static final Type<DepthstrikeChargedBallPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "depthstrike_charged_ball"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DepthstrikeChargedBallPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new DepthstrikeChargedBallPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DepthstrikeChargedBallPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof DepthstrikeItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 6) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            DepthstrikeChargedBallEntity ball =
                    new DepthstrikeChargedBallEntity(player.level(), player);
            player.level().addFreshEntity(ball);
        });
    }
}