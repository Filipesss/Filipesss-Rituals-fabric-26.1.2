package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.BlightedPuddleEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPreset;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record BlightWebPacket(int targetId) implements CustomPacketPayload {

    public static final Type<BlightWebPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_web"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightWebPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlightWebPacket::targetId,
            BlightWebPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightWebPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        MinecraftServer server = ctx.server();

        server.execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof BlightspearItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 2) return;

            ServerLevel level = player.level();
            Entity target = level.getEntity(pkt.targetId());

            if (target == null || player.distanceToSqr(target) > 625.0) return;

            BlockPos feetPos = target.blockPosition();

            if (level.getBlockState(feetPos).canBeReplaced()) {
                level.setBlockAndUpdate(feetPos, Blocks.COBWEB.defaultBlockState());
            }

            BlightedPuddleEntity puddle = new BlightedPuddleEntity(ModEntities.BLIGHTED_PUDDLE, level);
            puddle.setPos(target.getX(), target.getY(), target.getZ());
            puddle.setOwnerUUID(player.getUUID());
            level.addFreshEntity(puddle);

            for (int i = 0; i < 10; i++) {
                double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
                double horizSpd = 0.18 + level.getRandom().nextDouble() * 0.22;
                double vertSpd = 0.05 + level.getRandom().nextDouble() * 0.25;

                SparkEntity webSpark = new SparkEntity(ModEntities.SPARK, level, target.getX(), target.getY() + 0.6, target.getZ());
                webSpark.applyPreset(SparkPresets.BLIGHT_WEB);
                webSpark.forcedVelocity = new Vec3(
                        Math.cos(angle) * horizSpd,
                        vertSpd,
                        Math.sin(angle) * horizSpd
                );
                level.addFreshEntity(webSpark);
            }

            for (int i = 0; i < 3; i++) {
                SparkEntity ambientSpark = new SparkEntity(ModEntities.SPARK, level, target.getX(), target.getY() + 0.1, target.getZ());
                ambientSpark.applyPreset(SparkPresets.BLIGHT_SINGLE);
                ambientSpark.forcedVelocity = new Vec3(
                        (level.getRandom().nextDouble() - 0.5) * 0.05,
                        0.15,
                        (level.getRandom().nextDouble() - 0.5) * 0.05
                );
                level.addFreshEntity(ambientSpark);
            }

            spawnOrbitingWebSpark(level, target, 0.65, 0.22, SparkPresets.BLIGHT_WEB, server);

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 0.6f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.8f, 0.5f);
        });
    }

    private static void spawnOrbitingWebSpark(ServerLevel level, Entity target, double orbitRadius,
                                              double orbitSpeed, SparkPreset preset, MinecraftServer server) {
        Vec3 pos = target.position();

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x + orbitRadius, pos.y + 0.2, pos.z);
        spark.applyPreset(preset);
        spark.setNoGravity(true);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;

        level.addFreshEntity(spark);

        runWebOrbitTicker(target, spark, 0, 60, orbitRadius, orbitSpeed, server);
    }

    private static void runWebOrbitTicker(Entity target, SparkEntity spark, int currentTick,
                                          int maxTicks, double radius, double speed, MinecraftServer server) {

        if (currentTick >= maxTicks || !target.isAlive() || !spark.isAlive()) {
            if (spark.isAlive()) spark.discard();
            return;
        }

        Vec3 targetPos = target.position();

        double angle = currentTick * speed;
        double sx = targetPos.x + Math.cos(angle) * radius;
        double sz = targetPos.z + Math.sin(angle) * radius;

        double heightProgress = ((double) currentTick / maxTicks) * 1.4;
        double sy = targetPos.y + 0.1 + heightProgress;

        spark.setPos(sx, sy, sz);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() ->
                server.execute(() -> runWebOrbitTicker(target, spark, currentTick + 1, maxTicks, radius, speed, server))
        );
    }
}