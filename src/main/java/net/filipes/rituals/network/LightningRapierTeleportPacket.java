package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.TeleportTrailEntity;
import net.filipes.rituals.item.custom.LightningRapierItem;
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

public class LightningRapierTeleportPacket implements CustomPacketPayload {

    public static final Type<LightningRapierTeleportPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_teleport"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LightningRapierTeleportPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new LightningRapierTeleportPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long  COOLDOWN_MS        = 20_000L;
    public  static final float TELEPORT_DISTANCE  = 10.0f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LightningRapierTeleportPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof LightningRapierItem)) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            Vec3 look  = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 start = player.position();
            Vec3 end   = start.add(look.scale(TELEPORT_DISTANCE));

            ServerLevel level = (ServerLevel) player.level();

            TeleportTrailEntity trail = new TeleportTrailEntity(
                    ModEntities.LIGHTNING_RAPIER_TELEPORT, level, player, start, end
            );
            level.addFreshEntity(trail);

            player.teleportTo(end.x, player.getY(), end.z);
            player.setDeltaMovement(Vec3.ZERO);

        });
    }
}