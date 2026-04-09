package net.filipes.rituals.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.blocks.entity.RitualPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RitualPedestalBlockEntityRenderer
        implements BlockEntityRenderer<RitualPedestalBlockEntity, RitualPedestalBlockEntityRenderer.PedestalRenderState> {

    private final Font font;

    public RitualPedestalBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.font();
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public final List<FormattedCharSequence> lines = new ArrayList<>();
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(RitualPedestalBlockEntity blockEntity,
                                   PedestalRenderState state,
                                   float partialTicks,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        LinkedHashMap<String, Integer>          countMap = new LinkedHashMap<>();
        LinkedHashMap<String, MutableComponent> nameMap  = new LinkedHashMap<>();

        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            ItemStack stack = blockEntity.getItem(i);
            if (!stack.isEmpty()) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                countMap.merge(id, stack.getCount(), Integer::sum);
                nameMap.putIfAbsent(id, stack.getHoverName().copy());
            }
        }

        state.lines.clear();
        for (String id : countMap.keySet()) {
            int total = countMap.get(id);
            MutableComponent line = nameMap.get(id).copy().append(" " + total + "x");
            state.lines.add(line.getVisualOrderText());
        }
    }

    @Override
    public void submit(PedestalRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (state.lines.isEmpty()) return;

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(0.5, 2.0, 0.5);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(-0.025f, -0.025f, 0.025f);

        float y = 0f;
        for (FormattedCharSequence line : state.lines) {
            float x = -font.width(line) / 2f;
            font.drawInBatch(
                    line, x, y,
                    0xFFFFFF,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    0xF000F0
            );
            y += font.lineHeight + 1;
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }
}