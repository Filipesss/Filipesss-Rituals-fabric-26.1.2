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

public class PedestalSavedData extends SavedData {

    private static final String DATA_KEY = "ritual_pedestals";

    private final Map<String, BlockPos> placed = new LinkedHashMap<>();

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
            )
    ).apply(instance, entries -> {
        PedestalSavedData data = new PedestalSavedData();
        for (PedestalEntry entry : entries) {
            data.placed.put(entry.id(), entry.pos());
        }
        return data;
    }));


    public static final SavedDataType<PedestalSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("rituals", DATA_KEY), // <-- FIX: Wrap the String in an Identifier
            PedestalSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );


    public static PedestalSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
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