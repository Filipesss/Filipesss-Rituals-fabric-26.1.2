package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.DepthstrikeGroundEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class DepthstrikeGroundAbilityPacket implements CustomPacketPayload {

    public static final Type<DepthstrikeGroundAbilityPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "depthstrike_ground_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DepthstrikeGroundAbilityPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new DepthstrikeGroundAbilityPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long   COOLDOWN_MS   = 25_000L;

    private static final int    SPIKE_COUNT   = 6;
    private static final double FIRST_DIST    = 2.0;  // blocks ahead of player
    private static final double SPIKE_SPACING = 2.0;  // blocks between spikes
    private static final int    TICK_STAGGER  = 5;    // ticks between each spike emerging

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DepthstrikeGroundAbilityPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            if (player.getMainHandItem().getItem() != ModItems.DEPTHSTRIKE) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            float  yawRad = (float) Math.toRadians(player.getYRot());
            double dx     = -Math.sin(yawRad);
            double dz     =  Math.cos(yawRad);

            for (int i = 0; i < SPIKE_COUNT; i++) {
                double dist   = FIRST_DIST + i * SPIKE_SPACING;
                double spawnX = player.getX() + dx * dist;
                double spawnZ = player.getZ() + dz * dist;
                double spawnY = findGroundY(level, spawnX, spawnZ, player.getY());

                int delay = i * TICK_STAGGER;
                DepthstrikeGroundEntity spike = new DepthstrikeGroundEntity(
                        level, new Vec3(spawnX, spawnY, spawnZ), delay);
                spike.setYRot(player.getYRot() + 90f); // face perpendicular to line
                level.addFreshEntity(spike);


                // Small pre-emerge spark at each position (fires immediately regardless of delay)
                SparkEntity preSpawn = new SparkEntity(ModEntities.SPARK, level,
                        spawnX, spawnY + 0.1, spawnZ);

                preSpawn.applyPreset(SparkPresets.DEPTHSTRIKE_TRAIL);
                preSpawn.forcedVelocity = new Vec3(0, 0.2, 0);
                // Schedule via delay: only spawn the pre-spark close in time to the spike
                // Simple approach: spawn it only for early spikes so it precedes the emerge
                if (delay == 0) level.addFreshEntity(preSpawn);
            }
        });
    }

    /**
     * Finds the Y level of the ground at (x, z) near startY.
     * Scans downward first, then upward, up to 5 blocks each way.
     */
    private static double findGroundY(ServerLevel level, double x, double z, double startY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                (int) Math.floor(x),
                (int) Math.floor(startY),
                (int) Math.floor(z));

        // Scan down: find first solid block
        for (int i = 0; i < 5; i++) {
            if (!level.getBlockState(pos).isAir()) {
                return pos.getY() + 1.0;
            }
            pos.move(Direction.DOWN);
        }

        // Scan up from start if player is inside a block or above a gap
        pos.set((int) Math.floor(x), (int) Math.floor(startY), (int) Math.floor(z));
        for (int i = 0; i < 5; i++) {
            if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                return pos.getY();
            }
            pos.move(Direction.UP);
        }

        return startY;
    }
}