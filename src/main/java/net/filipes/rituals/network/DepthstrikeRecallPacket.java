package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DepthstrikeRecallPacket implements CustomPacketPayload {

    public static final Type<DepthstrikeRecallPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "depthstrike_recall"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DepthstrikeRecallPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new DepthstrikeRecallPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DepthstrikeRecallPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            AABB searchBox = player.getBoundingBox().inflate(512);
            List<ThrownDepthstrikeEntity> owned = player.level()
                    .getEntitiesOfClass(ThrownDepthstrikeEntity.class, searchBox,
                            e -> e.getOwner() != null
                                    && e.getOwner().getUUID().equals(player.getUUID()));
            for (ThrownDepthstrikeEntity trident : owned) {
                trident.recallNow();
            }
        });
    }
}