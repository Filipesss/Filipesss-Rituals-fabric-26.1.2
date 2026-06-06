package net.filipes.rituals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
import net.filipes.rituals.entity.custom.*;
import net.filipes.rituals.event.PlayerKillListener;
import net.filipes.rituals.item.ModItemGroups;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.*;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Rituals implements ModInitializer {
	public static final String MOD_ID = "rituals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Set<UUID> BONUS_GUARD = new HashSet<>();
	private static int vortexFrame = 0;
	private static int frameTicker = 0;

	private static Vec3 randomSphere(double speed) {
		double theta = Math.random() * Math.PI * 2.0;
		double phi   = Math.acos(2.0 * Math.random() - 1.0);
		return new Vec3(
				Math.sin(phi) * Math.cos(theta) * speed,
				Math.cos(phi) * speed,
				Math.sin(phi) * Math.sin(theta) * speed
		);
	}

	private static Vec3 randomSpread(double spread) {
		return new Vec3(
				(Math.random() - 0.5) * 2.0 * spread,
				(Math.random() - 0.5) * 2.0 * spread,
				(Math.random() - 0.5) * 2.0 * spread
		);
	}

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
		RosegoldPickaxeUsageEvent.register();
		KillUpgradeRegistry.registerAll();
		PharathornStillHandler.register();
		CinderboltShieldHandler.register();
		CinderboltDeathSaveHandler.register();
		TwinBladesHandler.register();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 != 0) return;

			for (ServerLevel level : server.getAllLevels()) {
				List<ServerPlayer> pharathornHolders = level.players().stream()
						.filter(p -> p.getMainHandItem().getItem() instanceof PharathornItem)
						.toList();

				if (pharathornHolders.isEmpty()) continue;

				for (ServerPlayer holder : pharathornHolders) {
					AABB searchBox = holder.getBoundingBox().inflate(32.0);
					List<LivingEntity> candidates = level.getEntitiesOfClass(
							LivingEntity.class, searchBox,
							e -> e != holder
									&& e.isAlive()
									&& e.getHealth() <= e.getMaxHealth() * 0.5f
									&& !PharathornMarkTracker.isMarked(e.getUUID()));

					for (LivingEntity target : candidates) {
						PharathornMarkTracker.mark(target.getUUID());

						double spawnX = target.getX();
						double spawnY = target.getY() + target.getBbHeight() * 0.5;
						double spawnZ = target.getZ();

						PharathornMarkEntity mark = new PharathornMarkEntity(
								ModEntities.PHARATHORN_MARK, level, spawnX, spawnY, spawnZ);
						mark.setTargetUUID(target.getUUID());
						mark.setEntityScale(1.0f);
						level.addFreshEntity(mark);

						Vec3 toHolder = holder.position()
								.add(0, holder.getBbHeight() * 0.5, 0)
								.subtract(spawnX, spawnY, spawnZ)
								.normalize();

						for (int i = 0; i < 8; i++) {
							Vec3 vel = i < 3
									? toHolder.add(randomSpread(0.35)).normalize().scale(0.5 + Math.random() * 0.4)
									: randomSphere(0.4 + Math.random() * 0.5);
							SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
							spark.applyPreset(SparkPresets.PHARATHORN_MARK_BIG);
							spark.forcedVelocity = vel;
							spark.setDeltaMovement(vel);
							level.addFreshEntity(spark);
						}

						for (int i = 0; i < 6; i++) {
							Vec3 vel = i < 3
									? toHolder.add(randomSpread(0.25)).normalize().scale(0.35 + Math.random() * 0.3)
									: randomSphere(0.25 + Math.random() * 0.35);
							SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, spawnX, spawnY, spawnZ);
							spark.applyPreset(SparkPresets.PHARATHORN_MARK);
							spark.forcedVelocity = vel;
							spark.setDeltaMovement(vel);
							level.addFreshEntity(spark);
						}

						level.playSound(null, spawnX, spawnY, spawnZ,
								SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
								SoundSource.PLAYERS,
								0.7f, 1.4f);
					}
				}
			}
			ShadowguardItem.tickInvisibility();
			LifestealMarkTracker.tick();
			LunarMarkTracker.tick();
			SolarMarkTracker.tick();
			SolarBladeChargeTracker.tickServer(server);
			LunarBladeOnHitTracker.tickServer(server);
		});
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide()) return net.minecraft.world.InteractionResult.PASS;
			if (!(player instanceof ServerPlayer sp)) return net.minecraft.world.InteractionResult.PASS;
			if (!(entity instanceof LivingEntity target)) return net.minecraft.world.InteractionResult.PASS;
			if (!(sp.getMainHandItem().getItem() instanceof LunarBladeItem))
				return net.minecraft.world.InteractionResult.PASS;
			if (LunarBladeOnHitTracker.isActive(sp.getUUID())) {
				LunarBladeOnHitTracker.onHit(sp, target);
			}
			if (sp.getMainHandItem().getItem() instanceof SolarBladeItem
					&& SolarBladeChargeTracker.isActive(sp.getUUID())) {
				SolarBladeChargeTracker.onHit(sp, target);
			}
			return net.minecraft.world.InteractionResult.PASS;
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, killed) -> {
			if (!(source.getEntity() instanceof ServerPlayer attacker)) return;
			if (damageTaken <= 0) return;

			ItemStack mainHand = attacker.getMainHandItem();

			if (mainHand.getItem() instanceof PharathornItem
					&& PharathornMarkTracker.isMarked(entity.getUUID())
					&& entity.level() instanceof ServerLevel serverLevel) {

				if (!BONUS_GUARD.contains(entity.getUUID())) {
					BONUS_GUARD.add(entity.getUUID());
					entity.hurtServer(serverLevel, source, 2.0f);
					BONUS_GUARD.remove(entity.getUUID());
				}

				double cx = entity.getX();
				double cy = entity.getY() + entity.getBbHeight() * 0.5;
				double cz = entity.getZ();

				for (int i = 0; i < 8; i++) {
					Vec3 offset = randomSphere(0.8 + Math.random() * 0.7);
					Vec3 vel    = offset.normalize().scale(-(0.35 + Math.random() * 0.3));
					SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel,
							cx + offset.x, cy + offset.y, cz + offset.z);
					spark.applyPreset(SparkPresets.PHARATHORN_MARK_BIG);
					spark.forcedVelocity = vel;
					spark.setDeltaMovement(vel);
					serverLevel.addFreshEntity(spark);
				}

				for (int i = 0; i < 6; i++) {
					Vec3 offset = randomSphere(0.6 + Math.random() * 0.5);
					Vec3 vel    = offset.normalize().scale(-(0.2 + Math.random() * 0.25));
					SparkEntity spark = new SparkEntity(ModEntities.SPARK, serverLevel,
							cx + offset.x, cy + offset.y, cz + offset.z);
					spark.applyPreset(SparkPresets.PHARATHORN_MARK);
					spark.forcedVelocity = vel;
					spark.setDeltaMovement(vel);
					serverLevel.addFreshEntity(spark);
				}
			}

			if (mainHand.getItem() instanceof ShadowguardItem) {
				int stage = ModDataComponents.getStage(mainHand);
				if (stage >= 2 && attacker.level().getRandom().nextFloat() < 0.10f) {
					attacker.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
					ShadowguardItem.markInvisible(attacker.getUUID());
					ServerPlayNetworking.send(attacker, new ShadowguardInvisiblePacket());
				}
			}

			if (entity instanceof LivingEntity damagedEntity
					&& LifestealMarkTracker.isMarkedBy(damagedEntity.getUUID(), attacker.getUUID())
					&& attacker.level() instanceof ServerLevel level) {

				float healAmount = damageTaken * 0.5f;
				attacker.heal(healAmount);

				double targetX = damagedEntity.getX();
				double targetY = damagedEntity.getY() + damagedEntity.getBbHeight() * 0.5;
				double targetZ = damagedEntity.getZ();
				double playerX = attacker.getX();
				double playerY = attacker.getY() + attacker.getBbHeight() * 0.5;
				double playerZ = attacker.getZ();

				for (int i = 0; i < 5; i++) {
					SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, playerX, playerY, playerZ);
					spark.applyPreset(SparkPresets.LIFESTEAL_BIG);
					spark.forcedVelocity = randomSphere(0.3 + Math.random() * 0.35);
					level.addFreshEntity(spark);
				}
				for (int i = 0; i < 4; i++) {
					SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, playerX, playerY, playerZ);
					spark.applyPreset(SparkPresets.LIFESTEAL_SHADOWGUARD);
					spark.forcedVelocity = randomSphere(0.2 + Math.random() * 0.25);
					level.addFreshEntity(spark);
				}

				Vec3 toPlayer = new Vec3(playerX - targetX, playerY - targetY, playerZ - targetZ);
				Vec3 dir      = toPlayer.normalize();
				int streamCount = Math.max(3, (int)(toPlayer.length() * 1.5));

				for (int i = 0; i < streamCount; i++) {
					double t  = (double) i / streamCount;
					double sx = targetX + toPlayer.x * t + (Math.random() - 0.5) * 0.3;
					double sy = targetY + toPlayer.y * t + (Math.random() - 0.5) * 0.3;
					double sz = targetZ + toPlayer.z * t + (Math.random() - 0.5) * 0.3;

					SparkEntity spark = new SparkEntity(ModEntities.SPARK, level, sx, sy, sz);
					spark.applyPreset(SparkPresets.LIFESTEAL_SHADOWGUARD);
					double speed = 0.4 + Math.random() * 0.3;
					spark.forcedVelocity = new Vec3(
							dir.x * speed + (Math.random() - 0.5) * 0.1,
							dir.y * speed + (Math.random() - 0.5) * 0.1,
							dir.z * speed + (Math.random() - 0.5) * 0.1);
					level.addFreshEntity(spark);
				}
			}
		});

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
		PayloadTypeRegistry.serverboundPlay().register(
				LunarMarkPacket.TYPE,
				LunarMarkPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				LunarMarkPacket.TYPE,
				LunarMarkPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				SolarMarkPacket.TYPE,
				SolarMarkPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				SolarMarkPacket.TYPE,
				SolarMarkPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				DepthstrikeAbilityPacket.TYPE,
				DepthstrikeAbilityPacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				DepthstrikeChargedBallPacket.TYPE,
				DepthstrikeChargedBallPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				DepthstrikeChargedBallPacket.TYPE,
				DepthstrikeChargedBallPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				SolarBladeChargePacket.TYPE,
				SolarBladeChargePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				SolarBladeChargePacket.TYPE,
				SolarBladeChargePacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				LunarBladeOnHitPacket.TYPE,
				LunarBladeOnHitPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				LunarBladeOnHitPacket.TYPE,
				LunarBladeOnHitPacket::handle
		);
		PayloadTypeRegistry.clientboundPlay().register(
				CinderboltSaveTriggeredPacket.TYPE,
				CinderboltSaveTriggeredPacket.CODEC
		);
		PayloadTypeRegistry.clientboundPlay().register(
				LunarBladeActivePacket.TYPE,
				LunarBladeActivePacket.CODEC
		);
		PayloadTypeRegistry.clientboundPlay().register(
				SolarBladeActivePacket.TYPE,
				SolarBladeActivePacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				CinderboltTriplePacket.TYPE,
				CinderboltTriplePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				CinderboltTriplePacket.TYPE,
				CinderboltTriplePacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				FireCinderboltBeamPacket.TYPE,
				FireCinderboltBeamPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				FireCinderboltBeamPacket.TYPE,
				FireCinderboltBeamPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				VortexSwapPacket.TYPE,
				VortexSwapPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				VortexSwapPacket.TYPE,
				VortexSwapPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				VortexShockwavePacket.TYPE,
				VortexShockwavePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				VortexShockwavePacket.TYPE,
				VortexShockwavePacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				VortexSlamPacket.TYPE,
				VortexSlamPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				VortexSlamPacket.TYPE,
				VortexSlamPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				BlightWebPacket.TYPE,
				BlightWebPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				BlightWebPacket.TYPE,
				BlightWebPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				BlightDrainPacket.TYPE,
				BlightDrainPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				BlightDrainPacket.TYPE,
				BlightDrainPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				BlightDashPacket.TYPE,
				BlightDashPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				BlightDashPacket.TYPE,
				BlightDashPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				BlightTetherPacket.TYPE,
				BlightTetherPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				BlightTetherPacket.TYPE,
				BlightTetherPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				PharathornGroundSmashPacket.TYPE,
				PharathornGroundSmashPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				PharathornGroundSmashPacket.TYPE,
				PharathornGroundSmashPacket::handle);
		PayloadTypeRegistry.serverboundPlay().register(
				SpiralStabPacket.TYPE,
				SpiralStabPacket.CODEC)
		;
		ServerPlayNetworking.registerGlobalReceiver(
				SpiralStabPacket.TYPE,
				SpiralStabPacket::handle);
		PayloadTypeRegistry.serverboundPlay().register(
				ShadowguardLaunchPacket.TYPE,
				ShadowguardLaunchPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ShadowguardLaunchPacket.TYPE,
				ShadowguardLaunchPacket::handle
		);

		PayloadTypeRegistry.serverboundPlay().register(
				ShadowguardGrapplePacket.TYPE,
				ShadowguardGrapplePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				ShadowguardGrapplePacket.TYPE,
				ShadowguardGrapplePacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				TwinsActionTwoPacket.TYPE,
				TwinsActionTwoPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				TwinsActionTwoPacket.TYPE,
				TwinsActionTwoPacket::handle
		);
		PayloadTypeRegistry.clientboundPlay().register(
				TwinsStartCooldownPacket.TYPE,
				TwinsStartCooldownPacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				TwinsResonancePacket.TYPE,
				TwinsResonancePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				TwinsResonancePacket.TYPE,
				TwinsResonancePacket::handle
		);

		PayloadTypeRegistry.serverboundPlay().register(
				PharathornFortifyPacket.TYPE,
				PharathornFortifyPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				PharathornFortifyPacket.TYPE,
				PharathornFortifyPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				PulseBlasterOverchargePacket.TYPE,
				PulseBlasterOverchargePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				PulseBlasterOverchargePacket.TYPE,
				PulseBlasterOverchargePacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				FireDeathLaserPacket.TYPE,
				FireDeathLaserPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				FireDeathLaserPacket.TYPE,
				FireDeathLaserPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				PulseBlasterShotgunPacket.TYPE,
				PulseBlasterShotgunPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				PulseBlasterShotgunPacket.TYPE,
				PulseBlasterShotgunPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				PharathornRevealPacket.TYPE,
				PharathornRevealPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				PharathornRevealPacket.TYPE,
				PharathornRevealPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				LifestealMarkPacket.TYPE,
				LifestealMarkPacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				LifestealMarkPacket.TYPE,
				LifestealMarkPacket::handle
		);
		ServerPlayNetworking.registerGlobalReceiver(
				DepthstrikeAbilityPacket.TYPE,
				DepthstrikeAbilityPacket::handle
		);
		PayloadTypeRegistry.serverboundPlay().register(
				DepthstrikeGroundAbilityPacket.TYPE,
				DepthstrikeGroundAbilityPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				DepthstrikeGroundAbilityPacket.TYPE,
				DepthstrikeGroundAbilityPacket::handle);
		PayloadTypeRegistry.serverboundPlay().register(
				DepthstrikeRecallPacket.TYPE,
				DepthstrikeRecallPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(
				DepthstrikeRecallPacket.TYPE,
				DepthstrikeRecallPacket::handle);
		PayloadTypeRegistry.clientboundPlay().register(
				PulseBlasterAmmoPayload.ID,
				PulseBlasterAmmoPayload.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				TogglePickaxeMiningPacket.TYPE,
				TogglePickaxeMiningPacket.CODEC
		);
		PayloadTypeRegistry.serverboundPlay().register(
				LightningRapierInstantChargePacket.TYPE,
				LightningRapierInstantChargePacket.CODEC
		);
		ServerPlayNetworking.registerGlobalReceiver(
				TogglePickaxeMiningPacket.TYPE,
				TogglePickaxeMiningPacket::handle
		);
		PayloadTypeRegistry.clientboundPlay().register(
				ShadowguardInvisiblePacket.TYPE,
				ShadowguardInvisiblePacket.CODEC
		);

		Set<UUID> hasDoubleJumped = ConcurrentHashMap.newKeySet();

		ServerPlayNetworking.registerGlobalReceiver(
				LightningRapierInstantChargePacket.TYPE,
				(packet, context) -> {
					context.server().execute(() -> {
						ServerPlayer player = context.player();
						ItemStack held = player.getMainHandItem();

						if (held.getItem() instanceof LightningRapierItem
								&& ModDataComponents.getStage(held) >= 4) {

							LightningRapierItem.setCharge(held, 6);

							player.level().playSound(null,
									player.getX(), player.getY(), player.getZ(),
									ModSounds.LIGHTNING_RAPIER_ATTACK2,
									SoundSource.PLAYERS, 1.0f, 0.4f);
						}
					});
				}
		);

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
							12, 0.3, 0.05, 0.3, 0.01);
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(PharathornDashPacket.TYPE, PharathornDashPacket::handle);

		ServerTickEvents.END_SERVER_TICK.register(server ->
				server.getPlayerList().getPlayers().forEach(p -> {
					if (p.onGround()) hasDoubleJumped.remove(p.getUUID());
					LightningRapierStreakTracker.tick(server);
					ServerTickEvents.END_SERVER_TICK.register(VortexSlamPacket::tickServerSlams);
					long currentTime = server.overworld().getGameTime();

					BlightDrainPacket.ACTIVE_DRAINS.entrySet().removeIf(entry -> {
						if (currentTime >= entry.getValue()) {
							var player = server.getPlayerList().getPlayer(entry.getKey());
							if (player != null) {
								var attribute = player.getAttribute(Attributes.MAX_HEALTH);
								if (attribute != null) {
									// Pass the identifier instead of the UUID
									attribute.removeModifier(BlightDrainPacket.MODIFIER_ID);
								}
							}
							return true;
						}
						return false;
					});
					BlightTetherPacket.ACTIVE_TETHERS.removeIf(tether -> {
						// 1. Terminate when the 5 seconds expire
						if (currentTime >= tether.expiryTick) {
							return true;
						}

						ServerLevel level = server.getLevel(tether.dimension);
						if (level != null) {
							Entity entity = level.getEntity(tether.targetUuid);
							if (entity instanceof LivingEntity living && living.isAlive()) {
								Vec3 currentPos = living.position();
								Vec3 root = tether.rootPos;

								double distance = currentPos.distanceTo(root);

								// 2. If they attempt to breach the 3-block boundary radius
								if (distance > BlightTetherPacket.TETHER_RADIUS) {
									Vec3 directionFromRoot = currentPos.subtract(root).normalize();
									// Calculate exact edge coordinate vector along their escape vector
									Vec3 boundarySnapPos = root.add(directionFromRoot.scale(BlightTetherPacket.TETHER_RADIUS));

									// Hard snap them back to the edge line
									living.setPos(boundarySnapPos.x, currentPos.y, boundarySnapPos.z);

									// Apply a strong inward rubber-band velocity pulling them back down
									Vec3 elasticPull = root.subtract(currentPos).normalize().scale(0.35);
									living.setDeltaMovement(elasticPull.x, living.getDeltaMovement().y, elasticPull.z);

									if (living instanceof ServerPlayer player) {
										player.hurtMarked = true;
									}

									// Play a snapping chain audio alert when they stress the line
									if (currentTime % 4 == 0) {
										level.playSound(null, boundarySnapPos.x, boundarySnapPos.y, boundarySnapPos.z,
												net.minecraft.sounds.SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 0.8f, 1.2f);
									}
								}

								// 3. Ambient visual particles outlining the tether link status
								if (currentTime % 3 == 0) {
									// Particle marker on the floor center anchor spot
									level.sendParticles(ParticleTypes.CHERRY_LEAVES, root.x, root.y + 0.1, root.z, 4, 0.05, 0.0, 0.05, 0.0);
									// Swirling particle field tracking around the target feet
									level.sendParticles(ParticleTypes.WITCH, living.getX(), living.getY() + 0.3, living.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
								}
							}
						}
						return false;
					});
				})
		);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			frameTicker++;
			if (frameTicker % 4 != 0) return;
			vortexFrame = (vortexFrame + 1) % 9;

			ItemStack held = client.player.getMainHandItem();
			if (held.is(ModItems.VORTEX_EDGE)) {
				held.set(
						DataComponents.CUSTOM_MODEL_DATA,
						new CustomModelData(List.of((float) vortexFrame), List.of(), List.of(), List.of())
				);
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				hasDoubleJumped.remove(handler.player.getUUID())
		);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				TwinsActionTwoPacket.onPlayerDisconnect(handler.getPlayer().getUUID())
		);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				TwinsResonancePacket.onPlayerDisconnect(handler.getPlayer().getUUID())
		);


		EntityRenderers.register(ModEntities.DEATH_LASER, DeathLaserEntityRenderer::new);

		LOGGER.info("Hello Fabric world!");
	}
}