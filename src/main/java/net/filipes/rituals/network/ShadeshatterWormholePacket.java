package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.item.custom.ShadeshatterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadeshatterWormholePacket implements CustomPacketPayload {

    public static final Type<ShadeshatterWormholePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_wormhole"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterWormholePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBlockPos(pkt.targetPos),
                    buf -> new ShadeshatterWormholePacket(buf.readBlockPos())
            );

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final long COOLDOWN_MS = 25_000L;

    private final BlockPos targetPos;

    public ShadeshatterWormholePacket(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShadeshatterWormholePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();

        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ShadeshatterItem)) return;

            if (ShadeshatterWormholeHandler.isActive(player.getUUID())) return;

            UUID uuid = player.getUUID();
            long now = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            Vec3 center = Vec3.atBottomCenterOf(pkt.targetPos.above());
            if (player.distanceToSqr(center) > 24 * 24) return;

            ShadeshatterWormholeHandler.beginWormhole(player, center, ShadeshatterWormholeHandler.WORMHOLE_TICKS);
        });
    }
}