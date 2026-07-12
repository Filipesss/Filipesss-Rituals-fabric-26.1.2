package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C. Now carries the caster's entity id so every receiving client can
 * start the animation for the CORRECT entity via ShadeshatterAnimTracker,
 * rather than assuming it's always about the local player.
 *
 * IMPORTANT: this must be broadcast to every player tracking the caster
 * (PlayerLookup.tracking(player)) AND the caster themself — not just sent
 * to the caster alone — or nobody else will ever see the animation start.
 * See the send-site change below.
 */
public record ShadeshatterSpellStartPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ShadeshatterSpellStartPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "shadeshatter_spell_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShadeshatterSpellStartPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShadeshatterSpellStartPacket::entityId,
                    ShadeshatterSpellStartPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}