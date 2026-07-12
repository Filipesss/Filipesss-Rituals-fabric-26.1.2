package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.item.custom.ShadeshatterItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadeshatterSpellPacket implements CustomPacketPayload {

    public static final Type<ShadeshatterSpellPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterSpellPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new ShadeshatterSpellPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long            COOLDOWN_MS      = 4_000L;

    private static final int SPAWN_DELAY = 42;
    private static final int SECOND_CHARGE_DELAY = 22;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ShadeshatterSpellPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof ShadeshatterItem)) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BREEZE_CHARGE, SoundSource.PLAYERS, 1.0f, 0.85f + player.getRandom().nextFloat() * 0.1f);

            ShadeshatterSpellStartPacket startPkt = new ShadeshatterSpellStartPacket(player.getId());
            ServerPlayNetworking.send(player, startPkt);
            for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(tracker, startPkt);
            }

            int currentServerTick = ctx.server().getTickCount();

            ShadeshatterSpellHandler.scheduleSecondCharge(
                    player.getUUID(),
                    currentServerTick + SECOND_CHARGE_DELAY);

            ShadeshatterSpellHandler.schedule(
                    player.getUUID(),
                    currentServerTick + SPAWN_DELAY);
        });
    }
}