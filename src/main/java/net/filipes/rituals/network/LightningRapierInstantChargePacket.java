package net.filipes.rituals.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Sent client → server when the player presses actionOne with a Stage 4+ Lightning Rapier.
 * The server handler (in Rituals.java) sets the charge component to 6 (supercharged).
 */
public record LightningRapierInstantChargePacket() implements CustomPacketPayload {

    public static final Type<LightningRapierInstantChargePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_instant_charge"));

    public static final StreamCodec<ByteBuf, LightningRapierInstantChargePacket> CODEC =
            StreamCodec.unit(new LightningRapierInstantChargePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}