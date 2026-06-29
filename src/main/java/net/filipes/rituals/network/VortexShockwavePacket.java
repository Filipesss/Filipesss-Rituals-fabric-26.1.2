package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPreset;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.VortexEdgeItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record VortexShockwavePacket() implements CustomPacketPayload {

    public static final Type<VortexShockwavePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "vortex_shockwave"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VortexShockwavePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new VortexShockwavePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(VortexShockwavePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        MinecraftServer server = ctx.server();

        server.execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof VortexEdgeItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 3) return;

            ServerLevel level = player.level();
            double radius = 7.0;
            Vec3 playerPos = player.position();

            AABB area = player.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive() && player.distanceToSqr(e) <= radius * radius);

            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, true));
            }

            ScreenShakeEntity shake = new ScreenShakeEntity(level, playerPos, 22f, 0.45f, 16);
            level.addFreshEntity(shake);

            level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0f, 0.75f);
            level.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.WARDEN_AMBIENT, SoundSource.HOSTILE, 0.55f, 0.5f);

            spawnSparkRing(level, playerPos, 10, 0.0, 0.5, 0.25, 0.05, 0.1, SparkPresets.VORTEX_SPARK_THIN);
            spawnSparkRing(level, playerPos, 7, Math.PI / 7.0, 0.25, 0.2, 0.15, 0.25, SparkPresets.VORTEX_SPARK_TRIPLE);

            level.sendParticles(ParticleTypes.PORTAL, playerPos.x, playerPos.y + 1.0, playerPos.z, 40, 0.45, 0.75, 0.45, 0.55);
            level.sendParticles(ParticleTypes.END_ROD, playerPos.x, playerPos.y + 1.0, playerPos.z, 10, 0.25, 0.4, 0.25, 0.18);

            List<Bat> spawnedBats = new ArrayList<>();
            spawnBatsAround(level, playerPos, 5, spawnedBats);

            for (LivingEntity target : targets) {
                Vec3 targetPos = target.position();

                spawnSparkRing(level, targetPos, 4, 0.0, 0.4, 0.2, 0.05, 0.1, SparkPresets.VORTEX_SPARK_THIN);

                spawnOrbitingSparkRing(level, target, 6, 0.65, 0.28, SparkPresets.VORTEX_SPARK_TRIPLE, server);

                spawnBatsAround(level, targetPos, 2, spawnedBats);
                level.sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y + 1.0, targetPos.z, 15, 0.3, 0.5, 0.3, 0.2);
            }

            for (int delayMs = 250; delayMs <= 1250; delayMs += 250) {
                long finalDelay = delayMs;
                CompletableFuture.delayedExecutor(finalDelay, TimeUnit.MILLISECONDS)
                        .execute(() -> server.execute(() -> {
                            if (spawnedBats.isEmpty()) return;
                            var rand = level.getRandom();
                            Bat randomBat = spawnedBats.get(rand.nextInt(spawnedBats.size()));
                            if (randomBat.isAlive()) {
                                level.playSound(null, randomBat.getX(), randomBat.getY(), randomBat.getZ(),
                                        SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL,
                                        0.6f, 0.7f + rand.nextFloat() * 0.4f);
                            }
                        }));
            }

            CompletableFuture.delayedExecutor(1500, TimeUnit.MILLISECONDS)
                    .execute(() -> server.execute(() -> {
                        var rand = level.getRandom();
                        for (Bat bat : spawnedBats) {
                            if (!bat.isAlive()) continue;
                            level.playSound(null, bat.getX(), bat.getY(), bat.getZ(),
                                    SoundEvents.BAT_LOOP, SoundSource.NEUTRAL,
                                    0.7f, 0.85f + rand.nextFloat() * 0.3f);
                            bat.discard();
                        }
                    }));
        });
    }


    private static void spawnSparkRing(ServerLevel level, Vec3 pos, int count, double angleOffset,
                                       double baseSpeed, double randSpeed, double baseVert, double randVert,
                                       SparkPreset preset) {
        var random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 / count) * i + angleOffset;
            double speed = baseSpeed + random.nextDouble() * randSpeed;
            Vec3 vel = new Vec3(
                    Math.cos(angle) * speed,
                    baseVert + random.nextDouble() * randVert,
                    Math.sin(angle) * speed);
            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, pos.x, pos.y + 0.5, pos.z);
            spark.applyPreset(preset);
            spark.setNoGravity(false);
            spark.setDeltaMovement(vel);
            spark.forcedVelocity = vel;
            level.addFreshEntity(spark);
        }
    }

    private static void spawnOrbitingSparkRing(ServerLevel level, LivingEntity target, int count,
                                               double orbitRadius, double orbitSpeed, SparkPreset preset,
                                               MinecraftServer server) {
        List<SparkEntity> sparkList = new ArrayList<>();
        Vec3 pos = target.position();

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 / count) * i;
            double sx = pos.x + Math.cos(angle) * orbitRadius;
            double sz = pos.z + Math.sin(angle) * orbitRadius;
            double sy = pos.y + 0.4;

            SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, sx, sy, sz);
            spark.applyPreset(preset);
            spark.setNoGravity(true);
            spark.setDeltaMovement(Vec3.ZERO);
            spark.forcedVelocity = Vec3.ZERO;

            level.addFreshEntity(spark);
            sparkList.add(spark);
        }

        runOrbitTicker(target, sparkList, 0, 50, orbitRadius, orbitSpeed, server);
    }

    private static void runOrbitTicker(LivingEntity target, List<SparkEntity> sparks, int currentTick,
                                       int maxTicks, double radius, double speed, MinecraftServer server) {
        if (currentTick >= maxTicks || !target.isAlive()) {
            for (SparkEntity spark : sparks) {
                if (spark.isAlive()) spark.discard();
            }
            return;
        }

        Vec3 targetPos = target.position();

        for (int i = 0; i < sparks.size(); i++) {
            SparkEntity spark = sparks.get(i);
            if (!spark.isAlive()) continue;

            double angle = (Math.PI * 2.0 / sparks.size()) * i + (currentTick * speed);
            double sx = targetPos.x + Math.cos(angle) * radius;
            double sz = targetPos.z + Math.sin(angle) * radius;

            double sy = targetPos.y + 0.4 + (currentTick * 0.035) + (Math.sin(currentTick * 0.25) * 0.1);

            spark.setPos(sx, sy, sz);
            spark.setDeltaMovement(Vec3.ZERO);
            spark.forcedVelocity = Vec3.ZERO;
        }

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() ->
                server.execute(() -> runOrbitTicker(target, sparks, currentTick + 1, maxTicks, radius, speed, server))
        );
    }

    

    private static void spawnBatsAround(ServerLevel level, Vec3 pos, int count, List<Bat> registry) {
        var random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 / count) * i;
            double spawnR = 0.3 + random.nextDouble() * 0.4;
            double bx = pos.x + Math.cos(angle) * spawnR;
            double bz = pos.z + Math.sin(angle) * spawnR;
            double by = pos.y + 0.6 + random.nextDouble() * 0.5;

            Bat bat = new Bat(EntityTypes.BAT, level);
            bat.setPos(bx, by, bz);
            bat.setNoAi(false);
            double speed = 0.45 + random.nextDouble() * 0.35;
            bat.setDeltaMovement(
                    Math.cos(angle) * speed,
                    0.2 + random.nextDouble() * 0.35,
                    Math.sin(angle) * speed);
            level.addFreshEntity(bat);
            registry.add(bat);

            level.playSound(null, bx, by, bz,
                    SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL,
                    0.85f, 0.55f + random.nextFloat() * 0.5f);
        }
    }
}