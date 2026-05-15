package net.filipes.rituals.entity.custom;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PharathornMarkTracker {

    private static final Set<UUID> MARKS = new HashSet<>();

    public static boolean isMarked(UUID uuid)  { return MARKS.contains(uuid); }
    public static void    mark(UUID uuid)       { MARKS.add(uuid); }
    public static void    unmark(UUID uuid)     { MARKS.remove(uuid); }
}