package net.filipes.rituals.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ReverseControlsHandler {

    private static int remainingTicks = 0;

    public static void trigger(int durationTicks) { remainingTicks = durationTicks; }
    public static boolean isActive()              { return remainingTicks > 0; }
    public static void tick()                     { if (remainingTicks > 0) remainingTicks--; }
}