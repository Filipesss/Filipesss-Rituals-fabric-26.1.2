package net.filipes.rituals.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.filipes.rituals.client.TemporalMuteHudOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class TemporalMuteClearPacket implements CustomPacketPayload {

    public static final Type<TemporalMuteClearPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_mute_clear"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalMuteClearPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalMuteClearPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (packet, context) ->
                TemporalMuteHudOverlay.clear());
    }
}