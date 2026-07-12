package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public record ShadeshatterWormholeTriggerPacket() implements CustomPacketPayload {

    public static final Type<ShadeshatterWormholeTriggerPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_wormhole_trigger"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterWormholeTriggerPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterWormholeTriggerPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShadeshatterWormholeTriggerPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ShadeshatterWormholeStartPacket startPkt = new ShadeshatterWormholeStartPacket(player.getId());
            ServerPlayNetworking.send(player, startPkt);
            for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(tracker, startPkt);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 1.0f, 1.0f);
        });
    }
}