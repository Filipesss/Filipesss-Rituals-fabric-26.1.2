package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.SolarBladeChargeTracker;
import net.filipes.rituals.item.custom.SolarBladeItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SolarBladeChargePacket implements CustomPacketPayload {

    public static final Type<SolarBladeChargePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "solar_blade_charge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SolarBladeChargePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new SolarBladeChargePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SolarBladeChargePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof SolarBladeItem)) return;

            int stage = ModDataComponents.getStage(stack);
            if (stage < 4) return;

            SolarBladeChargeTracker.activate(player);
        });
    }
}