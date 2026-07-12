package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.filipes.rituals.item.custom.DepthstrikeItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof DepthstrikeItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 2) return;
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