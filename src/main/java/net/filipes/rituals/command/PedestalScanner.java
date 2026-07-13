package net.filipes.rituals.command;

import com.mojang.datafixers.util.Pair;
import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.pedestal.PedestalSavedData;
import net.filipes.rituals.pedestal.PedestalTypes;
import net.filipes.rituals.worldgen.ScatteredStructurePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashSet;
import java.util.Set;

public class PedestalScanner {

    private static final int SCAN_RADIUS = 2;

    public static int scanForPedestals(ServerLevel level) {
        PedestalSavedData data = PedestalSavedData.getOrCreate(level);
        int totalTypes = PedestalTypes.REGISTRY.size();
        if (data.getPlaced().size() >= totalTypes) return 0;

        Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> pedestalKey = ResourceKey.create(Registries.STRUCTURE,
                Identifier.fromNamespaceAndPath("rituals", "pedestal_structure"));
        Structure pedestalStructure = structures.getValueOrThrow(pedestalKey);
        Holder<Structure> pedestalHolder = structures.wrapAsHolder(pedestalStructure);

        var chunkSource = level.getChunkSource();
        if (!(chunkSource instanceof ServerChunkCache serverChunkCache)) return 0;
        var structureState = serverChunkCache.getGeneratorState();
        var placements = structureState.getPlacementsForStructure(pedestalHolder);

        int found = 0;
        for (var placement : placements) {
            if (!(placement instanceof ScatteredStructurePlacement scattered)) continue;

            Set<ChunkPos> targetChunks = scattered.getTargetChunks(level.getSeed());
            System.out.println("[Rituals] Scanning " + targetChunks.size() + " known target chunks.");

            for (ChunkPos chunkPos : targetChunks) {
                if (data.getPlaced().size() >= totalTypes) break;
                boolean claimed = claimAtChunk(level, chunkPos, data);
                if (claimed) found++;
            }
        }

        System.out.println("[Rituals] Scan finished. found=" + found + " totalPlaced=" + data.getPlaced().size());
        return found;
    }

    private static boolean claimAtChunk(ServerLevel level, ChunkPos targetChunk, PedestalSavedData data) {
        int minX = targetChunk.x() - SCAN_RADIUS;
        int maxX = targetChunk.x() + SCAN_RADIUS;
        int minZ = targetChunk.z() - SCAN_RADIUS;
        int maxZ = targetChunk.z() + SCAN_RADIUS;

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }

        try {
            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    LevelChunk chunk = level.getChunk(cx, cz);
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof RitualPedestalBlockEntity pedestal) {
                            int before = data.getPlaced().size();
                            pedestal.tryClaimPedestalType(level);
                            boolean gained = data.getPlaced().size() > before;
                            System.out.println("[Rituals] Found pedestal at " + be.getBlockPos()
                                    + " (target chunk was " + targetChunk + "), claimed=" + gained);
                            if (gained) return true;
                        }
                    }
                }
            }
        } finally {
            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    level.setChunkForced(cx, cz, false);
                }
            }
        }
        return false;
    }
}