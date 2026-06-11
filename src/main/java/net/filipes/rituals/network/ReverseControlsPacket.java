package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ReverseControlsPacket implements CustomPacketPayload {

    public static final Type<ReverseControlsPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "reverse_controls"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReverseControlsPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeInt(pkt.durationTicks),
                    buf -> new ReverseControlsPacket(buf.readInt())
            );

    public final int durationTicks;

    public ReverseControlsPacket(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}