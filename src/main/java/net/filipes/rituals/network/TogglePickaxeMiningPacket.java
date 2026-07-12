package net.filipes.rituals.network;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record TogglePickaxeMiningPacket() implements CustomPacketPayload {

    public static final Type<TogglePickaxeMiningPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("rituals", "toggle_pickaxe_mining")
    );
    public static final StreamCodec<FriendlyByteBuf, TogglePickaxeMiningPacket> CODEC =
            StreamCodec.unit(new TogglePickaxeMiningPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TogglePickaxeMiningPacket packet, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof RosegoldPickaxeItem)) return;
        if (RosegoldPickaxeItem.getStage(stack) < 4) return;

        boolean current = stack.getOrDefault(ModDataComponents.MINING_ENABLED, true);
        stack.set(ModDataComponents.MINING_ENABLED, !current);
    }
}