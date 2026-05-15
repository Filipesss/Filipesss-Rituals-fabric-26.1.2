package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.PharathornGroundSmashEntity;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PharathornGroundSmashPacket implements CustomPacketPayload {

    public static final Type<PharathornGroundSmashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pharathorn_ground_smash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharathornGroundSmashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PharathornGroundSmashPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    private static final double[] ROW_DIST   = { 1.5, 2.5, 3.5, 4.5 };
    private static final int[]    ROW_COUNT  = { 3,   5,   7,   9   };
    private static final int[]    ROW_DELAY  = { 0,   2,  4,  6   };
    private static final float[]  ROW_SCALE  = { 0.75f, 0.9f, 1.05f, 1.2f };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PharathornGroundSmashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PharathornItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 7) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            // Attack swing animation
            player.swing(InteractionHand.MAIN_HAND, true);

            // Explosion sound at cast origin
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 1.0f, 0.6f);

            // Screen shake centered on player
            level.addFreshEntity(new ScreenShakeEntity(
                    level,
                    player.position(),
                    20.0f,   // radius in blocks
                    1.2f,    // strength
                    25       // duration in ticks
            ));

            float  yawRad = (float) Math.toRadians(player.getYRot());
            double fwdX   = -Math.sin(yawRad);
            double fwdZ   =  Math.cos(yawRad);
            double rgtX   =  Math.cos(yawRad);
            double rgtZ   =  Math.sin(yawRad);

            for (int row = 0; row < ROW_DIST.length; row++) {
                int   count     = ROW_COUNT[row];
                float baseScale = ROW_SCALE[row];

                for (int s = 0; s < count; s++) {
                    double maxSpread = row * 1.5;
                    double spread    = (count == 1) ? 0.0
                            : ((double) s / (count - 1) - 0.5) * maxSpread * 2.0;

                    spread += (Math.random() - 0.5) * 0.4;
                    double fwdJitter = (Math.random() - 0.5) * 0.3;

                    double spawnX = player.getX() + fwdX * (ROW_DIST[row] + fwdJitter) + rgtX * spread;
                    double spawnZ = player.getZ() + fwdZ * (ROW_DIST[row] + fwdJitter) + rgtZ * spread;
                    double spawnY = findGroundY(level, spawnX, spawnZ, player.getY());

                    float randomYRot      = (float)(Math.random() * 360.0);
                    float scaleVariation  = baseScale + (float)(Math.random() - 0.5) * 0.12f;

                    PharathornGroundSmashEntity spike = new PharathornGroundSmashEntity(
                            level, spawnX, spawnY, spawnZ,
                            ROW_DELAY[row], randomYRot, scaleVariation);
                    level.addFreshEntity(spike);
                }
            }

            // Original impact sound still plays underneath
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EVOKER_FANGS_ATTACK,
                    SoundSource.PLAYERS, 1.2f, 0.5f);
        });
    }

    private static double findGroundY(ServerLevel level, double x, double z, double startY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                (int) Math.floor(x), (int) Math.floor(startY), (int) Math.floor(z));

        // Scan downward until we hit something with solid collision
        for (int i = 0; i < 10; i++) {
            if (isSolidGround(level, pos)) return pos.getY() + 1.0;
            pos.move(Direction.DOWN);
        }
        // Scan upward from start as fallback
        pos.set((int) Math.floor(x), (int) Math.floor(startY), (int) Math.floor(z));
        for (int i = 0; i < 10; i++) {
            if (isSolidGround(level, pos) && !isSolidGround(level, pos.above())) return pos.getY() + 1.0;
            pos.move(Direction.UP);
        }
        return startY;
    }

    private static boolean isSolidGround(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}