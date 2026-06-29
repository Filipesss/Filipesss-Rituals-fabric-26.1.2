package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.DashStabEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PharathornDashPacket implements CustomPacketPayload {

    public static final Type<PharathornDashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pharathorn_dash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharathornDashPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PharathornDashPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long  COOLDOWN_MS   = 25_000L;
    public  static final float DASH_DISTANCE = 7.0f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PharathornDashPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof PharathornItem)) return;
            if (ModDataComponents.getStage(held) < 5) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            Vec3 look  = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            Vec3 start = player.position();
            Vec3 end   = start.add(look.scale(DASH_DISTANCE));

            ServerLevel level = (ServerLevel) player.level();

            ClipContext clipCtx = new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            BlockHitResult hitResult = level.clip(clipCtx);
            Vec3 actualEnd = (hitResult.getType() == HitResult.Type.BLOCK)
                    ? hitResult.getLocation()
                    : end;

            double actualDist = actualEnd.distanceTo(start);

            DashStabEntity trail = new DashStabEntity(
                    ModEntities.DASH_STAB, level, player, start, actualEnd
            );
            level.addFreshEntity(trail);

            for (int i = 0; i < 2; i++) {
                double speed = 1.5 + i * 0.15;

                SparkEntity main = new SparkEntity(ModEntities.SPARK, level,
                        start.x, start.y + 0.9, start.z);
                main.applyPreset(SparkPresets.PHARATHORN_DASH_MAIN);
                main.forcedVelocity = look.scale(speed);
                main.setNoGravity(true);
                main.maxLifetime = (int) Math.ceil(actualDist / speed) + 1;

                level.addFreshEntity(main);
            }

            Random rand = new Random();
            for (int i = 0; i < 8; i++) {
                double angle     = rand.nextDouble() * 2.0 * Math.PI;
                double horizSpd  = 0.25 + rand.nextDouble() * 0.25;
                double vertSpd   = 0.10 + rand.nextDouble() * 0.55;

                SparkEntity spark = new SparkEntity(ModEntities.SPARK, level,
                        actualEnd.x, actualEnd.y + 0.5, actualEnd.z);
                spark.applyPreset(SparkPresets.PHARATHORN_DASH);
                spark.forcedVelocity = new Vec3(
                        Math.cos(angle) * horizSpd,
                        vertSpd,
                        Math.sin(angle) * horizSpd
                );
                level.addFreshEntity(spark);
            }

            player.setDeltaMovement(look.scale(1.8));
            player.hurtMarked = true;
        });
    }
}