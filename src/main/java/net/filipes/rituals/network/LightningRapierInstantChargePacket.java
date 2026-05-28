package net.filipes.rituals.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LightningRapierInstantChargePacket() implements CustomPacketPayload {

    public static final Type<LightningRapierInstantChargePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_instant_charge"));

    public static final StreamCodec<ByteBuf, LightningRapierInstantChargePacket> CODEC =
            StreamCodec.unit(new LightningRapierInstantChargePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}