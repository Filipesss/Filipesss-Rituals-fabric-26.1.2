package net.filipes.rituals.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Renders the Pulse Blaster item as a 3D gun with a rotating cylinder.
 *
 * Registration (done in RitualsClient):
 *   SpecialModelTypes.ID_MAPPER.put(Identifier.of("rituals","pulse_blaster"), Unbaked.CODEC)
 *
 * Item model JSON (assets/rituals/items/pulse_blaster.json) references this via:
 *   "type": "rituals:pulse_blaster"
 */
public class PulseBlasterSpecialRenderer implements SpecialModelRenderer<Unit> {

    /**
     * Path to the gun's texture.
     * Update this if your texture is named differently or lives in a sub-folder.
     */
    private static final Identifier TEXTURE =
            Identifier.of("rituals", "textures/item/pulse_blaster.png");

    /**
     * Scale factor: ModelPart cuboids are in pixel units (1/16 of a block),
     * so we divide by 16 to map them to the 0-1 item coordinate space.
     */
    private static final float MODEL_SCALE = 1f / 16f;

    private final PulseBlasterGunModel model;

    public PulseBlasterSpecialRenderer(PulseBlasterGunModel model) {
        this.model = model;
    }

    // ── SpecialModelRenderer ──────────────────────────────────────────────────

    @Override
    public @Nullable Unit getData(ItemStack stack) {
        // No per-stack data needed — spin state is global (client-side singleton)
        return Unit.INSTANCE;
    }

    @Override
    public void render(
            @Nullable Unit data,
            ItemDisplayContext displayContext,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            boolean glint,
            int i    // extra param in 1.21.11 — packed ARGB tint, pass through unused
    ) {
        matrices.push();

        // Scale pixel-unit model down to item-space (1 block = 1.0 unit)
        matrices.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

        // Centre the model here if needed — tweak translate if gun appears offset
        matrices.translate(0f, 0f, 0f);

        float cylinderAngle = PulseBlasterCylinderState.getAngle();

        // submitModelPart mirrors exactly how PulseBlasterBeamRenderer renders —
        // RenderLayers (with S!) contains the factory methods in 1.21.11.
        model.render(matrices, queue, light, overlay, cylinderAngle);

        matrices.pop();
    }

    @Override
    public void collectVertices(Consumer<Vector3fc> consumer) {
        // Not needed for our item — used for e.g. shadow/collision shape calculation.
        // Leave empty; returning without calling consumer is valid.
    }

    // ── Unbaked (registered with SpecialModelTypes.ID_MAPPER) ─────────────────

    public static final class Unbaked implements SpecialModelRenderer.Unbaked {

        /**
         * Codec with no fields — our renderer is stateless (all state lives in
         * PulseBlasterCylinderState). MapCodec.unit gives us a zero-argument codec.
         */
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            // BakeContext gives us LoadedEntityModels to bake our registered layer
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