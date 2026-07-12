package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class VortexDarknessOverlay {

    private static final long FADE_MS = 600;

    private static long activeUntil = 0;
    private static long totalDurationMs = 0;

    public static void trigger(int durationTicks) {
        totalDurationMs = durationTicks * 50L;
        activeUntil = System.currentTimeMillis() + totalDurationMs;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("rituals", "vortex_darkness"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                        render(graphics);
                    }
                }
        );
    }

    private static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) return;

        long remaining = activeUntil - System.currentTimeMillis();
        if (remaining <= 0) return;

        float alpha;
        if (remaining < FADE_MS) {
            alpha = (float) remaining / FADE_MS;
        } else if (totalDurationMs - remaining < FADE_MS) {
            alpha = (float) (totalDurationMs - remaining) / FADE_MS;
        } else {
            alpha = 1.0f;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Near-total blackout — tune the 235 cap if you want it slightly less absolute
        int a = (int) (alpha * 235) & 0xFF;
        int color = (a << 24); // solid black, alpha-scaled

        guiGraphics.fill(0, 0, width, height, color);
    }
}