package net.filipes.rituals.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import io.netty.buffer.ByteBuf;

public record DoubleJumpPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DoubleJumpPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("rituals", "double_jump"));

    public static final StreamCodec<ByteBuf, DoubleJumpPayload> CODEC =
            StreamCodec.unit(new DoubleJumpPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}