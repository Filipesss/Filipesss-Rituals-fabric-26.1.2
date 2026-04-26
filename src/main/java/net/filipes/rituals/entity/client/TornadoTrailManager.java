package net.filipes.rituals.entity.client;

import net.minecraft.world.phys.Vec3;
import java.util.ArrayDeque;
import java.util.Deque;

public class TornadoTrailManager {

    public record Entry(Vec3 pos, boolean wasMoving) {}

    private final Deque<Entry> entries;
    private final int maxLength;

    public TornadoTrailManager(int maxLength) {
        this.maxLength = maxLength;
        this.entries   = new ArrayDeque<>(maxLength + 1);
    }

    public void push(Vec3 pos, boolean wasMoving) {
        entries.addFirst(new Entry(pos, wasMoving));
        while (entries.size() > maxLength) entries.removeLast();
    }

    public Entry[] getEntries() {
        return entries.toArray(new Entry[0]);
    }

    public int size()  { return entries.size(); }
    public void clear(){ entries.clear(); }
}