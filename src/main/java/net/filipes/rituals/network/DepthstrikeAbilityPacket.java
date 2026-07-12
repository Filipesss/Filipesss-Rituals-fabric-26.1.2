package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.filipes.rituals.item.ModItems;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DepthstrikeAbilityPacket implements CustomPacketPayload {

    public static final Type<DepthstrikeAbilityPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "depthstrike_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DepthstrikeAbilityPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new DepthstrikeAbilityPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 30_000L;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DepthstrikeAbilityPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (held.getItem() != ModItems.DEPTHSTRIKE) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 3) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ThrownDepthstrikeEntity.CHARGED_PLAYERS.add(uuid);
        });
    }
}