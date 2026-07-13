package net.filipes.rituals.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScatteredStructurePlacement extends StructurePlacement {

    public static final MapCodec<ScatteredStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            (RecordCodecBuilder.Instance<ScatteredStructurePlacement> instance) ->
                    StructurePlacement.placementCodec(instance).and(instance.group(
                            Codec.intRange(1, 4095).fieldOf("count").forGetter(p -> p.count),
                            Codec.intRange(0, 1_000_000).fieldOf("radius_blocks").forGetter(p -> p.radiusBlocks)
                    )).apply(instance, ScatteredStructurePlacement::new)
    );

    public static final StructurePlacementType<ScatteredStructurePlacement> TYPE = () -> CODEC;
    private static final java.util.concurrent.atomic.AtomicInteger CALL_COUNT = new java.util.concurrent.atomic.AtomicInteger();


    private static final ConcurrentHashMap<Long, Set<ChunkPos>> CACHE = new ConcurrentHashMap<>();

    private final int count;
    private final int radiusBlocks;
    public Set<ChunkPos> getTargetChunks(long levelSeed) {
        return getOrComputePositions(levelSeed);
    }

    public ScatteredStructurePlacement(Vec3i locateOffset,
                                       StructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
                                       float frequency,
                                       int salt,
                                       Optional<StructurePlacement.ExclusionZone> exclusionZone,
                                       int count,
                                       int radiusBlocks) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.count = count;
        this.radiusBlocks = radiusBlocks;
    }

    private Set<ChunkPos> getOrComputePositions(long levelSeed) {
        long key = levelSeed ^ ((long) salt() * 0x9E3779B97F4A7C15L);
        return CACHE.computeIfAbsent(key, seed -> {
            Set<ChunkPos> positions = new HashSet<>();
            RandomSource rng = RandomSource.create(seed);
            int attempts = 0;
            while (positions.size() < count && attempts < count * 50) {
                attempts++;
                double angle = rng.nextDouble() * Math.PI * 2;
                double dist = Math.sqrt(rng.nextDouble()) * radiusBlocks;
                int blockX = (int) (Math.cos(angle) * dist);
                int blockZ = (int) (Math.sin(angle) * dist);
                positions.add(new ChunkPos(blockX >> 4, blockZ >> 4));
            }

            return positions;
        });
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        CALL_COUNT.incrementAndGet();
        Set<ChunkPos> positions = getOrComputePositions(state.getLevelSeed());
        return positions.contains(new ChunkPos(sourceX, sourceZ));
    }

    @Override
    public StructurePlacementType<?> type() {
        return TYPE;
    }
}