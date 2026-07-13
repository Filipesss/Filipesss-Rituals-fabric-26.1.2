package net.filipes.rituals.pedestal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PedestalSavedData extends SavedData {

    private static final String DATA_KEY = "ritual_pedestals";

    private final Map<String, BlockPos> placed = new LinkedHashMap<>();
    private final Map<String, BlockPos> targets = new LinkedHashMap<>();

    private record PedestalEntry(String id, BlockPos pos) {}

    private static final Codec<PedestalEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(PedestalEntry::id),
            Codec.INT.fieldOf("x").forGetter(e -> e.pos().getX()),
            Codec.INT.fieldOf("y").forGetter(e -> e.pos().getY()),
            Codec.INT.fieldOf("z").forGetter(e -> e.pos().getZ())
    ).apply(instance, (id, x, y, z) -> new PedestalEntry(id, new BlockPos(x, y, z))));

    public static final Codec<PedestalSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ENTRY_CODEC.listOf().fieldOf("placed").forGetter(data ->
                    data.placed.entrySet().stream()
                            .map(e -> new PedestalEntry(e.getKey(), e.getValue()))
                            .toList()
            ),
            ENTRY_CODEC.listOf().optionalFieldOf("targets", List.of()).forGetter(data ->
                    data.targets.entrySet().stream()
                            .map(e -> new PedestalEntry(e.getKey(), e.getValue()))
                            .toList()
            )
    ).apply(instance, (placedEntries, targetEntries) -> {
        PedestalSavedData data = new PedestalSavedData();
        for (PedestalEntry entry : placedEntries) {
            data.placed.put(entry.id(), entry.pos());
        }
        for (PedestalEntry entry : targetEntries) {
            data.targets.put(entry.id(), entry.pos());
        }
        return data;
    }));


    public static final SavedDataType<PedestalSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("rituals", DATA_KEY),
            PedestalSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );


    public static PedestalSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public synchronized String claimNextUnplacedType() {
        List<String> unclaimed = new ArrayList<>();
        for (PedestalType type : PedestalTypes.REGISTRY.values()) {
            if (!placed.containsKey(type.id())) {
                unclaimed.add(type.id());
            }
        }
        if (unclaimed.isEmpty()) return null;

        int index = ThreadLocalRandom.current().nextInt(unclaimed.size());
        return unclaimed.get(index);
    }

    public Map<String, BlockPos> getTargets() {
        return Collections.unmodifiableMap(targets);
    }
    public synchronized void ensureTargetsGenerated(long levelSeed, int radiusBlocks) {
        if (!targets.isEmpty()) return;

        Random rng = new Random(levelSeed ^ 0xBADC0FFEEL);
        for (PedestalType type : PedestalTypes.REGISTRY.values()) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(rng.nextDouble()) * radiusBlocks;
            int x = (int) (Math.cos(angle) * dist);
            int z = (int) (Math.sin(angle) * dist);
            targets.put(type.id(), new BlockPos(x, 0, z));
        }
        setDirty();
    }

    public synchronized @Nullable String claimNearestUnplacedType(BlockPos fromPos) {
        String best = null;
        long bestDistSq = Long.MAX_VALUE;

        for (Map.Entry<String, BlockPos> entry : targets.entrySet()) {
            String typeId = entry.getKey();
            if (placed.containsKey(typeId)) continue;

            BlockPos target = entry.getValue();
            long dx = target.getX() - fromPos.getX();
            long dz = target.getZ() - fromPos.getZ();
            long distSq = dx * dx + dz * dz;

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = typeId;
            }
        }
        return best;
    }

    public boolean isTypeHandled(String typeId) {
        return placed.containsKey(typeId);
    }

    public void recordPlaced(String typeId, BlockPos pos) {
        placed.put(typeId, pos);
        setDirty();
    }

    public Map<String, BlockPos> getPlaced() {
        return Collections.unmodifiableMap(placed);
    }

    public @Nullable BlockPos getPos(String typeId) {
        return placed.get(typeId);
    }
}