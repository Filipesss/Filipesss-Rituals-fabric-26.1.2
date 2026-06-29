package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.TemporalRecallEntity;
import net.filipes.rituals.entity.custom.TemporalRecallTracker;
import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class TemporalRecallPacket implements CustomPacketPayload {

    public static final Type<TemporalRecallPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_recall"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalRecallPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalRecallPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TemporalRecallPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();

        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof TemporalGlassreaverItem)) return;

            if (ModDataComponents.getStage(stack) < 5) return;

            TemporalRecallTracker.Phase phase = TemporalRecallTracker.getPhase(player.getUUID());

            if (phase == TemporalRecallTracker.Phase.NONE) {
                ServerLevel level = (ServerLevel) player.level();
                Vec3 pos = player.position();

                TemporalRecallEntity clone = new TemporalRecallEntity(
                        ModEntities.TEMPORAL_RECALL, level, player, pos);
                clone.setPos(pos.x, pos.y, pos.z);
                level.addFreshEntity(clone);

                TemporalRecallTracker.onClonePlaced(player.getUUID(), clone.getUUID());

            } else if (phase == TemporalRecallTracker.Phase.CLONE_ACTIVE) {
                TemporalRecallTracker.triggerEarlyRecall(player.getUUID(), (ServerLevel) player.level());
            }
        });
    }
}