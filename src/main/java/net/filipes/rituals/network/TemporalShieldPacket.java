package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.TemporalShieldEntity;
import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporalShieldPacket implements CustomPacketPayload {

    public static final Type<TemporalShieldPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_shield_barrier"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalShieldPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalShieldPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final long COOLDOWN_MS = 25_000L;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TemporalShieldPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof TemporalGlassreaverItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 2) return;

            UUID uuid = player.getUUID();
            long now = System.currentTimeMillis();

            Long lastGlobal = SERVER_COOLDOWNS.get(uuid);
            if (lastGlobal != null && now - lastGlobal < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = player.level();

            Vec3 lookDir = player.getLookAngle();
            Vec3 spawnPos = player.position().add(lookDir.scale(2.0)).add(0, 1.2, 0);

            TemporalShieldEntity shield = new TemporalShieldEntity(ModEntities.TEMPORAL_SHIELD, level, player);
            shield.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            shield.setYRot(player.getYRot());
            shield.setXRot(player.getXRot());
            level.addFreshEntity(shield);

            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 0.6f);
        });
    }
}