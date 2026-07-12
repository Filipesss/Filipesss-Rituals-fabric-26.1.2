package net.filipes.rituals.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;
import java.util.Random;

public class PedestalStructurePlacer {

    public static final Identifier PLATFORM_ID =
            Identifier.fromNamespaceAndPath("rituals", "pedestal_structure");

    private static final BlockPos PEDESTAL_LOCAL_OFFSET = new BlockPos(3, 4, 3);

    private static final Rotation[] ROTATIONS = Rotation.values();

    public static boolean placeAt(ServerLevel world, BlockPos targetPedestalPos, Random rng) {
        StructureTemplateManager structureManager = world.getServer().getStructureManager();
        Optional<StructureTemplate> templateOpt = structureManager.get(PLATFORM_ID);

        if (templateOpt.isEmpty()) {
            System.out.println("[Rituals] Could not load structure template: " + PLATFORM_ID);
            return false;
        }

        StructureTemplate template = templateOpt.get();
        Rotation rotation = ROTATIONS[rng.nextInt(ROTATIONS.length)];
        Mirror mirror = Mirror.NONE;

        BlockPos rotatedOffset = rotateAroundOrigin(PEDESTAL_LOCAL_OFFSET, rotation);

        BlockPos structureOrigin = targetPedestalPos.subtract(rotatedOffset);

        Vec3i rawSize = template.getSize();
        boolean swapped = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        int sizeX = swapped ? rawSize.getZ() : rawSize.getX();
        int sizeZ = swapped ? rawSize.getX() : rawSize.getZ();

        forceLoadFootprint(world, structureOrigin, sizeX, sizeZ);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setRotationPivot(BlockPos.ZERO)
                .setIgnoreEntities(false);

        RandomSource random = RandomSource.create(rng.nextLong());
        boolean placed = template.placeInWorld(world, structureOrigin, structureOrigin, settings, random, 2);

        if (!placed) {
            System.out.println("[Rituals] Structure placement failed at " + structureOrigin);
            return false;
        }

        return true;
    }

    private static BlockPos rotateAroundOrigin(BlockPos pos, Rotation rotation) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return switch (rotation) {
            case NONE -> new BlockPos(x, y, z);
            case CLOCKWISE_90 -> new BlockPos(-z, y, x);
            case CLOCKWISE_180 -> new BlockPos(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, -x);
        };
    }

    private static void forceLoadFootprint(ServerLevel world, BlockPos origin, int sizeX, int sizeZ) {
        int minChunkX = origin.getX() >> 4;
        int maxChunkX = (origin.getX() + sizeX - 1) >> 4;
        int minChunkZ = origin.getZ() >> 4;
        int maxChunkZ = (origin.getZ() + sizeZ - 1) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                world.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
    }
}