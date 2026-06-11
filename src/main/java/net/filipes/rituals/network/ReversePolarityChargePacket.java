package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.filipes.rituals.sound.ModSounds;
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
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ReversePolarityChargePacket implements CustomPacketPayload {

    public static final Type<ReversePolarityChargePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "reverse_polarity_charge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReversePolarityChargePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ReversePolarityChargePacket());

    // UUID → expiry timestamp; expires after 30s if they never fire
    private static final Map<UUID, Long> CHARGED_UNTIL = new HashMap<>();
    private static final long CHARGE_WINDOW_MS = 30_000L;

    public static boolean consumeCharge(UUID uuid) {
        Long until = CHARGED_UNTIL.remove(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // Per-spark chaotic orbit parameters
    private record OrbitParams(double angleOffset, double speed, double radius,
                               double vertPhase, double vertFreq) {}

    public static void handle(ReversePolarityChargePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            if (!(player.getMainHandItem().getItem() instanceof PolarityBowItem)) return;

            CHARGED_UNTIL.put(player.getUUID(), System.currentTimeMillis() + CHARGE_WINDOW_MS);

            ServerLevel level = player.level();
            Vec3 pos = player.position();
            double cx = pos.x, cy = pos.y + 1.0, cz = pos.z;


            level.sendParticles(ParticleTypes.ENCHANT,
                    cx, cy, cz, 20, 0.4, 0.6, 0.4, 2.2);
            level.sendParticles(new DustParticleOptions(0xFFDD00, 2.2f),
                    cx, cy, cz, 14, 0.35, 0.5, 0.35, 0.7);
            level.sendParticles(ParticleTypes.END_ROD,
                    cx, cy, cz, 8, 0.25, 0.35, 0.25, 0.15);

            level.playSound(null, cx, cy, cz,
                    ModSounds.LIGHTNING_BOLT_2, SoundSource.PLAYERS, 0.8f, 1.3f);
            level.playSound(null, cx, cy, cz,
                    ModSounds.POLARITY_CHANGE, SoundSource.PLAYERS, 0.6f, 1.5f);

        });
    }




}