package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;
import net.filipes.rituals.network.PulseBlasterHeatPayload;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PulseBlasterHudOverlay {

    private static final int SEG_W   = 7;
    private static final int SEG_H   = 10;
    private static final int SEG_GAP = 2;

    private static int  liveAmmo = -1;
    private static long clientOverchargeExpiry = -1L;

    private static float clientHeatAtSync   = 0f;
    private static long  clientHeatSyncTime = 0L;
    private static boolean clientOverheated = false;
    private static long clientOverheatUntil = 0L;

    public static void setLiveAmmo(int ammo) { liveAmmo = ammo; }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                PulseBlasterAmmoPayload.ID,
                (payload, context) -> {
                    int newAmmo = payload.ammo();
                    if (newAmmo >= 0 && liveAmmo > newAmmo) {
                        PulseBlasterCylinderState.onShot();
                    }
                    liveAmmo = newAmmo;
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                PulseBlasterHeatPayload.ID,
                (payload, context) -> {
                    clientHeatAtSync = payload.heatFraction();
                    clientHeatSyncTime = System.currentTimeMillis();
                    clientOverheated = payload.overheated();
                    if (clientOverheated) {
                        clientOverheatUntil = System.currentTimeMillis() + PulseBlasterItem.OVERHEAT_LOCKOUT_MS;
                    }
                }
        );

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_hud"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                        renderHud(graphics);
                    }
                }
        );
    }

    private static float getPredictedHeatFraction(int stage) {
        float maxHeat = PulseBlasterItem.getMaxHeat(stage);
        long elapsedMs = System.currentTimeMillis() - clientHeatSyncTime;
        float decayed = clientHeatAtSync - (elapsedMs / 1000f) * (PulseBlasterItem.HEAT_DECAY_PER_SEC / maxHeat);
        return Math.max(0f, Math.min(1f, decayed));
    }

    private static void renderHud(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) return;

        ItemStack main = client.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off  = client.player.getItemInHand(InteractionHand.OFF_HAND);

        ItemStack blaster = null;
        if (main.getItem() instanceof PulseBlasterItem) blaster = main;
        else if (off.getItem() instanceof PulseBlasterItem) blaster = off;
        if (blaster == null) { liveAmmo = -1; return; }

        int stage   = ModDataComponents.getStage(blaster);
        int maxAmmo = PulseBlasterItem.getMaxAmmo(stage);
        int ammo    = (liveAmmo >= 0) ? liveAmmo : PulseBlasterItem.getAmmo(blaster);

        int barW = maxAmmo * SEG_W + (maxAmmo - 1) * SEG_GAP;

        int screenW    = client.getWindow().getGuiScaledWidth();
        int screenH    = client.getWindow().getGuiScaledHeight();
        int hotbarLeft = (screenW - 182) / 2;
        int x          = hotbarLeft - barW - 14;
        int y          = screenH - 22;

        boolean low     = ammo <= 2 && ammo > 0;
        boolean empty   = ammo == 0;
        long    time    = System.currentTimeMillis();
        boolean flashOn = !low || (time / 350) % 2 == 0;
        boolean displayOverheated = System.currentTimeMillis() < clientOverheatUntil;

        for (int i = 0; i < maxAmmo; i++) {
            int     sx     = x + i * (SEG_W + SEG_GAP);
            boolean filled = i < ammo;

            if (filled) {
                float ratio = (float) ammo / maxAmmo;
                int fill;
                if (displayOverheated) {
                    float pulse = (float) Math.abs(Math.sin(System.currentTimeMillis() / 200.0));
                    fill = lerpColor(0xFFCC2200, 0xFFFF9900, pulse);
                } else if (isOvercharged()) {
                    float pulse = (float) ((System.currentTimeMillis() % 400) / 400.0);
                    fill = flashOn ? lerpColor(0xFFFF6600, 0xFFFFDD00,
                            (float) Math.abs(Math.sin(pulse * Math.PI))) : 0xFF888888;
                } else {
                    fill = flashOn ? blendColor(ratio) : 0xFF888888;
                }
                graphics.fill(sx - 1, y - 1, sx + SEG_W + 1, y + SEG_H + 1, darken(fill, 0.4f));
                graphics.fill(sx,     y,     sx + SEG_W,     y + SEG_H,     fill);
                graphics.fill(sx,     y,     sx + SEG_W,     y + 2,         brighten(fill, 1.6f));
                graphics.fill(sx,     y + SEG_H - 2, sx + SEG_W, y + SEG_H, darken(fill, 0.6f));
            } else {
                graphics.fill(sx - 1, y - 1, sx + SEG_W + 1, y + SEG_H + 1, 0xFF1A1A1A);
                graphics.fill(sx,     y,     sx + SEG_W,     y + SEG_H,     0xFF2A2A2A);
                graphics.fill(sx,     y,     sx + SEG_W,     y + 1,         0xFF111111);
            }
        }

        int heatBarY = y + SEG_H + 4;
        float heatFraction = getPredictedHeatFraction(stage);

        graphics.fill(x - 1, heatBarY - 1, x + barW + 1, heatBarY + 4, 0xFF1A1A1A);

        int filledWidth = (int) (barW * heatFraction);
        int heatColor = displayOverheated
                ? ((System.currentTimeMillis() / 200) % 2 == 0 ? 0xFFFF3333 : 0xFF881111)
                : lerpColor(0xFF4477FF, 0xFFFF4400, heatFraction);

        if (filledWidth > 0) {
            graphics.fill(x, heatBarY, x + filledWidth, heatBarY + 3, heatColor);
        }

        String label;
        int    labelColor;
        if (displayOverheated) {
            label = "OVERHEATED";
            labelColor = 0xFFFF2200;
        } else if (isOvercharged()) {
            long remaining = (clientOverchargeExpiry - time) / 1000 + 1;
            label = "OVERCHARGE  " + remaining + "s";
            labelColor = (time / 250) % 2 == 0 ? 0xFFFFCC00 : 0xFFFF6600;
        } else if (empty) {
            label = (time / 500) % 2 == 0 ? "RELOAD" : "";
            labelColor = 0xFFAA3333;
        } else if (low) {
            label = "LOW  " + ammo + "/" + maxAmmo;
            labelColor = flashOn ? 0xFFFF4444 : 0xFF883333;
        } else {
            label = "PWR  " + ammo + "/" + maxAmmo;
            labelColor = 0xFFAAAAAA;
        }

        int labelX = x + barW / 2 - client.font.width(label) / 2;
        graphics.text(client.font, label, labelX, y - 10, labelColor, true);
    }

    public static void triggerOvercharge() {
        clientOverchargeExpiry = System.currentTimeMillis() + PulseBlasterItem.OVERCHARGE_DURATION_MS;
    }

    public static boolean isOvercharged() {
        return System.currentTimeMillis() < clientOverchargeExpiry;
    }

    private static int blendColor(float ratio) {
        int r, g, b;
        if (ratio >= 0.5f) {
            float t = (ratio - 0.5f) * 2f;
            r = lerp(0xFF, 0x00, t); g = lerp(0xAA, 0xBB, t); b = lerp(0x00, 0xFF, t);
        } else {
            float t = ratio * 2f;
            r = lerp(0xFF, 0xFF, t); g = lerp(0x22, 0xAA, t); b = lerp(0x00, 0x00, t);
        }
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | (lerp(ar, br, t) << 16) | (lerp(ag, bg, t) << 8) | lerp(ab, bb, t);
    }

    private static int darken(int color, float f) {
        int r = (int) (((color >> 16) & 0xFF) * f);
        int g = (int) (((color >>  8) & 0xFF) * f);
        int b = (int) (( color        & 0xFF) * f);
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int brighten(int color, float f) {
        int r = (int) (((color >> 16) & 0xFF) * f);
        int g = (int) (((color >>  8) & 0xFF) * f);
        int b = (int) (( color        & 0xFF) * f);
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int lerp(int a, int b, float t) { return (int) (a + (b - a) * t); }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}