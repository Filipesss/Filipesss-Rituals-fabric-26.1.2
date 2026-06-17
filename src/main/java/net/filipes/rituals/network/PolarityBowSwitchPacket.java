package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolarityBowSwitchPacket implements CustomPacketPayload {

    public static final Type<PolarityBowSwitchPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "polarity_bow_switch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PolarityBowSwitchPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PolarityBowSwitchPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final long COOLDOWN_MS = 6_000L;

    private static final int UPGRADE_STAGE = 4;
    private static final double PUSH_PULL_RADIUS = 6.0;
    private static final double PUSH_STRENGTH = 0.10;
    private static final double PULL_STRENGTH = 0.12;

    private static class SparkSpawnerState {
        final boolean toRed;
        final boolean upgraded;
        final List<SparkEntity> liveSparks = new ArrayList<>();
        int ticksExisted = 0;

        SparkSpawnerState(boolean toRed, boolean upgraded) {
            this.toRed = toRed;
            this.upgraded = upgraded;
        }
    }

    private static final Map<UUID, SparkSpawnerState> ACTIVE_SPARK_SPAWNERS = new HashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PolarityBowSwitchPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof PolarityBowItem)) return;

            int stage = ModDataComponents.getStage(held);
            if (stage < 1) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            CustomModelData current = held.get(DataComponents.CUSTOM_MODEL_DATA);
            boolean isRed = current != null
                    && !current.flags().isEmpty()
                    && current.flags().get(0);

            boolean nextIsRed = !isRed;
            boolean upgraded = stage >= UPGRADE_STAGE;

            held.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(), List.of(nextIsRed), List.of(), List.of()));

            ACTIVE_SPARK_SPAWNERS.put(uuid, new SparkSpawnerState(nextIsRed, upgraded));

            ServerLevel level = player.level();
            double px = player.getX();
            double py = player.getY() + 1.0;
            double pz = player.getZ();
            int burstColor = nextIsRed ? 0xFF1A1A : 0x00B2FF;

            level.sendParticles(ParticleTypes.ENCHANT,
                    px, py, pz,
                    25, 0.5, 0.7, 0.5, 2.5);

            level.sendParticles(new DustParticleOptions(burstColor, 2.5f),
                    px, py, pz,
                    18, 0.4, 0.5, 0.4, 0.8);

            level.sendParticles(ParticleTypes.END_ROD,
                    px, py, pz,
                    14, 0.3, 0.4, 0.3, 0.2);

            if (upgraded) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        px, py, pz,
                        30, 0.7, 0.9, 0.7, 0.5);

                level.sendParticles(new DustParticleOptions(burstColor, 3.5f),
                        px, py, pz,
                        20, 0.6, 0.6, 0.6, 1.2);
            }

            level.playSound(null, px, py, pz,
                    ModSounds.POLARITY_CHANGE, SoundSource.PLAYERS, 1.0f, nextIsRed ? 0.9f : 1.5f);
        });
    }

    public static void tickServerSparks(MinecraftServer server) {
        Iterator<Map.Entry<UUID, SparkSpawnerState>> iterator = ACTIVE_SPARK_SPAWNERS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, SparkSpawnerState> entry = iterator.next();
            UUID uuid = entry.getKey();
            SparkSpawnerState state = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null || state.ticksExisted >= 20) {
                iterator.remove();
                continue;
            }

            state.ticksExisted++;
            ServerLevel level = player.level();
            var random = level.getRandom();
            Vec3 playerPos = player.position();

            int hexColor = state.toRed ? 0xFF1A1A : 0x00B2FF;
            DustParticleOptions dustParticle = new DustParticleOptions(hexColor, 1.4f);

            double cx = playerPos.x;
            double cy = playerPos.y + 1.0;
            double cz = playerPos.z;

            if (state.upgraded) {
                Vec3 anchor = playerPos.add(0, 1.0, 0);
                AABB pushPullZone = player.getBoundingBox().inflate(PUSH_PULL_RADIUS);

                List<Entity> affected = level.getEntitiesOfClass(Entity.class, pushPullZone,
                        e -> e != player && e.isAlive());

                for (Entity e : affected) {
                    Vec3 dir = state.toRed
                            ? e.position().subtract(anchor)
                            : anchor.subtract(e.position());

                    if (dir.lengthSqr() < 0.05) continue;

                    double strength = state.toRed ? PUSH_STRENGTH : PULL_STRENGTH;
                    e.setDeltaMovement(e.getDeltaMovement().add(dir.normalize().scale(strength)));
                    e.hurtMarked = true;
                }

                // Telegraph the push/pull radius
                for (int i = 0; i < 6; i++) {
                    double ringAngle = random.nextDouble() * Math.PI * 2.0;
                    double rx = cx + Math.cos(ringAngle) * PUSH_PULL_RADIUS;
                    double rz = cz + Math.sin(ringAngle) * PUSH_PULL_RADIUS;
                    level.sendParticles(dustParticle, rx, cy - 0.8, rz, 0, 0, 0, 0, 0);
                }
            }

            level.sendParticles(ParticleTypes.ENCHANT,
                    cx, cy, cz,
                    4, 0.35, 0.45, 0.35, 1.8);

            level.sendParticles(ParticleTypes.END_ROD,
                    cx, cy, cz,
                    1, 0.2, 0.3, 0.2, 0.05);

            // Existing curving spark kinematics
            state.liveSparks.removeIf(spark -> !spark.isAlive());
            for (SparkEntity spark : state.liveSparks) {
                Vec3 toSpark = spark.position().subtract(playerPos.add(0, 1.0, 0));
                Vec3 up = new Vec3(0, 1, 0);

                Vec3 tangent = toSpark.cross(up).normalize();
                double spiralDirection = state.toRed ? 1.0 : -1.0;
                double curveStrength = 0.05;

                Vec3 currentVel = spark.getDeltaMovement();
                Vec3 curvedVel = currentVel.add(tangent.scale(spiralDirection * curveStrength));

                spark.setDeltaMovement(curvedVel);
                spark.forcedVelocity = curvedVel;
            }

            // Chaotic Spark Spawn loop
            int sparksThisTick = 1 + random.nextInt(2);
            for (int i = 0; i < sparksThisTick; i++) {
                double yaw = random.nextDouble() * Math.PI * 2.0;
                double pitch = Math.asin(random.nextDouble() * 2.0 - 1.0);
                double speed = 0.15 + random.nextDouble() * 0.45;

                double cosPitch = Math.cos(pitch);
                Vec3 vel = new Vec3(
                        Math.cos(yaw) * cosPitch * speed,
                        Math.sin(pitch) * speed,
                        Math.sin(yaw) * cosPitch * speed
                );

                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, cx, cy, cz);
                spark.applyPreset(state.toRed ? SparkPresets.POLARITY_RED_SINGLE : SparkPresets.POLARITY_BLUE_SINGLE);
                spark.setNoGravity(false);
                spark.setDeltaMovement(vel);
                spark.forcedVelocity = vel;

                level.addFreshEntity(spark);
                state.liveSparks.add(spark);
            }

            for (int i = 0; i < 4; i++) {
                double pYaw = random.nextDouble() * Math.PI * 2.0;
                double pPitch = (random.nextDouble() - 0.5) * 0.4;
                double pSpeed = 0.4 + random.nextDouble() * 0.3;

                double pCosPitch = Math.cos(pPitch);
                double velX = Math.cos(pYaw) * pCosPitch * pSpeed;
                double velY = Math.sin(pPitch) * pSpeed;
                double velZ = Math.sin(pYaw) * pCosPitch * pSpeed;

                level.sendParticles(dustParticle,
                        cx, cy, cz,
                        0, velX, velY, velZ, 1.0);
            }
        }
    }
}