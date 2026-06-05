package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BlightTetherPacket(int targetId) implements CustomPacketPayload {

    public static final Type<BlightTetherPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_tether"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightTetherPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlightTetherPacket::targetId,
            BlightTetherPacket::new
    );

    // Data structure to hold active tethers globally on the server
    public static class TetherInstance {
        public final UUID targetUuid;
        public final Vec3 rootPos;
        public final ResourceKey<Level> dimension;
        public final long expiryTick;

        public TetherInstance(UUID targetUuid, Vec3 rootPos, ResourceKey<Level> dimension, long expiryTick) {
            this.targetUuid = targetUuid;
            this.rootPos = rootPos;
            this.dimension = dimension;
            this.expiryTick = expiryTick;
        }
    }

    public static final List<TetherInstance> ACTIVE_TETHERS = new ArrayList<>();
    public static final double TETHER_RADIUS = 3.0;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightTetherPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            if (!(player.getMainHandItem().getItem() instanceof BlightspearItem)) return;

            ServerLevel level = player.level();
            Entity targetEntity = level.getEntity(pkt.targetId());

            if (targetEntity instanceof LivingEntity target && target.isAlive()) {
                // Set anchor exactly where the target is standing right now
                Vec3 rootPosition = target.position();
                long durationTicks = 100; // 5 seconds * 20 ticks
                long expiry = level.getGameTime() + durationTicks;

                // Add to active ticking loop
                ACTIVE_TETHERS.add(new TetherInstance(target.getUUID(), rootPosition, level.dimension(), expiry));

                // Audio cue at position
                level.playSound(null, rootPosition.x, rootPosition.y, rootPosition.z,
                        SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.5f, 0.5f);
            }
        });
    }
}