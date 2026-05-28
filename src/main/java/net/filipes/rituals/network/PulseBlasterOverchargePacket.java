package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.PulseBlasterItem;
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

public class PulseBlasterOverchargePacket implements CustomPacketPayload {

    public static final Type<PulseBlasterOverchargePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_overcharge"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PulseBlasterOverchargePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PulseBlasterOverchargePacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 30_000L;

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PulseBlasterOverchargePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PulseBlasterItem)) return;
            if (ModDataComponents.getStage(stack) < 5) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            PulseBlasterItem.overchargeExpiry.put(uuid,
                    now + PulseBlasterItem.OVERCHARGE_DURATION_MS);

            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.9f);
        });
    }
}