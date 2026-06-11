package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
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

    private static class SparkSpawnerState {
        final boolean toRed;
        final List<SparkEntity> liveSparks = new ArrayList<>();
        int ticksExisted = 0;

        SparkSpawnerState(boolean toRed) {
            this.toRed = toRed;
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

            held.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(), List.of(nextIsRed), List.of(), List.of()));

            ACTIVE_SPARK_SPAWNERS.put(uuid, new SparkSpawnerState(nextIsRed));

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

            AABB magnetZone = player.getBoundingBox().inflate(6.0);
            if (!state.toRed) {
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, magnetZone);
                for (ItemEntity item : items) {
                    Vec3 pullDir = playerPos.add(0, 0.5, 0).subtract(item.position());
                    if (pullDir.lengthSqr() > 0.2) {
                        item.setDeltaMovement(item.getDeltaMovement().add(pullDir.normalize().scale(0.12)));
                        item.hurtMarked = true;
                    }
                }
                List<AbstractArrow> arrows = level.getEntitiesOfClass(AbstractArrow.class, magnetZone, a -> a.getOwner() != player);
                for (AbstractArrow arrow : arrows) {
                    Vec3 pullDir = playerPos.add(0, 1.0, 0).subtract(arrow.position());
                    arrow.setDeltaMovement(arrow.getDeltaMovement().add(pullDir.normalize().scale(0.06)));
                    arrow.hurtMarked = true;
                }
            } else {
                List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, magnetZone, p -> p.getOwner() != player);
                for (Projectile proj : projectiles) {
                    Vec3 pushDir = proj.position().subtract(playerPos.add(0, 1.0, 0));
                    if (pushDir.lengthSqr() > 0.1) {
                        proj.setDeltaMovement(proj.getDeltaMovement().add(pushDir.normalize().scale(0.10)));
                        proj.hurtMarked = true;
                    }
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