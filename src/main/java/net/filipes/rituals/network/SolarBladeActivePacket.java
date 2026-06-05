package net.filipes.rituals.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.filipes.rituals.client.SolarBladeHudOverlay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class SolarBladeActivePacket implements CustomPacketPayload {

    public static final Type<SolarBladeActivePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "solar_blade_active"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SolarBladeActivePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.playerUUID),
                    buf -> new SolarBladeActivePacket(buf.readUUID())
            );

    public final UUID playerUUID;

    public SolarBladeActivePacket(UUID playerUUID) { this.playerUUID = playerUUID; }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (packet, context) -> {
            if (context.player().getUUID().equals(packet.playerUUID)) {
                SolarBladeHudOverlay.trigger();
            }
        });
    }
}