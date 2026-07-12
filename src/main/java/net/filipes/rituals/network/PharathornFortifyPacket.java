package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.PharathornItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PharathornFortifyPacket implements CustomPacketPayload {

    public static final Type<PharathornFortifyPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "pharathorn_fortify"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharathornFortifyPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PharathornFortifyPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public  static final long COOLDOWN_MS = 20_000L;

    private static final int DURATION_TICKS = 80;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PharathornFortifyPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof PharathornItem)) return;
            int stage = ModDataComponents.getStage(stack);
            if (stage < 2) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION_TICKS, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,  DURATION_TICKS, 4, false, true));

            ServerLevel level = (ServerLevel) player.level();
            double px = player.getX();
            double py = player.getY() + player.getBbHeight() * 0.5;
            double pz = player.getZ();

            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2.0 / 10) * i;
                double radius = 0.6;
                net.filipes.rituals.entity.custom.SparkEntity spark =
                        new net.filipes.rituals.entity.custom.SparkEntity(
                                net.filipes.rituals.entity.ModEntities.SPARK, level,
                                px + Math.cos(angle) * radius,
                                py - player.getBbHeight() * 0.3,
                                pz + Math.sin(angle) * radius);
                spark.applyPreset(net.filipes.rituals.entity.custom.SparkPresets.PHARATHORN_IMMUNITY);
                spark.forcedVelocity = new net.minecraft.world.phys.Vec3(
                        Math.cos(angle) * 0.25,
                        0.4 + level.getRandom().nextDouble() * 0.3,
                        Math.sin(angle) * 0.25);
                level.addFreshEntity(spark);
            }

            for (int i = 0; i < 5; i++) {
                double theta = level.getRandom().nextDouble() * Math.PI * 2.0;
                net.filipes.rituals.entity.custom.SparkEntity spark =
                        new net.filipes.rituals.entity.custom.SparkEntity(
                                net.filipes.rituals.entity.ModEntities.SPARK, level,
                                px, py, pz);
                spark.applyPreset(net.filipes.rituals.entity.custom.SparkPresets.PHARATHORN_IMMUNITY);
                spark.forcedVelocity = new net.minecraft.world.phys.Vec3(
                        Math.cos(theta) * 0.15,
                        0.6 + level.getRandom().nextDouble() * 0.4,
                        Math.sin(theta) * 0.15);
                level.addFreshEntity(spark);
            }

            level.playSound(null, px, py, pz,
                    net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f, 0.6f);
        });
    }
}