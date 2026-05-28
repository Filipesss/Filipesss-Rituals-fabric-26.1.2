package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.CinderboltItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CinderboltTriplePacket implements CustomPacketPayload {

    public static final Type<CinderboltTriplePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "cinderbolt_triple"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CinderboltTriplePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new CinderboltTriplePacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CinderboltTriplePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof CinderboltItem)) return;
            if (ModDataComponents.getStage(stack) < 4) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            CinderboltItem.markTriple(uuid);
        });
    }
}