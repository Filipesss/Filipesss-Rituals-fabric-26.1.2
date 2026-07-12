package net.filipes.rituals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ShadowguardInvisiblePacket(UUID targetUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShadowguardInvisiblePacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("rituals", "shadowguard_invisible"));

    public static final StreamCodec<FriendlyByteBuf, ShadowguardInvisiblePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.targetUUID()),
                    buf -> new ShadowguardInvisiblePacket(buf.readUUID())
            );

    @Override
    public CustomPacketPayload.Type<ShadowguardInvisiblePacket> type() {
        return TYPE;
    }
}