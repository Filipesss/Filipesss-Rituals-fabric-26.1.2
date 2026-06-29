package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.CinderboltBeamEntity;
import net.filipes.rituals.item.custom.CinderboltItem;
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

public class FireCinderboltBeamPacket implements CustomPacketPayload {

    public static final Type<FireCinderboltBeamPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "fire_cinderbolt_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireCinderboltBeamPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new FireCinderboltBeamPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    private static final int   BEAM_DURATION = 60;
    private static final float BEAM_DAMAGE   = 3.5f;
    private static final float BEAM_HP_PCT   = 1.5f;

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(FireCinderboltBeamPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof CinderboltItem)) return;
            if (ModDataComponents.getStage(stack) < 5) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level = (ServerLevel) player.level();

            float yaw   = (float)((player.yHeadRot + 90.0) * Math.PI / 180.0);
            float pitch = (float)(-player.getXRot()         * Math.PI / 180.0);

            CinderboltBeamEntity beam = new CinderboltBeamEntity(
                    ModEntities.CINDERBOLT_BEAM, level,
                    player,
                    player.getX(),
                    player.getY() + CinderboltBeamEntity.CASTER_Y_OFFSET,
                    player.getZ(),
                    yaw, pitch,
                    BEAM_DURATION,
                    BEAM_DAMAGE,
                    BEAM_HP_PCT
            );
            beam.setFire(true);
            level.addFreshEntity(beam);
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