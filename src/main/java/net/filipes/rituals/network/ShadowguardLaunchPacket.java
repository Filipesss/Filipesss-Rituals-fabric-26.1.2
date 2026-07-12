package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadowguardLaunchPacket implements CustomPacketPayload {

    public static final Type<ShadowguardLaunchPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadowguard_launch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadowguardLaunchPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadowguardLaunchPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS    = 12_000L;
    private static final float KNOCKBACK_RADIUS = 5.0f;
    private static final float KNOCKBACK_STRENGTH = 1.6f;
    private static final float LAUNCH_POWER = 1.2f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShadowguardLaunchPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ShadowguardItem)) return;

            int stage = ModDataComponents.getStage(stack);
            if (stage < 4) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            Vec3 current = player.getDeltaMovement();
            player.setDeltaMovement(current.x * 0.5, LAUNCH_POWER, current.z * 0.5);
            player.connection.send(new ClientboundSetEntityMotionPacket(
                    player.getId(), player.getDeltaMovement()));

            AABB box = new AABB(
                    player.getX() - KNOCKBACK_RADIUS, player.getY() - 2, player.getZ() - KNOCKBACK_RADIUS,
                    player.getX() + KNOCKBACK_RADIUS, player.getY() + 4, player.getZ() + KNOCKBACK_RADIUS);

            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (nearby == player) continue;

                Vec3 diff = nearby.position().subtract(player.position());
                double dist = diff.length();
                if (dist < 0.1 || dist > KNOCKBACK_RADIUS) continue;

                double falloff = 1.0 - (dist / KNOCKBACK_RADIUS);
                Vec3 impulse = diff.normalize()
                        .scale(KNOCKBACK_STRENGTH * falloff)
                        .add(0, 0.8 * falloff, 0);

                nearby.setDeltaMovement(nearby.getDeltaMovement().add(impulse));

                if (nearby instanceof ServerPlayer nearbyPlayer) {
                    nearbyPlayer.connection.send(new ClientboundSetEntityMotionPacket(
                            nearbyPlayer.getId(), nearbyPlayer.getDeltaMovement()));
                }
            }
            ScreenShakeEntity shake = new ScreenShakeEntity(level,
                    new Vec3(player.getX(), player.getY(), player.getZ()),
                    20f, 0.55f, 16);
            level.addFreshEntity(shake);

            int SMASH_POINTS = 12;
            for (int i = 0; i < SMASH_POINTS; i++) {
                double angle = (Math.PI * 2.0 / SMASH_POINTS) * i;
                double r = 0.3;

                SparkEntity smash = new SparkEntity(ModEntities.SPARK, level,
                        player.getX() + Math.cos(angle) * r,
                        player.getY() + 0.05,
                        player.getZ() + Math.sin(angle) * r);
                smash.applyPreset(SparkPresets.SHADOWGUARD_KNOCK_UP);
                smash.forcedVelocity = new Vec3(
                        Math.cos(angle) * 0.85,
                        0.08 + level.getRandom().nextDouble() * 0.1,
                        Math.sin(angle) * 0.85);
                level.addFreshEntity(smash);
            }

            for (int i = 0; i < 6; i++) {
                double angle = (Math.PI * 2.0 / 6) * i;
                SparkEntity debris = new SparkEntity(ModEntities.SPARK, level,
                        player.getX(), player.getY() + 0.1, player.getZ());
                debris.applyPreset(SparkPresets.SHADOWGUARD_KNOCK_UP);
                debris.forcedVelocity = new Vec3(
                        Math.cos(angle) * 0.3,
                        0.9 + level.getRandom().nextDouble() * 0.5,
                        Math.sin(angle) * 0.3);
                level.addFreshEntity(debris);
            }

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 0.1, player.getZ(),
                    3, 0.4, 0.1, 0.4, 0.05);
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX(), player.getY() + 0.1, player.getZ(),
                    6, 0.5, 0.1, 0.5, 0.02);

            level.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.2f, 0.75f + level.getRandom().nextFloat() * 0.2f);
            level.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.6f, 1.6f);

            int POINTS = 8;
            for (int i = 0; i < POINTS; i++) {
                double angle = (Math.PI * 2.0 / POINTS) * i;
                double radius = 1.2;

                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.1,
                        player.getZ() + Math.sin(angle) * radius);
                spark.applyPreset(SparkPresets.SHADOWGUARD_KNOCK_UP);
                spark.forcedVelocity = new Vec3(
                        Math.cos(angle) * 0.45,
                        0.5 + level.getRandom().nextDouble() * 0.3,
                        Math.sin(angle) * 0.45);
                level.addFreshEntity(spark);
            }

            for (int i = 0; i < 4; i++) {
                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                        player.getX() + (level.getRandom().nextDouble() - 0.5) * 0.4,
                        player.getY() + 0.2,
                        player.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.4);
                spark.applyPreset(SparkPresets.SHADOWGUARD_KNOCK_UP);
                spark.forcedVelocity = new Vec3(
                        (level.getRandom().nextDouble() - 0.5) * 0.2,
                        0.6 + level.getRandom().nextDouble() * 0.4,
                        (level.getRandom().nextDouble() - 0.5) * 0.2);
                level.addFreshEntity(spark);
            }
        });
    }
}