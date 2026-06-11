package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPreset;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record BlightDrainPacket(int targetId) implements CustomPacketPayload {

    public static final Type<BlightDrainPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_drain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightDrainPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlightDrainPacket::targetId,
            BlightDrainPacket::new
    );

    public static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath("rituals", "blight_drain");
    public static final Map<UUID, Long> ACTIVE_DRAINS = new HashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightDrainPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer caster = ctx.player();
        MinecraftServer server = ctx.server();

        server.execute(() -> {
            if (!(caster.getMainHandItem().getItem() instanceof BlightspearItem)) return;

            ServerLevel level = (ServerLevel) caster.level();
            Entity targetEntity = level.getEntity(pkt.targetId());
            boolean isMannequin = targetEntity != null && targetEntity.getType().toString().contains("mannequin");

            if (targetEntity instanceof LivingEntity target && (target instanceof Player || isMannequin)) {
                target.hurt(caster.damageSources().magic(), 4.0f);

                var attribute = caster.getAttribute(Attributes.MAX_HEALTH);
                if (attribute != null) {
                    attribute.removeModifier(MODIFIER_ID);
                    attribute.addPermanentModifier(new AttributeModifier(
                            MODIFIER_ID,
                            4.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ));

                    caster.heal(4.0f);
                    ACTIVE_DRAINS.put(caster.getUUID(), level.getGameTime() + 600);

                    double targetCenterY = target.getY() + (target.getBbHeight() * 0.5);
                    for (int i = 0; i < 6; i++) {
                        double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
                        double horizSpd = 0.14 + level.getRandom().nextDouble() * 0.22;
                        double vertSpd = (level.getRandom().nextDouble() - 0.4) * 0.08;

                        SparkEntity targetSpurt = new SparkEntity(ModEntities.SPARK, level, target.getX(), targetCenterY, target.getZ());
                        targetSpurt.applyPreset(SparkPresets.BLIGHT_DRAIN_SINGLE);
                        targetSpurt.forcedVelocity = new Vec3(
                                Math.cos(angle) * horizSpd,
                                vertSpd,
                                Math.sin(angle) * horizSpd
                        );
                        level.addFreshEntity(targetSpurt);
                    }

                    Vec3 targetTorso = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 casterTorso = caster.position().add(0, caster.getBbHeight() * 0.5, 0);
                    Vec3 travelVector = casterTorso.subtract(targetTorso);

                    if (travelVector.lengthSqr() > 0.01) {
                        Vec3 flatDir = new Vec3(travelVector.x, 0, travelVector.z).normalize();
                        Vec3 lateralPerpend = new Vec3(-flatDir.z, 0, flatDir.x).normalize();

                        Vec3 curveOffset1 = lateralPerpend.scale(1.4).add(0, 0.8, 0);
                        spawnCurvedSiphon(level, target, caster, curveOffset1, 22, SparkPresets.BLIGHT_DRAIN, server);

                        Vec3 curveOffset2 = lateralPerpend.scale(-1.4).add(0, 0.4, 0);
                        spawnCurvedSiphon(level, target, caster, curveOffset2, 28, SparkPresets.BLIGHT_DRAIN, server);
                    }

                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), targetCenterY, target.getZ(), 8, 0.25, 0.3, 0.25, 0.05);
                    level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + (caster.getBbHeight() * 0.5), caster.getZ(), 4, 0.3, 0.4, 0.3, 0.0);
                }
            }
        });
    }

    private static void spawnCurvedSiphon(ServerLevel level, LivingEntity target, ServerPlayer caster,
                                          Vec3 curveOffset, int maxTicks, SparkPreset preset, MinecraftServer server) {
        Vec3 start = target.position().add(0, target.getBbHeight() * 0.5, 0);

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, start.x, start.y, start.z);
        spark.applyPreset(preset);
        spark.setNoGravity(true);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;

        level.addFreshEntity(spark);

        runDrainCurveTicker(target, caster, spark, 0, maxTicks, curveOffset, server);
    }

    private static void runDrainCurveTicker(LivingEntity target, ServerPlayer caster, SparkEntity spark,
                                            int currentTick, int maxTicks, Vec3 curveOffset, MinecraftServer server) {

        if (currentTick >= maxTicks || !caster.isAlive() || !spark.isAlive()) {
            if (spark.isAlive()) {
                if (currentTick >= maxTicks && caster.isAlive()) {
                    triggerArrivalBurst((ServerLevel) caster.level(), caster);
                }
                spark.discard();
            }
            return;
        }

        ServerLevel level = (ServerLevel) caster.level();

        Vec3 startPos = target.isAlive() ? target.position().add(0, target.getBbHeight() * 0.5, 0) : spark.position();
        Vec3 endPos = caster.position().add(0, caster.getBbHeight() * 0.5, 0);

        double pct = (double) currentTick / maxTicks;
        Vec3 baseLinePos = startPos.lerp(endPos, pct);

        double waveEnvelope = Math.sin(Math.PI * pct);
        Vec3 finalArcPos = baseLinePos.add(curveOffset.scale(waveEnvelope));

        spark.setPos(finalArcPos.x, finalArcPos.y, finalArcPos.z);
        spark.setDeltaMovement(Vec3.ZERO);
        spark.forcedVelocity = Vec3.ZERO;

        if (level.getRandom().nextInt(2) == 0) {
            level.sendParticles(ParticleTypes.WARPED_SPORE, finalArcPos.x, finalArcPos.y, finalArcPos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() ->
                server.execute(() -> runDrainCurveTicker(target, caster, spark, currentTick + 1, maxTicks, curveOffset, server))
        );
    }

    private static void triggerArrivalBurst(ServerLevel level, ServerPlayer caster) {
        double centerY = caster.getY() + (caster.getBbHeight() * 0.5);
        for (int i = 0; i < 4; i++) {
            double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
            double horizSpd = 0.06 + level.getRandom().nextDouble() * 0.12;
            double vertSpd = 0.04 + level.getRandom().nextDouble() * 0.12;

            SparkEntity impactSpark = new SparkEntity(ModEntities.SPARK, level, caster.getX(), centerY, caster.getZ());
            impactSpark.applyPreset(SparkPresets.BLIGHT_DRAIN_SINGLE);
            impactSpark.forcedVelocity = new Vec3(
                    Math.cos(angle) * horizSpd,
                    vertSpd,
                    Math.sin(angle) * horizSpd
            );
            level.addFreshEntity(impactSpark);
        }
    }
}