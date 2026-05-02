package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.DashStabEntity;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PharathornDashPacket implements CustomPacketPayload {

    public static final Type<PharathornDashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pharathorn_dash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharathornDashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PharathornDashPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long  COOLDOWN_MS   = 25_000L;
    public  static final float DASH_DISTANCE = 7.0f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PharathornDashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof PharathornItem)) return;
            if (ModDataComponents.getStage(held) < 2) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            Vec3 look  = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 start = player.position();
            Vec3 end   = start.add(look.scale(DASH_DISTANCE));

            ServerLevel level = (ServerLevel) player.level();
            DashStabEntity trail = new DashStabEntity(
                    ModEntities.DASH_STAB, level, player, start, end
            );
            level.addFreshEntity(trail);

            player.setDeltaMovement(look.scale(1.8));
            player.hurtMarked = true;
        });
    }
}