package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class CinderboltSaveHudOverlay {

    private static final long DURATION_MS = 5000L; // matches IMMUNITY_DURATION_MS
    private static final long FADE_MS = 600L;

    private static final Identifier VIGNETTE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/misc/basic_vignette.png");

    private static long activeUntil = 0;

    public static void trigger() {
        activeUntil = System.currentTimeMillis() + DURATION_MS;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("rituals", "cinderbolt_save_hud"),
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

        float envelope;
        if (remaining < FADE_MS) {
            envelope = (float) remaining / FADE_MS;
        } else if (DURATION_MS - remaining < FADE_MS) {
            envelope = (float) (DURATION_MS - remaining) / FADE_MS;
        } else {
            envelope = 1.0f;
        }

        long time = System.currentTimeMillis();
        float pulse = (float) (0.6 + 0.4 * Math.abs(Math.sin(time / 250.0)));

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int a = (int) (envelope * pulse * 100) & 0xFF;
        int color = (a << 24) | 0xFF6600;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VIGNETTE_TEXTURE,
                0, 0,
                0.0f, 0.0f,
                width, height,
                256, 256,
                256, 256,
                color
        );
    }
}