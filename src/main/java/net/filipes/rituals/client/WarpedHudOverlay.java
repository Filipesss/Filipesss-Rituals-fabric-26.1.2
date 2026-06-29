package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WarpedHudOverlay {

    private static final long FADE_MS = 500;
    private static final Identifier VIGNETTE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/misc/basic_vignette.png");

    private static final Identifier MOVEMENT_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_slow_movement");

    private static float alpha = 0.0f;
    private static long lastTime = System.currentTimeMillis();

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("rituals", "warped_hud"),
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

        // Calculate frame delta time for smooth frame-rate independent fading
        long now = System.currentTimeMillis();
        long delta = now - lastTime;
        lastTime = now;

        // Check if the player has the slow-zone attribute modifier (synced automatically by the server)
        var speedAttr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean isWarped = speedAttr != null && speedAttr.getModifier(MOVEMENT_MODIFIER_ID) != null;

        // Smoothly adjust alpha state
        if (isWarped) {
            alpha += (float) delta / FADE_MS;
            if (alpha > 1.0f) alpha = 1.0f;
        } else {
            alpha -= (float) delta / FADE_MS;
            if (alpha < 0.0f) alpha = 0.0f;
        }

        if (alpha <= 0.0f) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Deep blue vignette tint
        int a = (int) (alpha * 85) & 0xFF;
        int color = (a << 24) | 0x0A369B;

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

        String label = "[WARPED]";
        int textWidth = mc.font.width(label);

        // Electric/bright blue text for readability against the dark vignette
        int textAlpha = (int) (alpha * 255) & 0xFF;
        int textColor = (textAlpha << 24) | 0x00A2FF;

        guiGraphics.text(
                mc.font,
                label,
                (width - textWidth) / 2,
                height - 52,
                textColor,
                true
        );
    }
}