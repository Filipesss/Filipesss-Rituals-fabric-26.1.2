package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SpiralStabEntity;
import net.filipes.rituals.item.custom.PharathornItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiralStabPacket implements CustomPacketPayload {

    public static final Type<SpiralStabPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "spiral_stab"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiralStabPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new SpiralStabPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 12_000L;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SpiralStabPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(player.getMainHandItem().getItem() instanceof PharathornItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 6) return;


            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            SpiralStabEntity spiral = new SpiralStabEntity(
                    ModEntities.SPIRAL_STAB, level,
                    player,
                    player.getX(), player.getY(), player.getZ()
            );
            level.addFreshEntity(spiral);

            player.swing(InteractionHand.MAIN_HAND, true);
        });
    }
}