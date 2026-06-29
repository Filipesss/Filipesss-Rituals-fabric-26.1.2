package net.filipes.rituals.client.cooldown;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CooldownHudOverlay {

    private static final long FADE_OUT_MS   = 800;
    private static final int  BAR_WIDTH     = 100;
    private static final int  BAR_HEIGHT    = 5;
    private static final int  ENTRY_HEIGHT  = 18;
    private static final int  BOTTOM_OFFSET = 60;
    private static final int  LEFT_OFFSET   = 8;

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("rituals", "cooldown_hud"),
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

        List<Map.Entry<String, CooldownManager.AbilityDefinition>> toRender = new ArrayList<>();
        for (Map.Entry<String, CooldownManager.AbilityDefinition> entry : CooldownManager.getDefinitions().entrySet()) {
            if (CooldownManager.isOnCooldown(entry.getKey())) {
                toRender.add(entry);
            }
        }
        if (toRender.isEmpty()) return;

        int screenHeight = mc.getWindow().getGuiScaledHeight();

        for (int i = 0; i < toRender.size(); i++) {
            String id  = toRender.get(i).getKey();
            CooldownManager.AbilityDefinition def = toRender.get(i).getValue();

            float progress    = CooldownManager.getProgress(id);
            long  remainingMs = CooldownManager.getRemainingMs(id);

            float alpha = remainingMs < FADE_OUT_MS ? (float) remainingMs / FADE_OUT_MS : 1.0f;
            int   a     = (int)(alpha * 255) & 0xFF;

            int baseY = screenHeight - BOTTOM_OFFSET - (i * (ENTRY_HEIGHT + 6));
            int barY  = baseY + 11;

            long secondsLeft = (remainingMs / 1000) + 1;
            String leftLabel  = def.displayName();
            String rightLabel = secondsLeft + "s";

            guiGraphics.text(mc.font, leftLabel,  LEFT_OFFSET, baseY, (a << 24) | 0xFFFFFF, true);
            guiGraphics.text(mc.font, rightLabel,
                    LEFT_OFFSET + mc.font.width(leftLabel) + 4,
                    baseY, (a << 24) | 0xAAAAAA, true);

            guiGraphics.fill(LEFT_OFFSET - 1,             barY - 1,
                    LEFT_OFFSET + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1,
                    (a << 24) | 0x000000);
            guiGraphics.fill(LEFT_OFFSET,             barY,
                    LEFT_OFFSET + BAR_WIDTH, barY + BAR_HEIGHT,
                    (a << 24) | 0x222222);

            int filledWidth = (int)(progress * BAR_WIDTH);
            if (filledWidth > 0) {
                int rgb = def.barColor();
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b =  rgb        & 0xFF;
                guiGraphics.fill(LEFT_OFFSET,
                        barY,
                        LEFT_OFFSET + filledWidth,
                        barY + BAR_HEIGHT,
                        (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }
}