package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.LunarBladeOnHitTracker;
import net.filipes.rituals.item.custom.LunarBladeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LunarBladeOnHitPacket implements CustomPacketPayload {

    public static final Type<LunarBladeOnHitPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "lunar_blade_on_hit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LunarBladeOnHitPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new LunarBladeOnHitPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LunarBladeOnHitPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof LunarBladeItem)) return;
            if (ModDataComponents.getStage(stack) < 4) return;
            LunarBladeOnHitTracker.activate(player);
        });
    }
}