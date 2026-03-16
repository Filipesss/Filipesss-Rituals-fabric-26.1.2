package net.filipes.rituals.blocks.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<RitualPedestalBlockEntity> RITUAL_PEDESTAL_BE =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(Rituals.MOD_ID, "ritual_pedestal"),
                    FabricBlockEntityTypeBuilder.create(
                            RitualPedestalBlockEntity::new,
                            ModBlocks.RITUAL_PEDESTAL
                    ).build()
            );

    public static void registerModBlockEntities() {
        Rituals.LOGGER.info("Registering Block Entities for " + Rituals.MOD_ID);     } }