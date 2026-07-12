package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record ShadeshatterMimicTriggerPacket() implements CustomPacketPayload {

    public static final Type<ShadeshatterMimicTriggerPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_mimic_trigger"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterMimicTriggerPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterMimicTriggerPacket());

    private static final int   CHARGE_SOUND_COUNT   = 7;
    private static final long  CHARGE_SOUND_SPACING_MS = 300L;
    private static final float CHARGE_SOUND_PITCH_START = 0.5f;
    private static final float CHARGE_SOUND_PITCH_STEP  = 0.1f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShadeshatterMimicTriggerPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ShadeshatterMimicStartPacket startPkt = new ShadeshatterMimicStartPacket(player.getId());
            ServerPlayNetworking.send(player, startPkt);
            for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(tracker, startPkt);
            }

            playChargeSounds(player, ctx.server(), 0);
        });
    }

    private static void playChargeSounds(ServerPlayer player, MinecraftServer server, int index) {
        if (index >= CHARGE_SOUND_COUNT || player.isRemoved()) return;

        ServerLevel level = player.level();
        float pitch = CHARGE_SOUND_PITCH_START + index * CHARGE_SOUND_PITCH_STEP;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BREEZE_CHARGE, SoundSource.PLAYERS, 1.0f, pitch);

        CompletableFuture.delayedExecutor(CHARGE_SOUND_SPACING_MS, TimeUnit.MILLISECONDS).execute(() ->
                server.execute(() -> playChargeSounds(player, server, index + 1))
        );
    }
}