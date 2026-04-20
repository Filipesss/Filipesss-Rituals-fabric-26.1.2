package net.filipes.rituals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.blocks.ModBlocks;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.command.RitualCommands;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.DeathLaserEntityRenderer;
import net.filipes.rituals.entity.custom.DeathLaserEntity;
import net.filipes.rituals.event.PlayerKillListener;
import net.filipes.rituals.item.ModItemGroups;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.network.FireDeathLaserPacket;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.filipes.rituals.network.ShadowguardInvisiblePacket;
import net.filipes.rituals.network.TogglePickaxeMiningPacket;
import net.filipes.rituals.screen.ModMenuTypes;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.upgrade.KillUpgradeRegistry;
import net.filipes.rituals.upgrade.UpgradeRecipeRegistry;
import net.filipes.rituals.util.RosegoldPickaxeUsageEvent;
import net.filipes.rituals.worldgen.RitualWorldGen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
		ModDataComponents.register();
		UpgradeRecipeRegistry.registerAll();
		ModItems.registerModItems();
		ModStatusEffects.registerModStatusEffects();
		ModBlockEntities.registerModBlockEntities();
		ModItemGroups.registerItemGroups();
		ModEntities.registerModEntities();
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

// 2. Register the receiver
		ServerPlayNetworking.registerGlobalReceiver(FireDeathLaserPacket.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ServerLevel level = (ServerLevel) player.level();

			context.server().execute(() -> {
				ItemStack stack = player.getMainHandItem();

				// Ensure they are actually holding the blaster
				if (!(stack.getItem() instanceof PulseBlasterItem)) return;

				// Optional: Check Ammo. Let's say the Death Laser requires at least 3 ammo.
				int ammo = PulseBlasterItem.getAmmo(stack);
				if (ammo < 3) {
					level.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 1.0f);
					return;
				}

				// Consume ammo (Cost: 3)
				PulseBlasterItem.setAmmo(stack, ammo - 0);
				// Note: You might want to call your syncAmmo method here to update the client HUD

				// --- Calculate Raycast ---
				double maxDist = 40.0;
				// Start slightly lower than eye level so it lines up with the gun
				Vec3 start = player.getEyePosition().subtract(0, 0.2, 0);
				Vec3 look = player.getViewVector(1.0f);
				Vec3 end = start.add(look.scale(maxDist));

				// Raycast against blocks to find the hit point
				HitResult hitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
				Vec3 actualEnd = hitResult.getLocation();
				float actualDist = (float) start.distanceTo(actualEnd);

				// --- Spawn Visual Laser ---
				// Color is ARGB hex. Let's make it an angry red/orange: 0xFFFF2222

				// --- Damage Entities ---
				AABB hitBox = new AABB(start, actualEnd).inflate(1.0); // 1 block margin of error
				for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player)) {
					// Precise intersection check for the beam path
					Optional<Vec3> hit = target.getBoundingBox().inflate(0.2).clip(start, actualEnd);
					if (hit.isPresent()) {
						target.hurt(level.damageSources().playerAttack(player), 15.0f); // 15 Damage
						// Optional: set them on fire
						// target.igniteForSeconds(5);
					}
				}

				// Play firing sound (replace with your custom ModSounds if desired)
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