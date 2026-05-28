package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.LunarFragmentEntity;
import net.filipes.rituals.entity.custom.SolarStormcellEntity;
import net.filipes.rituals.item.custom.LunarBladeItem;
import net.filipes.rituals.item.custom.SolarBladeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TwinsActionTwoPacket implements CustomPacketPayload {

    public static final Type<TwinsActionTwoPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "twins_action_two"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TwinsActionTwoPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.isSolarCast),
                    buf -> new TwinsActionTwoPacket(buf.readBoolean())
            );
    public final boolean isSolarCast;
    private static final int  FRAGMENT_COUNT    = 4;
    private static final long SPAWN_DEBOUNCE_MS = 500L;


    private static final Map<UUID, Long> LAST_SPAWN_TIME = new HashMap<>();

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public TwinsActionTwoPacket(boolean isSolarCast) {
        this.isSolarCast = isSolarCast;
    }
    public TwinsActionTwoPacket() { this(false); }

    public static void handle(TwinsActionTwoPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            ItemStack stack = player.getMainHandItem();
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();

            if (!pkt.isSolarCast) {
                if (ModDataComponents.getStage(stack) < 1) return;

                List<LunarFragmentEntity> orbiting = level.getEntitiesOfClass(
                        LunarFragmentEntity.class,
                        player.getBoundingBox().inflate(5),
                        f -> f.getOwnerId() == player.getId() && !f.isLaunched()
                );

                if (!orbiting.isEmpty()) {
                    LivingEntity target = findLookedAtEntity(player, level);
                    if (target != null) {
                        int launchIndex = FRAGMENT_COUNT - orbiting.size();
                        orbiting.get(0).launch(target, launchIndex);
                    }
                } else {
                    long now = System.currentTimeMillis();
                    Long last = LAST_SPAWN_TIME.get(uuid);
                    if (last != null && now - last < SPAWN_DEBOUNCE_MS) return;
                    LAST_SPAWN_TIME.put(uuid, now);

                    for (int i = 0; i < FRAGMENT_COUNT; i++) {
                        LunarFragmentEntity fragment =
                                new LunarFragmentEntity(ModEntities.LUNAR_FRAGMENT, level);
                        fragment.setOwner(player);
                        fragment.setSlot(i);
                        fragment.setPos(player.getX(), player.getY() + 1.0, player.getZ());
                        level.addFreshEntity(fragment);
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.7f);
                    player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                }

            } else {

                ItemStack solarStack = stack.getItem() instanceof SolarBladeItem ? stack
                        : player.getOffhandItem().getItem() instanceof SolarBladeItem
                        ? player.getOffhandItem() : stack;
                if (ModDataComponents.getStage(solarStack) < 1) return;

                long now = System.currentTimeMillis();
                Long last = LAST_SPAWN_TIME.get(uuid);
                if (last != null && now - last < SPAWN_DEBOUNCE_MS) return;
                LAST_SPAWN_TIME.put(uuid, now);

                LivingEntity target = findLookedAtEntity(player, level);

                List<LunarFragmentEntity> orbiting = level.getEntitiesOfClass(
                        LunarFragmentEntity.class,
                        player.getBoundingBox().inflate(5),
                        f -> f.getOwnerId() == player.getId() && !f.isLaunched()
                );

                float damageMultiplier = 1.0f;
                int toConsume = 0;
                if (orbiting.size() >= 2) {
                    damageMultiplier = 0.75f;
                    toConsume = 2;
                } else if (orbiting.size() == 1) {
                    damageMultiplier = 0.75f;
                    toConsume = 1;
                }

                for (int i = 0; i < toConsume; i++) {
                    orbiting.get(i).consumeForSolar();
                }

                SolarStormcellEntity stormcell =
                        new SolarStormcellEntity(ModEntities.SOLAR_STORMCELL, level);
                stormcell.setOwner(player);
                stormcell.setPos(player.getX(), player.getY() + 1.0, player.getZ());
                stormcell.setDamageMultiplier(damageMultiplier);
                stormcell.launch(target, player.getLookAngle());
                level.addFreshEntity(stormcell);

                ItemStack offhand = player.getOffhandItem();
                boolean bothEquipped = offhand.getItem() instanceof LunarBladeItem;
                if (bothEquipped || TwinsResonancePacket.PENDING_RESONANCE.remove(player.getUUID())) {
                    stormcell.activateResonance();
                }

                if (orbiting.size() <= toConsume) {
                    ServerPlayNetworking.send(player, new TwinsStartCooldownPacket());
                }

                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(player.getDeltaMovement()
                        .add(-look.x * 0.5, 0.15, -look.z * 0.5));
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            }
        });
    }

    private static @Nullable LivingEntity findLookedAtEntity(ServerPlayer player, ServerLevel level) {
        Vec3 eye   = player.getEyePosition();
        Vec3 look  = player.getLookAngle();
        double range = 20.0;
        Vec3 end   = eye.add(look.scale(range));

        AABB searchBox = new AABB(eye, end).inflate(3.0);

        double bestAlong = Double.MAX_VALUE;
        LivingEntity best = null;

        for (Entity e : level.getEntities(player, searchBox)) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;

            Vec3 center   = le.getBoundingBox().getCenter();
            Vec3 toCenter = center.subtract(eye);
            double along  = toCenter.dot(look);

            if (along < 0 || along > range) continue;

            Vec3 projected  = eye.add(look.scale(along));
            double perpDist = projected.distanceTo(center);

            if (perpDist < 2.5 && along < bestAlong) {
                bestAlong = along;
                best      = le;
            }
        }
        return best;
    }

    public static void onPlayerDisconnect(UUID uuid) {
        LAST_SPAWN_TIME.remove(uuid);
    }
}