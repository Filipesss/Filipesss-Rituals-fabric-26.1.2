package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class LunarBladeHudOverlay {

    private static final long DURATION_MS = 5000;
    private static final long FADE_MS     = 600;

    private static final Identifier VIGNETTE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/misc/basic_vignette.png");

    private static long activeUntil = 0;
    private static long flashUntil = 0L;
    private static final long FLASH_MS = 250L;

    public static void triggerFlash() {
        flashUntil = System.currentTimeMillis() + FLASH_MS;
    }

    public static void trigger() {
        activeUntil = System.currentTimeMillis() + DURATION_MS;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("rituals", "lunar_blade_hud"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics,
                                                   DeltaTracker deltaTracker) {
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
        } else if (DURATION_MS - remaining < FADE_MS) {
            alpha = (float) (DURATION_MS - remaining) / FADE_MS;
        } else {
            alpha = 1.0f;
        }

        long flashRemaining = flashUntil - System.currentTimeMillis();
        float flashStrength = flashRemaining > 0 ? (float) flashRemaining / FLASH_MS : 0f;

        int width  = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int a = (int) (alpha * 90) & 0xFF;

        int baseColor = 0xC8D2FF;
        int flashColor = 0xFFFFFF;
        int blendedRgb = lerpRgb(baseColor, flashColor, flashStrength);

        int color = (a << 24) | blendedRgb;

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

    private static int lerpRgb(int from, int to, float t) {
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }
}