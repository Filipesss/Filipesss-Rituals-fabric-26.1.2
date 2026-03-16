package net.filipes.rituals.client.render;

import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.filipes.rituals.client.render.state.PedestalRenderState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class RitualPedestalBlockEntityRenderer implements BlockEntityRenderer<RitualPedestalBlockEntity, PedestalRenderState> {
    private final TextRenderer font;

    public RitualPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.font = ctx.textRenderer();
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void updateRenderState(RitualPedestalBlockEntity be, PedestalRenderState state, float tickDelta, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(be, state, tickDelta, cameraPos, crumblingOverlay);

        state.lines.clear();

        // Key by registry ID string — completely unambiguous
        java.util.LinkedHashMap<String, Integer> countMap = new java.util.LinkedHashMap<>();
        java.util.LinkedHashMap<String, MutableText> nameMap = new java.util.LinkedHashMap<>();

        for (int i = 0; i < be.size(); i++) {
            ItemStack stack = be.getStack(i);
            if (!stack.isEmpty()) {
                String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                countMap.merge(id, stack.getCount(), Integer::sum);
                nameMap.putIfAbsent(id, stack.getName().copy());
            }
        }

        for (String id : countMap.keySet()) {
            int total = countMap.get(id);
            MutableText line = nameMap.get(id).copy();
            line.append(" " + total + "x");
            state.lines.add(line.asOrderedText());
        }
    }

    @Override
    public void render(PedestalRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        OrderedText text = Text.literal("Hello!").asOrderedText();
        int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        matrices.push();
        matrices.translate(0.5, 2.0, 0.5);
        matrices.multiply(new Quaternionf(cameraState.orientation));
        matrices.scale(-0.025f, -0.025f, 0.025f);

        int width = font.getWidth(text);
        float x = -width / 2f;

        queue.submitText(
                matrices,
                x, 0f,
                text,
                false,
                TextRenderer.TextLayerType.SEE_THROUGH,
                fullBright,
                0xFFFFFF,
                0,
                0
        );
        matrices.pop();
    }
}