package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.PolarityTornadoBlueEntity;
import net.filipes.rituals.entity.custom.PolarityTornadoRedEntity;
import net.filipes.rituals.item.custom.PolarityBowItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class PolarityTornadoLaunchPacket implements CustomPacketPayload {

    public static final Type<PolarityTornadoLaunchPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "polarity_tornado_launch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PolarityTornadoLaunchPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PolarityTornadoLaunchPacket());

    private static final int LIFETIME_TICKS = 160;
    private static final float LAUNCH_SPEED  = 0.7f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PolarityTornadoLaunchPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(player.getMainHandItem().getItem() instanceof PolarityBowItem)) return;
            int stage = ModDataComponents.getStage(held);
            if (stage < 5) return;

            boolean isRed = PolarityBowItem.isRedPolarity(player.getMainHandItem());
            ServerLevel level = player.level();

            Vec3 look  = player.getLookAngle();
            Vec3 spawn = player.position()
                    .add(0, 0.8, 0)
                    .add(look.scale(1.2));

            Vec3 velocity = look.scale(LAUNCH_SPEED);

            if (isRed) {
                PolarityTornadoRedEntity tornado =
                        new PolarityTornadoRedEntity(level, spawn, LIFETIME_TICKS, 1.0f);
                tornado.launch(velocity);
                level.addFreshEntity(tornado);
            } else {
                PolarityTornadoBlueEntity tornado =
                        new PolarityTornadoBlueEntity(level, spawn, LIFETIME_TICKS, 1.0f);
                tornado.launch(velocity);
                level.addFreshEntity(tornado);
            }

            level.playSound(null, spawn.x, spawn.y, spawn.z,
                    SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.9f, isRed ? 0.75f : 1.25f);
        });
    }
}