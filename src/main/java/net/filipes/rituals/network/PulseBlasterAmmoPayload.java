package net.filipes.rituals.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C packet: tells the client the blaster's live ammo count.
 * ammo == -1 means "firing stopped — revert to reading NBT".
 */
public record PulseBlasterAmmoPayload(int ammo) implements CustomPayload {

    public static final CustomPayload.Id<PulseBlasterAmmoPayload> ID =
            new CustomPayload.Id<>(Identifier.of("rituals", "pulse_blaster_beam"));

    public static final PacketCodec<RegistryByteBuf, PulseBlasterAmmoPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER,
                    PulseBlasterAmmoPayload::ammo,
                    PulseBlasterAmmoPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}