package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShadeshatterAbilityResetPacket() implements CustomPacketPayload {

    public static final Type<ShadeshatterAbilityResetPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_ability_reset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterAbilityResetPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterAbilityResetPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}