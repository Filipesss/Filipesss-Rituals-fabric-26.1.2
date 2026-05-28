package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class CinderboltSaveTriggeredPacket implements CustomPacketPayload {

    public static final Type<CinderboltSaveTriggeredPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "cinderbolt_save_triggered"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CinderboltSaveTriggeredPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new CinderboltSaveTriggeredPacket());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void send(ServerPlayer player) {
        ServerPlayNetworking.send(player, new CinderboltSaveTriggeredPacket());
    }
}