package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.BlightspearItem;
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

    public static class TetherInstance {
        public final UUID targetUuid;
        public final Vec3 rootPos;
        public final ResourceKey<Level> dimension;
        public final long startTick;
        public final long expiryTick;

        public final SparkEntity hookSpark;
        public final SparkEntity orbitSpark;
        public final SparkEntity radiusSpark;
        public double hookPhase = 0.0;

        public TetherInstance(UUID targetUuid, Vec3 rootPos, ResourceKey<Level> dimension,
                              long startTick, long expiryTick, SparkEntity hookSpark,
                              SparkEntity orbitSpark, SparkEntity radiusSpark) {
            this.targetUuid = targetUuid;
            this.rootPos = rootPos;
            this.dimension = dimension;
            this.startTick = startTick;
            this.expiryTick = expiryTick;
            this.hookSpark = hookSpark;
            this.orbitSpark = orbitSpark;
            this.radiusSpark = radiusSpark;
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
            var held = player.getMainHandItem();
            int stage = ModDataComponents.getStage(held);
            if (stage < 6) return;

            ServerLevel level = player.level();
            Entity targetEntity = level.getEntity(pkt.targetId());

            if (targetEntity instanceof LivingEntity target && target.isAlive()) {
                Vec3 rootPosition = target.position();
                long start = level.getGameTime();
                long durationTicks = 100; // 5 seconds
                long expiry = start + durationTicks;

                Vec3 rawKnockbackDir = target.position().subtract(player.position());
                Vec3 flatDir = new Vec3(rawKnockbackDir.x, 0, rawKnockbackDir.z).normalize();
                if (rawKnockbackDir.lengthSqr() < 0.01) {
                    flatDir = new Vec3(0, 0, 1);
                }

                target.setDeltaMovement(target.getDeltaMovement().add(flatDir.scale(0.55).add(0, 0.28, 0)));
                if (target instanceof ServerPlayer sp) {
                    sp.hurtMarked = true;
                }

                SparkEntity hook = new SparkEntity(ModEntities.SPARK, level, rootPosition.x, rootPosition.y + 0.2, rootPosition.z);
                hook.applyPreset(SparkPresets.BLIGHT_TETHER_HOOK);
                hook.setNoGravity(true);
                hook.maxLifetime = 120;
                level.addFreshEntity(hook);

                SparkEntity orbit = new SparkEntity(ModEntities.SPARK, level, target.getX(), target.getY() + 1.0, target.getZ());
                orbit.applyPreset(SparkPresets.BLIGHT_TETHER_BORDER);
                orbit.setNoGravity(true);
                orbit.maxLifetime = 120;
                level.addFreshEntity(orbit);

                SparkEntity radiusSpark = new SparkEntity(ModEntities.SPARK, level, rootPosition.x + TETHER_RADIUS, rootPosition.y + 0.15, rootPosition.z);
                radiusSpark.applyPreset(SparkPresets.BLIGHT_TETHER_BORDER);
                radiusSpark.setNoGravity(true);
                radiusSpark.maxLifetime = 120;
                level.addFreshEntity(radiusSpark);

                ACTIVE_TETHERS.add(new TetherInstance(target.getUUID(), rootPosition, level.dimension(), start, expiry, hook, orbit, radiusSpark));

                level.playSound(null, rootPosition.x, rootPosition.y, rootPosition.z,
                        SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.5f, 0.5f);
            }
        });
    }
}