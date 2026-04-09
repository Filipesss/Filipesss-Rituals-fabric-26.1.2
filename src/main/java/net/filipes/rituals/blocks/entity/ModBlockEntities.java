package net.filipes.rituals.blocks.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<RitualPedestalBlockEntity> RITUAL_PEDESTAL_BE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "ritual_pedestal"),
                    FabricBlockEntityTypeBuilder.create(
                            RitualPedestalBlockEntity::new,
                            ModBlocks.RITUAL_PEDESTAL
                    ).build()
            );


    public static void registerModBlockEntities() {
        Rituals.LOGGER.info("Registering Block Entities for " + Rituals.MOD_ID);
    }
}