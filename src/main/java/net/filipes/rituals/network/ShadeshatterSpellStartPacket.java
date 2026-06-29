package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ShadeshatterSpellStartPacket implements CustomPacketPayload {

    public static final Type<ShadeshatterSpellStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_spell_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterSpellStartPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterSpellStartPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}