package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class TemporalRecallStartCooldownPacket implements CustomPacketPayload {

    public static final Type<TemporalRecallStartCooldownPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_recall_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalRecallStartCooldownPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalRecallStartCooldownPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}