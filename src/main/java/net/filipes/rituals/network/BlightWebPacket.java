package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.BlightedPuddleEntity;
import net.filipes.rituals.item.custom.BlightspearItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

public record BlightWebPacket(int targetId) implements CustomPacketPayload {

    public static final Type<BlightWebPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "blight_web"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlightWebPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlightWebPacket::targetId,
            BlightWebPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BlightWebPacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof BlightspearItem)) return;

            ServerLevel level = player.level();
            Entity target = level.getEntity(pkt.targetId());

            // Validate that the entity exists and is within a reasonable maximum extended range (25 blocks)
            if (target == null || player.distanceToSqr(target) > 625.0) return;

            BlockPos feetPos = target.blockPosition();

            // 1. Safely place the cobweb only if the block can be replaced (air, tall grass, etc.)
            if (level.getBlockState(feetPos).canBeReplaced()) {
                level.setBlockAndUpdate(feetPos, Blocks.COBWEB.defaultBlockState());
            }

            // 2. Instantiate and spawn your BlightedPuddleEntity at the exact impact spot
            BlightedPuddleEntity puddle = new BlightedPuddleEntity(ModEntities.BLIGHTED_PUDDLE, level);
            puddle.setPos(target.getX(), target.getY(), target.getZ());
            puddle.setOwnerUUID(player.getUUID());
            level.addFreshEntity(puddle);

            // Audio cues
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 0.6f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.8f, 0.5f);
        });
    }
}