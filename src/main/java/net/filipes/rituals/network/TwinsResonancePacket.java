package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.SolarStormcellEntity;
import net.filipes.rituals.item.custom.LunarBladeItem;
import net.filipes.rituals.item.custom.SolarBladeItem;
import net.filipes.rituals.util.MuteTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TwinsResonancePacket implements CustomPacketPayload {

    public static final Type<TwinsResonancePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "twins_resonance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TwinsResonancePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TwinsResonancePacket());

    public static final Set<UUID> PENDING_RESONANCE = new HashSet<>();

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TwinsResonancePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            ItemStack main = player.getMainHandItem();
            ItemStack off  = player.getOffhandItem();
            boolean hasSolar = (main.getItem() instanceof SolarBladeItem && ModDataComponents.getStage(main) >= 5)
                    || (off.getItem() instanceof SolarBladeItem && ModDataComponents.getStage(off) >= 5);
            boolean hasLunar = (main.getItem() instanceof LunarBladeItem && ModDataComponents.getStage(main) >= 5)
                    || (off.getItem() instanceof LunarBladeItem && ModDataComponents.getStage(off) >= 5);
            if (!hasSolar || !hasLunar) return;

            ServerLevel level = (ServerLevel) player.level();

            List<SolarStormcellEntity> stormcells = level.getEntitiesOfClass(
                    SolarStormcellEntity.class,
                    player.getBoundingBox().inflate(200),
                    e -> e.getOwnerId() == player.getId() && e.isAlive()
            );

            if (!stormcells.isEmpty()) {
                stormcells.get(0).activateResonance();
            } else {
                PENDING_RESONANCE.add(player.getUUID());
            }
        });
    }

    public static void onPlayerDisconnect(UUID uuid) {
        PENDING_RESONANCE.remove(uuid);
    }
}