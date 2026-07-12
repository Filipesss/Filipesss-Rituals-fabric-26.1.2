package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShadeshatterMimicStartPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ShadeshatterMimicStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_mimic_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterMimicStartPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShadeshatterMimicStartPacket::entityId,
                    ShadeshatterMimicStartPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}