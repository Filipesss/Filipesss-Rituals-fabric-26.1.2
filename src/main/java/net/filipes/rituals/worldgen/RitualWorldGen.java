package net.filipes.rituals.worldgen;

import net.filipes.rituals.blocks.ModBlocks;
import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.pedestal.PedestalSavedData;
import net.filipes.rituals.pedestal.PedestalType;
import net.filipes.rituals.pedestal.PedestalTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class RitualWorldGen {

    /**
     * Called once when the server starts. For any pedestal type not yet placed,
     * picks a random position and places the block, forcing chunk generation.
     * Only runs when new types are missing from the saved data.
     */
    public static void placeAllPedestals(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        PedestalSavedData data = PedestalSavedData.getOrCreate(overworld);

        // Seed from world seed so position is consistent if called twice before save
        Random rng = new Random(overworld.getSeed() ^ 0xDEADBEEF_CAFEL);

        for (PedestalType type : PedestalTypes.REGISTRY.values()) {
            if (data.isTypeHandled(type.id())) continue;

            BlockPos pos = findPlacementPos(overworld, rng);
            placePedestal(overworld, pos, type);
            data.recordPlaced(type.id(), pos);

            System.out.println("[Rituals] Placed '" + type.id() +
                    "' pedestal at " + pos.toShortString());
        }
    }

    private static BlockPos findPlacementPos(ServerLevel world, Random rng) {
        int radius = RitualConfig.PEDESTAL_SPAWN_RADIUS;
        // Pick a random point within the square radius, avoiding spawn (64 block minimum)
        int x, z;
        do {
            x = rng.nextInt(radius * 2) - radius;
            z = rng.nextInt(radius * 2) - radius;
        } while (Math.abs(x) < 64 && Math.abs(z) < 64);

        // Force-generate the chunk so we can read the heightmap
        world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return new BlockPos(x, y, z);
    }

    private static void placePedestal(ServerLevel world, BlockPos pos, PedestalType type) {
        world.setBlock(pos, ModBlocks.RITUAL_PEDESTAL.defaultBlockState(), 3);
        if (world.getBlockEntity(pos) instanceof RitualPedestalBlockEntity be) {
            be.setPedestalType(type.id());
        }
    }
}