package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record VortexDarknessPacket(int durationTicks) implements CustomPacketPayload {

    public static final Type<VortexDarknessPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "vortex_darkness"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VortexDarknessPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, VortexDarknessPacket::durationTicks,
            VortexDarknessPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}