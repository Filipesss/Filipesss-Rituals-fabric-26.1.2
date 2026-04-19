package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FireDeathLaserPacket() implements CustomPacketPayload {
    public static final Type<FireDeathLaserPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("rituals", "fire_death_laser"));

    // An empty codec since we don't need to send any extra data (the server knows who sent it)
    public static final StreamCodec<RegistryFriendlyByteBuf, FireDeathLaserPacket> CODEC = StreamCodec.unit(new FireDeathLaserPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}