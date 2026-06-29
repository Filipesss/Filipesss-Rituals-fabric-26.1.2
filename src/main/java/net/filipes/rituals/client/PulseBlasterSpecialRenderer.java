package net.filipes.rituals.client;

import com.mojang.serialization.MapCodec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import java.util.function.Consumer;

public class PulseBlasterSpecialRenderer implements SpecialModelRenderer<Unit> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/item/pulse_blaster.png");

    private static final float MODEL_SCALE = 0.9f;

    private final PulseBlasterGunModel model;

    public PulseBlasterSpecialRenderer(PulseBlasterGunModel model) {
        this.model = model;
    }

    @Override
    public @Nullable Unit extractArgument(ItemStack stack) {
        return Unit.INSTANCE;
    }

    @Override
    public void submit(@Nullable Unit data, PoseStack matrices,
                       SubmitNodeCollector submitNodeCollector,
                       int light, int overlay, boolean glint, int outlineColor) {
        matrices.pushPose();
        matrices.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        matrices.mulPose(Axis.XP.rotationDegrees(-90f));

        model.render(matrices, submitNodeCollector, light, overlay,
                PulseBlasterCylinderState.getAngle(), PulseBlasterCylinderState.isGlowing());

        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    public static final class Unbaked implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<?> bake(BakingContext context) {
            return new PulseBlasterSpecialRenderer(
                    new PulseBlasterGunModel(
                            context.entityModelSet().bakeLayer(PulseBlasterGunModel.LAYER)
                    )
            );
        }
    }
}