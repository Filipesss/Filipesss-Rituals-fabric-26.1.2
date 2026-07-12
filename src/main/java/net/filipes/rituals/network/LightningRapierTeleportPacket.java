package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.TeleportTrailEntity;
import net.filipes.rituals.item.custom.LightningRapierItem;
import net.filipes.rituals.sound.ModSounds; // Added sound import
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource; // Added SoundSource import
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
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
    private static final double WALL_BUFFER       = 0.4; // back off this far from any hit surface

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LightningRapierTeleportPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
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

            ClipContext clipContext = new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            HitResult hit = level.clip(clipContext);

            Vec3 safeEnd = end;
            if (hit.getType() != HitResult.Type.MISS) {
                Vec3 hitLoc = hit.getLocation();
                double travelled = start.distanceTo(hitLoc);
                double safeDistance = Math.max(0.0, travelled - WALL_BUFFER);
                safeEnd = start.add(look.scale(safeDistance));
            }

            TeleportTrailEntity trail = new TeleportTrailEntity(
                    ModEntities.LIGHTNING_RAPIER_TELEPORT, level, player, start, safeEnd
            );
            level.addFreshEntity(trail);

            float pitch = 0.95F + level.getRandom().nextFloat() * 0.1F;
            level.playSound(null, start.x, start.y, start.z,
                    ModSounds.GENERIC_DASH, SoundSource.PLAYERS, 0.6F, pitch);

            player.teleportTo(safeEnd.x, player.getY(), safeEnd.z);
            player.setDeltaMovement(Vec3.ZERO);
        });
    }
}