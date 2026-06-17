package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.VortexEdgeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record VortexSwapPacket(int targetId) implements CustomPacketPayload {

    public static final Type<VortexSwapPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "vortex_swap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VortexSwapPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, VortexSwapPacket::targetId,
            VortexSwapPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(VortexSwapPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof VortexEdgeItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 2) return;

            ServerLevel level = player.level();
            Entity target = level.getEntity(pkt.targetId());

            if (target == null || player.distanceToSqr(target) > 400.0) return;

            Vec3 playerPos = player.position();
            Vec3 targetPos = target.position();

            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            target.teleportTo(playerPos.x, playerPos.y, playerPos.z);

            player.setDeltaMovement(Vec3.ZERO);
            target.setDeltaMovement(Vec3.ZERO);

            // Sounds at both positions — chorus for the swap, amethyst for the impact
            level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);

            level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);

            // Burst at player's OLD position
            spawnSwapBurst(level, playerPos);

            // Burst at target's OLD position
            spawnSwapBurst(level, targetPos);
        });
    }

    private static void spawnSwapBurst(ServerLevel level, Vec3 pos) {
        double x = pos.x, y = pos.y + 1.0, z = pos.z;

        int ringCount = 8;
        for (int i = 0; i < ringCount; i++) {
            double angle = (Math.PI * 2.0 / ringCount) * i;
            double speed = 0.3 + Math.random() * 0.2;
            Vec3 vel = new Vec3(Math.cos(angle) * speed, 0.05 + Math.random() * 0.1,
                    Math.sin(angle) * speed);
            net.filipes.rituals.entity.custom.SparkEntity spark =
                    new net.filipes.rituals.entity.custom.SparkEntity(
                            net.filipes.rituals.entity.ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(net.filipes.rituals.entity.custom.SparkPresets.VORTEX_SPARK_THIN);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        int tripleCount = 5;
        for (int i = 0; i < tripleCount; i++) {
            double angle = (Math.PI * 2.0 / tripleCount) * i;
            double speed = 0.2 + Math.random() * 0.25;
            Vec3 vel = new Vec3(Math.cos(angle) * speed, 0.2 + Math.random() * 0.25,
                    Math.sin(angle) * speed);
            net.filipes.rituals.entity.custom.SparkEntity spark =
                    new net.filipes.rituals.entity.custom.SparkEntity(
                            net.filipes.rituals.entity.ModEntities.SPARK, level, x, y, z);
            spark.applyPreset(SparkPresets.VORTEX_SPARK_TRIPLE_BLACK);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }

        // Vanilla particles — portal swirl at ground, end rod flash upward
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                x, y, z, 20, 0.3, 0.5, 0.3, 0.5);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                x, y, z, 8, 0.2, 0.3, 0.2, 0.15);
    }
}