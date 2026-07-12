package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PulseBlasterHeatPayload(float heatFraction, boolean overheated) implements CustomPacketPayload {

    public static final Type<PulseBlasterHeatPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_heat"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PulseBlasterHeatPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PulseBlasterHeatPayload::heatFraction,
            ByteBufCodecs.BOOL, PulseBlasterHeatPayload::overheated,
            PulseBlasterHeatPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}