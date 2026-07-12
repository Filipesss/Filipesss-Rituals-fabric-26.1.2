package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class RosegoldPickaxeHudOverlay {

    private static final long DISPLAY_DURATION_MS = 3000;
    private static final long FADE_DURATION_MS = 1000;

    private static long lastShownTime = 0;
    private static boolean wasHoldingPickaxe = false;
    private static boolean lastEnabledState = true;

    public static void notifyToggled() {
        lastShownTime = System.currentTimeMillis();
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("rituals", "rosegold_pickaxe_hud"),
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

        ItemStack stack = mc.player.getMainHandItem();
        boolean holdingPickaxe = stack.getItem() instanceof RosegoldPickaxeItem;

        if (holdingPickaxe && !wasHoldingPickaxe) {
            lastShownTime = System.currentTimeMillis();
        }
        wasHoldingPickaxe = holdingPickaxe;

        if (!holdingPickaxe) return;

        int stage = RosegoldPickaxeItem.getStage(stack);
        if (stage < 4) return;

        boolean currentEnabled = stack.getOrDefault(ModDataComponents.MINING_ENABLED, true);
        if (currentEnabled != lastEnabledState) {
            lastShownTime = System.currentTimeMillis();
        }
        lastEnabledState = currentEnabled;

        long elapsed = System.currentTimeMillis() - lastShownTime;
        if (elapsed > DISPLAY_DURATION_MS) return;

        long timeLeft = DISPLAY_DURATION_MS - elapsed;
        float alpha = timeLeft < FADE_DURATION_MS ? (float) timeLeft / FADE_DURATION_MS : 1.0f;
        int a = (int) (alpha * 255) & 0xFF;

        String modeName = stage >= 5 ? "3x3x3" : "3x3";

        Component text = Component.literal(modeName + " Mining: ")
                .withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(0xFFFFFF))
                        .withItalic(false))
                .append(Component.literal(currentEnabled ? "Enabled" : "Disabled")
                        .withStyle(Style.EMPTY
                                .withColor(TextColor.fromRgb(currentEnabled ? 0x55FF55 : 0xFF5555))
                                .withItalic(false)));

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int textWidth = mc.font.width(text);

        guiGraphics.text(
                mc.font,
                text,
                (screenWidth - textWidth) / 2,
                screenHeight - 72,
                (a << 24) | 0xFFFFFF,
                true
        );
    }
}