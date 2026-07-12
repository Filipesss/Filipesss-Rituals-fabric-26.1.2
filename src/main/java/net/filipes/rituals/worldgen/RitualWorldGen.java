package net.filipes.rituals.worldgen;

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

    public static void placeAllPedestals(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        PedestalSavedData data = PedestalSavedData.getOrCreate(overworld);
        Random rng = new Random(overworld.getSeed() ^ 0xDEADBEEF_CAFEL);

        for (PedestalType type : PedestalTypes.REGISTRY.values()) {
            if (data.isTypeHandled(type.id())) continue;
            BlockPos pos = findPlacementPos(overworld, rng, data);

            boolean placed = PedestalStructurePlacer.placeAt(overworld, pos, rng);
            if (!placed) {
                System.out.println("[Rituals] Skipping '" + type.id() + "' — structure placement failed.");
                continue;
            }

            if (overworld.getBlockEntity(pos) instanceof RitualPedestalBlockEntity be) {
                be.setPedestalType(type.id());
            }

            data.recordPlaced(type.id(), pos);

        }
    }

    private static BlockPos findPlacementPos(ServerLevel world, Random rng, PedestalSavedData data) {
        int radius = RitualConfig.PEDESTAL_SPAWN_RADIUS;
        int minSeparation = 64;

        int x, z;
        BlockPos candidate;
        boolean tooClose;
        do {
            x = rng.nextInt(radius * 2) - radius;
            z = rng.nextInt(radius * 2) - radius;
            candidate = new BlockPos(x, 0, z);

            tooClose = Math.abs(x) < 64 && Math.abs(z) < 64;
            if (!tooClose) {
                for (BlockPos existing : data.getPlaced().values()) {
                    if (candidate.distSqr(new BlockPos(existing.getX(), 0, existing.getZ())) < (double) minSeparation * minSeparation) {
                        tooClose = true;
                        break;
                    }
                }
            }
        } while (tooClose);

        world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return new BlockPos(x, y, z);
    }
}