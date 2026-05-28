package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class TwinsStartCooldownPacket implements CustomPacketPayload {

    public static final Type<TwinsStartCooldownPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "twins_start_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TwinsStartCooldownPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TwinsStartCooldownPacket());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}