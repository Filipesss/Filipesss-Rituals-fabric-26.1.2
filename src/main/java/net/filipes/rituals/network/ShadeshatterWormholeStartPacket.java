package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShadeshatterWormholeStartPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ShadeshatterWormholeStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_wormhole_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterWormholeStartPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShadeshatterWormholeStartPacket::entityId,
                    ShadeshatterWormholeStartPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}