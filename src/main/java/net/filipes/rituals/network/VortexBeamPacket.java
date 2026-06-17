package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.VortexBoomEntity;
import net.filipes.rituals.item.custom.VortexEdgeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class VortexBeamPacket implements CustomPacketPayload {

    public static final Type<VortexBeamPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "vortex_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VortexBeamPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new VortexBeamPacket());

    private static final Map<UUID, Long> SERVER_COOLDOWNS = new HashMap<>();
    public static final  long  COOLDOWN_MS = 15_000L;
    private static final float DAMAGE      = 8f;
    private static final float MAX_RANGE   = 20f;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(VortexBeamPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();

        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof VortexEdgeItem)) return;

            int stage = ModDataComponents.getStage(stack);
            if (stage < 3) return;

            UUID uuid = player.getUUID();
            long now  = System.currentTimeMillis();
            Long last = SERVER_COOLDOWNS.get(uuid);
            if (last != null && now - last < COOLDOWN_MS) return;
            SERVER_COOLDOWNS.put(uuid, now);

            ServerLevel level  = (ServerLevel) player.level();
            Vec3        origin = player.getEyePosition();
            Vec3        dir    = player.getLookAngle();
            Vec3        end    = origin.add(dir.scale(MAX_RANGE));

            // Piercing — damage every entity whose hitbox intersects the ray
            AABB searchBox = new AABB(origin, end).inflate(1.0);
            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive()
            );

            for (LivingEntity candidate : candidates) {
                Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(origin, end);
                if (hit.isPresent()) {
                    candidate.hurtServer(level, level.damageSources().playerAttack(player), DAMAGE);
                    // Knock targets back along the beam direction
                    candidate.push(dir.x * 1.5, dir.y * 0.4, dir.z * 1.5);
                }
            }

            // Spawn the visual entity at the player's eye
            VortexBoomEntity boom = new VortexBoomEntity(
                    ModEntities.VORTEX_BOOM, level,
                    origin.x, origin.y, origin.z
            );
            boom.setBeamDirection(player.getYRot(), player.getXRot());
            boom.setBeamLength(MAX_RANGE);
            level.addFreshEntity(boom);

            // Warden sonic boom sound, slightly higher pitched
            level.playSound(null, origin.x, origin.y, origin.z,
                    SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.PLAYERS,
                    1.0f, 1.3f);
        });
    }
}