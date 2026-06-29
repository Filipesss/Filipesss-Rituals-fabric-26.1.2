package net.filipes.rituals.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShadeshatterHastePacket(float tickRate) implements CustomPacketPayload {

    public static final Type<ShadeshatterHastePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_haste"));

    public static final StreamCodec<ByteBuf, ShadeshatterHastePacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ShadeshatterHastePacket::tickRate,
                    ShadeshatterHastePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}