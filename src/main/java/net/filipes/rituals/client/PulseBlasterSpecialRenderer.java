package net.filipes.rituals.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.class_3902;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class PulseBlasterSpecialRenderer implements SpecialModelRenderer<class_3902> {

    private static final Identifier TEXTURE =
            Identifier.of("rituals", "textures/item/pulse_blaster.png");

    private static final float MODEL_SCALE = 0.9f;

    private final PulseBlasterGunModel model;

    public PulseBlasterSpecialRenderer(PulseBlasterGunModel model) {
        this.model = model;
    }

    @Override
    public @Nullable class_3902 getData(ItemStack stack) {
        return class_3902.field_17274;
    }

    @Override
    public void render(@Nullable class_3902 data, ItemDisplayContext displayContext,
                       MatrixStack matrices, OrderedRenderCommandQueue queue,
                       int light, int overlay, boolean glint, int i) {
        matrices.push();
        matrices.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f));

        float cylinderAngle = PulseBlasterCylinderState.getAngle();
        model.render(matrices, queue, light, overlay, cylinderAngle, PulseBlasterCylinderState.isGlowing());

        matrices.pop();
    }

    @Override
    public void collectVertices(Consumer<Vector3fc> consumer) {

    }



    public static final class Unbaked implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {

            return new PulseBlasterSpecialRenderer(
                    new PulseBlasterGunModel(
                            context.entityModelSet().getModelPart(PulseBlasterGunModel.LAYER)
                    )
            );
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> getCodec() {
            return CODEC;
        }
    }
}