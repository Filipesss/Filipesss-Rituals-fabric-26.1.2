package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.BlightedPuddleEntity;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlightDashPacket implements CustomPacketPayload {

    public static final Type<BlightDashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_dash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightDashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new BlightDashPacket());

    // Tracking structures
    private static class DashState {
        int count = 0;
        long lastDashTime = 0;
    }

    private static final Map<UUID, DashState> PLAYER_STATES = new HashMap<>();
    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();

    public static final long COOLDOWN_MS = 15_000L; // 15 seconds global cooldown
    public static final long WINDOW_MS = 4_000L;    // 4 seconds window to execute 2nd dash
    public static final float DASH_DISTANCE = 7.0f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightDashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof BlightspearItem)) return;

            UUID uuid = player.getUUID();
            long now = System.currentTimeMillis();

            // 1. Check if the master global cooldown is active
            Long lastGlobal = SERVER_COOLDOWNS.get(uuid);
            if (lastGlobal != null && now - lastGlobal < COOLDOWN_MS) return;

            DashState state = PLAYER_STATES.computeIfAbsent(uuid, k -> new DashState());

            // Reset charge count if they took too long to press it a second time
            if (state.count == 1 && now - state.lastDashTime > WINDOW_MS) {
                state.count = 0;
            }

            // 2. Determine dash direction based on execution stage
            Vec3 look = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 dashDir;

            if (state.count == 0) {
                // First Press -> Dash Backwards
                dashDir = look.scale(-1);
                state.count = 1;
                state.lastDashTime = now;
            } else {
                // Second Press -> Dash Forwards
                dashDir = look;
                state.count = 0; // Reset state
                SERVER_COOLDOWNS.put(uuid, now); // Lock with global cooldown
            }

            Vec3 start = player.position();
            Vec3 end = start.add(dashDir.scale(DASH_DISTANCE));
            ServerLevel level = player.level();

            // 3. Clip against terrain to avoid dashing through solid blocks
            ClipContext clipCtx = new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            BlockHitResult hitResult = level.clip(clipCtx);
            Vec3 actualEnd = (hitResult.getType() == HitResult.Type.BLOCK) ? hitResult.getLocation() : end;

            // 4. Linearly interpolate along the path to drop Blighted Puddles
            double distance = start.distanceTo(actualEnd);
            int puddleCount = (int) Math.max(1, distance / 1.5); // Drops a puddle roughly every 1.5 blocks

            for (int i = 0; i <= puddleCount; i++) {
                double pct = (double) i / puddleCount;
                Vec3 puddlePos = start.lerp(actualEnd, pct);

                BlightedPuddleEntity puddle = new BlightedPuddleEntity(ModEntities.BLIGHTED_PUDDLE, level);
                puddle.setPos(puddlePos.x, puddlePos.y, puddlePos.z);
                puddle.setOwnerUUID(uuid);
                level.addFreshEntity(puddle);
            }

            // Audio indicators
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), SoundSource.PLAYERS, 0.4f, 1.8f);

            // 5. Apply physical velocity push
            player.setDeltaMovement(dashDir.scale(1.6));
            player.hurtMarked = true;
        });
    }
}