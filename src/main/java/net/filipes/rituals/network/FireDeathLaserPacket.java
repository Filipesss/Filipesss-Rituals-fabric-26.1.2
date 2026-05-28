package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.DeathLaserEntity;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.sound.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireDeathLaserPacket implements CustomPacketPayload {

    public static final Type<FireDeathLaserPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "fire_death_laser"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireDeathLaserPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new FireDeathLaserPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    private static final int   LASER_DURATION = 60;
    private static final float LASER_DAMAGE   = 4.0f;
    private static final float LASER_HP_PCT   = 2.0f;

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(FireDeathLaserPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PulseBlasterItem)) return;
            if (ModDataComponents.getStage(stack) < 6) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            float yaw   = (float)((player.yHeadRot + 90.0) * Math.PI / 180.0);
            float pitch = (float)(-player.getXRot()         * Math.PI / 180.0);

            DeathLaserEntity laser = new DeathLaserEntity(
                    ModEntities.DEATH_LASER, level,
                    player,
                    player.getX(),
                    player.getEyeY() - 0.4,
                    player.getZ(),
                    yaw, pitch,
                    LASER_DURATION,
                    LASER_DAMAGE,
                    LASER_HP_PCT
            );
            level.addFreshEntity(laser);
            level.playSound(
                    null,
                    player.getX(), player.getEyeY() - 0.4, player.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE,
                    SoundSource.PLAYERS,
                    1.3f,
                    1.2f
            );
            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        });
    }
}