package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PulseBlasterShotgunPacket implements CustomPacketPayload {

    public static final Type<PulseBlasterShotgunPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_shotgun"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PulseBlasterShotgunPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PulseBlasterShotgunPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long  COOLDOWN_MS = 8_000L;
    private static final float SPREAD      = 0.28f;
    private static final float BEAM_SPEED  = 1.5f;

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PulseBlasterShotgunPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PulseBlasterItem)) return;
            if (ModDataComponents.getStage(stack) < 4) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;

            int ammo = PulseBlasterItem.getLiveAmmo(uuid, stack);
            if (ammo <= 0) return;
            SERVER_COOLDOWNS.put(uuid, now);

            Vec3   look   = player.getLookAngle();
            Vec3   eye    = new Vec3(player.getX(), player.getEyeY() - 0.1, player.getZ());
            Vec3   helper = Math.abs(look.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3   right  = look.cross(helper).normalize();
            Vec3   up     = right.cross(look).normalize();

            for (int i = 0; i < ammo; i++) {
                double angle  = Math.random() * 2.0 * Math.PI;
                double radius = Math.random() * SPREAD;

                Vec3 dir = look
                        .add(right.scale(Math.cos(angle) * radius))
                        .add(up.scale(   Math.sin(angle) * radius))
                        .normalize();

                Vec3 origin = eye
                        .add(right.scale((Math.random() - 0.5) * 0.3))
                        .add(up.scale(   (Math.random() - 0.5) * 0.3));

                PulseBlasterBeamEntity beam = new PulseBlasterBeamEntity(
                        ModEntities.PULSE_BLASTER_BEAM, player.level());
                beam.setOwner(player);
                beam.setPos(origin.x, origin.y, origin.z);
                beam.setDeltaMovement(dir.scale(BEAM_SPEED));
                player.level().addFreshEntity(beam);
            }

            PulseBlasterItem.setAmmo(stack, 0);
            PulseBlasterItem.clearActiveAmmo(uuid);
            PulseBlasterItem.syncAmmo(player, 0);

            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 0.75f);
        });
    }
}