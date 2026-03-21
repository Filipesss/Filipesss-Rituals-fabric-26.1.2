package net.filipes.rituals;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.filipes.rituals.blocks.ModBlocks;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.item.ModItemGroups;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RosegoldPickaxeUsageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rituals implements ModInitializer {
	public static final String MOD_ID = "rituals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModBlockEntities.registerModBlockEntities(); // ADD THIS
		ModItemGroups.registerItemGroups();
		ModEntities.registerModEntities();
		ModSounds.initialize();
		// Register the payload type for S2C
		PayloadTypeRegistry.playS2C().register(
				PulseBlasterAmmoPayload.ID,
				PulseBlasterAmmoPayload.CODEC
		);

		PlayerBlockBreakEvents.BEFORE.register(new RosegoldPickaxeUsageEvent());

		LOGGER.info("Hello Fabric world!");
	}
}