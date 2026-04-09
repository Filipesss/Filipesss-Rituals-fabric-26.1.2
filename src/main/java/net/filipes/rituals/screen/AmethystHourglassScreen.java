package net.filipes.rituals.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.upgrade.IngredientRequirement;
import net.filipes.rituals.upgrade.UpgradeRecipe;
import net.filipes.rituals.upgrade.UpgradeRecipeRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class AmethystHourglassScreen extends AbstractContainerScreen<AmethystHourglassScreenHandler> {

    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/gui/container/amethyst_hourglass.png");

    private static final int GUI_WIDTH  = 176;
    private static final int GUI_HEIGHT = 222;

    private static final Identifier BTN_ON  = Identifier.fromNamespaceAndPath("rituals", "upg_button_on");
    private static final Identifier BTN_OFF = Identifier.fromNamespaceAndPath("rituals", "upg_button_off");

    private static final int BTN_X = 136;
    private static final int BTN_Y = 83;
    private static final int BTN_W = 22;
    private static final int BTN_H = 13;

    private static final int BAR_X1 = 24;
    private static final int BAR_Y1 = 103;
    private static final int BAR_X2 = 151;
    private static final int BAR_Y2 = 118;

    private static final int PREVIEW_X = 70;
    private static final int PREVIEW_Y = 43;
    private static final int PREVIEW_W = 35;
    private static final int PREVIEW_H = 35;

    private static final int COLOR_ENOUGH     = 0xFFFFFFFF;
    private static final int COLOR_NOT_ENOUGH = 0xFFFF4444;

    public AmethystHourglassScreen(AmethystHourglassScreenHandler handler,
                                   Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        ItemStack inputStack = this.menu.getInventory().getItem(0);
        Optional<UpgradeRecipe> recipeOpt = UpgradeRecipeRegistry.getRecipe(inputStack);
        if (recipeOpt.isPresent()) {
            UpgradeRecipe recipe = recipeOpt.get();
            renderIngredientsPanel(graphics, recipe);
            renderUpgradePreview(graphics, recipe);
            renderUpgradeButton(graphics, recipe, mouseX, mouseY);
        }
    }
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xCC9427f5, false);
    }
    private void renderUpgradePreview(GuiGraphicsExtractor graphics, UpgradeRecipe recipe) {
        ItemStack input = this.menu.getInventory().getItem(0);
        if (input.isEmpty()) return;

        ItemStack preview = ModDataComponents.withStage(input, recipe.getResultStage());

        float scale = 2.5f;
        int scaledSize = (int)(16 * scale); // 40px

        int ix = leftPos + PREVIEW_X + (PREVIEW_W - scaledSize) / 2;
        int iy = topPos  + PREVIEW_Y + (PREVIEW_H - scaledSize) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(ix, iy);
        graphics.pose().scale(scale, scale);
        graphics.item(preview, 0, 0);
        graphics.pose().popMatrix();
    }

    private void renderIngredientsPanel(GuiGraphicsExtractor graphics, UpgradeRecipe recipe) {
        List<IngredientRequirement> ingredients = recipe.getIngredients();

        int barWidth = BAR_X2 - BAR_X1;
        int step = 18;
        int totalW = ingredients.size() * step - 2;
        int startX = leftPos + BAR_X1 + (barWidth - totalW) / 2;
        int startY = topPos  + BAR_Y1;

        for (int i = 0; i < ingredients.size(); i++) {
            IngredientRequirement req = ingredients.get(i);
            boolean satisfied = req.isSatisfied(this.minecraft.player);

            int ix = startX + i * step;
            int iy = startY;

            graphics.item(new ItemStack(req.item()), ix, iy);

            String countText = String.valueOf(req.count());
            int textX = ix + 16 - this.font.width(countText);
            int textY = iy + 8;
            graphics.text(this.font, countText, textX, textY,
                    satisfied ? COLOR_ENOUGH : COLOR_NOT_ENOUGH, false);
        }
    }

    private void renderUpgradeButton(GuiGraphicsExtractor graphics, UpgradeRecipe recipe,
                                     int mouseX, int mouseY) {
        int bx = leftPos + BTN_X;
        int by = topPos  + BTN_Y;

        boolean canUpgrade = recipe.canCraft(this.minecraft.player);
        boolean hovered    = mouseX >= bx && mouseX < bx + BTN_W
                && mouseY >= by && mouseY < by + BTN_H;

        Identifier texture = canUpgrade ? BTN_ON : BTN_OFF;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, bx, by, BTN_W, BTN_H);

        if (canUpgrade && hovered) {
            graphics.fill(bx, by, bx + BTN_W, by + BTN_H, 0x44FFFFFF);
        }

        int currentStage = ModDataComponents.getStage(this.menu.getInventory().getItem(0));
        graphics.text(this.font, currentStage + " \u2192 " + recipe.getResultStage(),
                bx - 3, by + BTN_H - 67, 0xFF777777, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int bx = leftPos + BTN_X;
        int by = topPos  + BTN_Y;
        if (event.button() == 0
                && event.x() >= bx && event.x() < bx + BTN_W
                && event.y() >= by && event.y() < by + BTN_H) {
            ItemStack input = this.menu.getInventory().getItem(0);
            Optional<UpgradeRecipe> recipe = UpgradeRecipeRegistry.getRecipe(input);
            if (recipe.isPresent() && recipe.get().canCraft(this.minecraft.player)) {

                this.minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)
                );
                this.minecraft.gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        AmethystHourglassScreenHandler.BUTTON_UPGRADE);
                this.minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f)
                );
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}