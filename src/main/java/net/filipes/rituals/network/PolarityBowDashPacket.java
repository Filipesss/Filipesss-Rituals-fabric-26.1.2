package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.PolarityShieldEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PolarityBowDashPacket implements CustomPacketPayload {

    public static final Type<PolarityBowDashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "polarity_bow_dash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PolarityBowDashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PolarityBowDashPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final long COOLDOWN_MS = 10_000L;
    public static final float DASH_DISTANCE = 6.5f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PolarityBowDashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof PolarityBowItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 6) return;

            UUID uuid = player.getUUID();
            long now = System.currentTimeMillis();

            Long lastGlobal = SERVER_COOLDOWNS.get(uuid);
            if (lastGlobal != null && now - lastGlobal < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            CustomModelData current = held.get(DataComponents.CUSTOM_MODEL_DATA);
            boolean isRed = current != null
                    && !current.flags().isEmpty()
                    && current.flags().get(0);

            Vec3 look = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 rightDir = new Vec3(-look.z, 0, look.x);
            Vec3 dashDir = isRed ? rightDir : rightDir.scale(-1);

            ServerLevel level = player.level();
            Vec3 start = player.position();
            Vec3 estimatedEnd = start.add(dashDir.scale(DASH_DISTANCE));

            int visualColor = isRed ? 0xFF1A1A : 0x00B2FF;
            DustParticleOptions dustParticle = new DustParticleOptions(visualColor, 1.2f);
            for (int i = 0; i <= 6; i++) {
                Vec3 particlePos = start.lerp(estimatedEnd, (double) i / 6);
                level.sendParticles(dustParticle, particlePos.x, particlePos.y + 0.6, particlePos.z, 1, 0, 0.05, 0, 0);
            }

            for (int i = 0; i < 2; i++) {
                double speed = 1.4 + (i * 0.2);
                SparkEntity trailSpark = new SparkEntity(ModEntities.SPARK, level, start.x, start.y + 0.9, start.z);
                trailSpark.applyPreset(isRed ? SparkPresets.POLARITY_RED_DASH : SparkPresets.POLARITY_BLUE_DASH);
                trailSpark.forcedVelocity = dashDir.scale(speed);
                trailSpark.setNoGravity(true);
                trailSpark.maxLifetime = 6;
                level.addFreshEntity(trailSpark);
            }

            PolarityShieldEntity shield = new PolarityShieldEntity(ModEntities.POLARITY_SHIELD, level, player, isRed);
            level.addFreshEntity(shield);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.GENERIC_DASH, SoundSource.PLAYERS, 0.7f, isRed ? 1.0f : 1.4f);

            player.setDeltaMovement(dashDir.scale(1.4));
            player.hurtMarked = true;
        });
    }
}