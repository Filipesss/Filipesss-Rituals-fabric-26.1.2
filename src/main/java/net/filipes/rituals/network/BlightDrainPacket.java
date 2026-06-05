package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        ctx.server().execute(() -> {
            if (!(caster.getMainHandItem().getItem() instanceof BlightspearItem)) return;

            Entity targetEntity = caster.level().getEntity(pkt.targetId());
            boolean isMannequin = targetEntity != null && targetEntity.getType().toString().contains("mannequin");

            if (targetEntity instanceof LivingEntity target && (target instanceof Player || isMannequin)) {
                target.hurt(caster.damageSources().magic(), 4.0f);

                var attribute = caster.getAttribute(Attributes.MAX_HEALTH);
                if (attribute != null) {
                    // 2. removeModifier now takes the Identifier directly
                    attribute.removeModifier(MODIFIER_ID);

                    // 3. Operation.ADDITION is now Operation.ADD_VALUE
                    attribute.addPermanentModifier(new AttributeModifier(
                            MODIFIER_ID,
                            4.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ));

                    caster.heal(4.0f);
                    ACTIVE_DRAINS.put(caster.getUUID(), caster.level().getGameTime() + 600);
                }
            }
        });
    }
}