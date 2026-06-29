package net.filipes.rituals.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.filipes.rituals.client.TemporalMuteHudOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class TemporalMuteActivePacket implements CustomPacketPayload {

    public static final Type<TemporalMuteActivePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_mute_active"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalMuteActivePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.targetUUID),
                    buf -> new TemporalMuteActivePacket(buf.readUUID())
            );

    public final UUID targetUUID;

    public TemporalMuteActivePacket(UUID targetUUID) { this.targetUUID = targetUUID; }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (packet, context) -> {
            if (context.player().getUUID().equals(packet.targetUUID)) {
                TemporalMuteHudOverlay.trigger();
            }
        });
    }
}