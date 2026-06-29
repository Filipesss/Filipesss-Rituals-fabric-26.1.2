package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.LifestealMarkEntity;
import net.filipes.rituals.entity.custom.LifestealMarkTracker;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LifestealMarkPacket implements CustomPacketPayload {

    public static final Type<LifestealMarkPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "lifesteal_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LifestealMarkPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new LifestealMarkPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS  = 8_000L;
    private static final double RAY_LENGTH = 20.0;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LifestealMarkPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ShadowguardItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 5) return;


            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;

            Vec3 start = player.getEyePosition();
            Vec3 end   = start.add(player.getLookAngle().scale(RAY_LENGTH));

            ServerLevel level = (ServerLevel) player.level();
            LivingEntity target = findTarget(player, level, start, end);

            if (target == null) return;

            SERVER_COOLDOWNS.put(uuid, now);
            LifestealMarkTracker.mark(target.getUUID(), player.getUUID());

            double spawnX = target.getX();
            double spawnY = target.getY() + target.getBbHeight() * 0.5;
            double spawnZ = target.getZ();

            LifestealMarkEntity mark = new LifestealMarkEntity(ModEntities.LIFESTEAL_MARK, level, spawnX, spawnY, spawnZ);
            mark.setTargetUUID(target.getUUID());
            mark.setEntityScale(1.0f);
            level.addFreshEntity(mark);

            Vec3 toPlayer = player.position()
                    .add(0, player.getBbHeight() * 0.5, 0)
                    .subtract(spawnX, spawnY, spawnZ)
                    .normalize();

            for (int i = 0; i < 8; i++) {
                Vec3 velocity;
                if (i < 3) {
                    velocity = toPlayer
                            .add(randomSpread(0.35))
                            .normalize()
                            .scale(0.5 + Math.random() * 0.4);
                } else {
                    velocity = randomSphere(0.4 + Math.random() * 0.5);
                }
                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
                spark.applyPreset(SparkPresets.LIFESTEAL_BIG);
                spark.forcedVelocity = velocity;
                spark.setDeltaMovement(velocity);
                level.addFreshEntity(spark);
            }

            for (int i = 0; i < 6; i++) {
                Vec3 velocity;
                if (i < 3) {
                    velocity = toPlayer
                            .add(randomSpread(0.25))
                            .normalize()
                            .scale(0.35 + Math.random() * 0.3);
                } else {
                    velocity = randomSphere(0.25 + Math.random() * 0.35);
                }
                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
                spark.applyPreset(SparkPresets.LIFESTEAL_SHADOWGUARD);
                spark.forcedVelocity = velocity;
                spark.setDeltaMovement(velocity);
                level.addFreshEntity(spark);
            }

            level.playSound(null,
                    target.getX(), target.getY(), target.getZ(),
                    SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f, 0.6f);
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

    private static LivingEntity findTarget(ServerPlayer player, ServerLevel level,
                                           Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                e -> e != player && e.isAlive());

        LivingEntity closest = null;
        double closestDist   = Double.MAX_VALUE;

        for (LivingEntity candidate : candidates) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest     = candidate;
                }
            }
        }

        return closest;
    }
}