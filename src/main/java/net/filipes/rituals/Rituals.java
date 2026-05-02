package net.filipes.rituals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.blocks.ModBlocks;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.command.RitualCommands;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.DeathLaserEntityRenderer;
import net.filipes.rituals.event.PlayerKillListener;
import net.filipes.rituals.item.ModItemGroups;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.network.*;
import net.filipes.rituals.particle.ModParticles;
import net.filipes.rituals.screen.ModMenuTypes;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.tooltip.ModTooltips;
import net.filipes.rituals.tooltip.TooltipRegistry;
import net.filipes.rituals.upgrade.KillUpgradeRegistry;
import net.filipes.rituals.upgrade.UpgradeRecipeRegistry;
import net.filipes.rituals.util.RosegoldPickaxeUsageEvent;
import net.filipes.rituals.worldgen.RitualWorldGen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Rituals implements ModInitializer {
	public static final String MOD_ID = "rituals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RitualConfig.load();

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
				RitualWorldGen.placeAllPedestals(server));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				RitualCommands.register(dispatcher));
		ModBlocks.registerModBlocks();
		ModMenuTypes.registerMenuTypes();
		TooltipRegistry.init();
		ModTooltips.register();
		ModDataComponents.register();
		UpgradeRecipeRegistry.registerAll();
		ModItems.registerModItems();
		ModStatusEffects.registerModStatusEffects();
		ModBlockEntities.registerModBlockEntities();
		ModItemGroups.registerItemGroups();
		ModEntities.registerModEntities();
		ModParticles.register();
		ModSounds.initialize();
		PlayerKillListener.register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ShadowguardItem.tickInvisibility();
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, killed) -> {
			if (!(source.getEntity() instanceof ServerPlayer attacker)) return;

			ItemStack stack = attacker.getMainHandItem();
			if (!(stack.getItem() instanceof ShadowguardItem)) return;

			int stage = ModDataComponents.getStage(stack);
			if (stage < 2) return;

			if (attacker.level().getRandom().nextFloat() < 0.50f) {
				attacker.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));

				ShadowguardItem.markInvisible(attacker.getUUID());
				ServerPlayNetworking.send(attacker, new ShadowguardInvisiblePacket());
			}
		});

		PayloadTypeRegistry.serverboundPlay().register(FireDeathLaserPacket.TYPE, FireDeathLaserPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(DoubleJumpPayload.ID, DoubleJumpPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PharathornDashPacket.TYPE, PharathornDashPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(
				LightningRapierTeleportPacket.TYPE,
				LightningRapierTeleportPacket.CODEC
		);

		ServerPlayNetworking.registerGlobalReceiver(
				LightningRapierTeleportPacket.TYPE,
				LightningRapierTeleportPacket::handle
		);

		Set<UUID> hasDoubleJumped = ConcurrentHashMap.newKeySet();


		ServerPlayNetworking.registerGlobalReceiver(DoubleJumpPayload.ID, (payload, ctx) -> {
			ServerPlayer player = ctx.player();
			ctx.server().execute(() -> {
				if (!player.onGround()
						&& !player.isInWater()
						&& !player.isInLava()
						&& !hasDoubleJumped.contains(player.getUUID())
						&& player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ROSEGOLD_BOOTS)) {

					Vec3 vel = player.getDeltaMovement();
					player.setDeltaMovement(vel.x, 0.55, vel.z);
					player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
					hasDoubleJumped.add(player.getUUID());

					ServerLevel level = (ServerLevel) player.level();
					level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
							player.getX(), player.getY() + 0.1, player.getZ(),
							12,
							0.3,
							0.05,
							0.3,
							0.01
					);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(PharathornDashPacket.TYPE, PharathornDashPacket::handle);

		ServerTickEvents.END_SERVER_TICK.register(server ->
				server.getPlayerList().getPlayers().forEach(p -> {
					if (p.onGround()) hasDoubleJumped.remove(p.getUUID());
				})
		);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				hasDoubleJumped.remove(handler.player.getUUID())
		);

		ServerPlayNetworking.registerGlobalReceiver(FireDeathLaserPacket.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ServerLevel level = (ServerLevel) player.level();

			context.server().execute(() -> {
				ItemStack stack = player.getMainHandItem();

				if (!(stack.getItem() instanceof PulseBlasterItem)) return;

				int ammo = PulseBlasterItem.getAmmo(stack);
				if (ammo < 3) {
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 1.0f);
					return;
				}

				PulseBlasterItem.setAmmo(stack, ammo - 0);

				double maxDist = 40.0;
				Vec3 start = player.getEyePosition().subtract(0, 0.2, 0);
				Vec3 look = player.getViewVector(1.0f);
				Vec3 end = start.add(look.scale(maxDist));

				HitResult hitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
				Vec3 actualEnd = hitResult.getLocation();
				float actualDist = (float) start.distanceTo(actualEnd);


				AABB hitBox = new AABB(start, actualEnd).inflate(1.0); // 1 block margin of error
				for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player)) {
					Optional<Vec3> hit = target.getBoundingBox().inflate(0.2).clip(start, actualEnd);
					if (hit.isPresent()) {
						target.hurt(level.damageSources().playerAttack(player), 15.0f);
					}
				}

				level.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 2.0f);
			});
		});
		EntityRenderers.register(ModEntities.DEATH_LASER, DeathLaserEntityRenderer::new);
		PayloadTypeRegistry.clientboundPlay().register(
				PulseBlasterAmmoPayload.ID,
				PulseBlasterAmmoPayload.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				TogglePickaxeMiningPacket.TYPE,
				TogglePickaxeMiningPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				TogglePickaxeMiningPacket.TYPE,
				TogglePickaxeMiningPacket::handle
		);
		PayloadTypeRegistry.clientboundPlay().register(
				ShadowguardInvisiblePacket.TYPE,
				ShadowguardInvisiblePacket.CODEC
		);
		RosegoldPickaxeUsageEvent.register();
		KillUpgradeRegistry.registerAll();

		LOGGER.info("Hello Fabric world!");
	}
}