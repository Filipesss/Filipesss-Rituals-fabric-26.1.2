package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.PharathornMarkEntity;
import net.filipes.rituals.entity.custom.PharathornMarkTracker;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PharathornRevealPacket implements CustomPacketPayload {

    public static final Type<PharathornRevealPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pharathorn_reveal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharathornRevealPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PharathornRevealPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long   COOLDOWN_MS  = 30_000L;
    private static final double REVEAL_RANGE = 32.0;
    private static final int    REVEAL_TICKS = 100; // 5 seconds

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PharathornRevealPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PharathornItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 3) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();
            AABB searchBox = player.getBoundingBox().inflate(REVEAL_RANGE);

            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive());

            for (LivingEntity target : candidates) {
                double spawnX = target.getX();
                double spawnY = target.getY() + target.getBbHeight() * 0.5;
                double spawnZ = target.getZ();

                if (PharathornMarkTracker.isMarked(target.getUUID())) {
                    AABB markBox = target.getBoundingBox().inflate(1.0);
                    List<PharathornMarkEntity> existing = level.getEntitiesOfClass(
                            PharathornMarkEntity.class, markBox,
                            e -> target.getUUID().equals(e.getTargetUUID()));
                    existing.forEach(e -> e.setForced(REVEAL_TICKS));
                } else {

                    PharathornMarkTracker.mark(target.getUUID());

                    PharathornMarkEntity mark = new PharathornMarkEntity(
                            ModEntities.PHARATHORN_MARK, level, spawnX, spawnY, spawnZ);
                    mark.setTargetUUID(target.getUUID());
                    mark.setForced(REVEAL_TICKS);
                    mark.setEntityScale(1.0f);
                    level.addFreshEntity(mark);


                    Vec3 toPlayer = player.position()
                            .add(0, player.getBbHeight() * 0.5, 0)
                            .subtract(spawnX, spawnY, spawnZ)
                            .normalize();

                    for (int i = 0; i < 8; i++) {
                        Vec3 vel = i < 3
                                ? toPlayer.add(randomSpread(0.35)).normalize().scale(0.5 + Math.random() * 0.4)
                                : randomSphere(0.4 + Math.random() * 0.5);
                        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
                        spark.applyPreset(SparkPresets.PHARATHORN_MARK_BIG);
                        spark.forcedVelocity = vel;
                        spark.setDeltaMovement(vel);
                        level.addFreshEntity(spark);
                    }
                    for (int i = 0; i < 6; i++) {
                        Vec3 vel = i < 3
                                ? toPlayer.add(randomSpread(0.25)).normalize().scale(0.35 + Math.random() * 0.3)
                                : randomSphere(0.25 + Math.random() * 0.35);
                        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
                        spark.applyPreset(SparkPresets.PHARATHORN_MARK);
                        spark.forcedVelocity = vel;
                        spark.setDeltaMovement(vel);
                        level.addFreshEntity(spark);
                    }
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
        });
    }

    private static Vec3 randomSphere(double speed) {
        double theta = Math.random() * Math.PI * 2.0;
        double phi   = Math.acos(2.0 * Math.random() - 1.0);
        return new Vec3(
                Math.sin(phi) * Math.cos(theta) * speed,
                Math.cos(phi) * speed,
                Math.sin(phi) * Math.sin(theta) * speed);
    }

    private static Vec3 randomSpread(double amount) {
        return new Vec3(
                (Math.random() - 0.5) * amount,
                (Math.random() - 0.5) * amount,
                (Math.random() - 0.5) * amount);
    }
}