package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class PolarityBowSpecialRenderer implements SpecialModelRenderer<PolarityBowSpecialRenderer.RenderData> {
    private static final Identifier HANDLE_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_handle_part");
    private static final Identifier TOP_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_top_part");
    private static final Identifier BOTTOM_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_bottom_part");
    private static final Identifier STRING_TOP_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_string_top");
    private static final Identifier STRING_MID_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_string_mid");
    private static final Identifier STRING_BOTTOM_MODEL_ID =
            Identifier.fromNamespaceAndPath("rituals", "polarity_bow_string_bottom");

    private static final float MODEL_UNIT = 1.0f / 16.0f;

    private static final float LIMB_PIVOT_X        = 8f;
    private static final float LIMB_TOP_PIVOT_Y    = 28f;
    private static final float LIMB_BOTTOM_PIVOT_Y = -20.0f;
    private static final float LIMB_PIVOT_Z        = 7.0f;

    private static final float STRING_TOP_PIVOT_X    = -3.5f;
    private static final float STRING_TOP_PIVOT_Y    = 9f;
    private static final float STRING_TOP_PIVOT_Z    = 7.0f;

    private static final float STRING_BOTTOM_PIVOT_X = -2.5f;
    private static final float STRING_BOTTOM_PIVOT_Y = -6.5f;
    private static final float STRING_BOTTOM_PIVOT_Z = 7.0f;

    private final ItemStackRenderState handleState       = new ItemStackRenderState();
    private final ItemStackRenderState topState          = new ItemStackRenderState();
    private final ItemStackRenderState bottomState       = new ItemStackRenderState();
    private final ItemStackRenderState stringTopState    = new ItemStackRenderState();
    private final ItemStackRenderState stringMidState    = new ItemStackRenderState();
    private final ItemStackRenderState stringBottomState = new ItemStackRenderState();

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        return new RenderData(stack.copy(), computePullProgress(stack));
    }

    @Override
    public void submit(@Nullable RenderData data, PoseStack matrices,
                       SubmitNodeCollector submitNodeCollector,
                       int light, int overlay, boolean glint, int outlineColor) {
        if (data == null) return;

        Minecraft client = Minecraft.getInstance();
        ItemModelResolver resolver = client.getItemModelResolver();
        ClientLevel level = client.level;
        ItemOwner owner = client.player;

        ItemModel handleModel       = client.getModelManager().getItemModel(HANDLE_MODEL_ID);
        ItemModel topModel          = client.getModelManager().getItemModel(TOP_MODEL_ID);
        ItemModel bottomModel       = client.getModelManager().getItemModel(BOTTOM_MODEL_ID);
        ItemModel stringTopModel    = client.getModelManager().getItemModel(STRING_TOP_MODEL_ID);
        ItemModel stringMidModel    = client.getModelManager().getItemModel(STRING_MID_MODEL_ID);
        ItemModel stringBottomModel = client.getModelManager().getItemModel(STRING_BOTTOM_MODEL_ID);

        updateRenderState(handleState,       handleModel,       data.stack(), resolver, level, owner);
        updateRenderState(topState,          topModel,          data.stack(), resolver, level, owner);
        updateRenderState(bottomState,       bottomModel,       data.stack(), resolver, level, owner);
        updateRenderState(stringTopState,    stringTopModel,    data.stack(), resolver, level, owner);
        updateRenderState(stringMidState,    stringMidModel,    data.stack(), resolver, level, owner);
        updateRenderState(stringBottomState, stringBottomModel, data.stack(), resolver, level, owner);

        float pull = data.pullProgress();
        float easedPull = 1.0f - (1.0f - pull) * (1.0f - pull);

        float limbTopRoll      =  0.18f * easedPull;
        float limbBottomRoll   = -0.18f * easedPull;
        float stringTopRoll    =  0.62f * easedPull;
        float stringBottomRoll =  0.62f * easedPull;
        float stringCenterPull =  2.8f  * easedPull;

        submitPart(handleState, matrices, submitNodeCollector, light, overlay, outlineColor);

        matrices.pushPose();
        matrices.translate(-stringCenterPull * MODEL_UNIT, 0f, 0f);
        rotateAroundPivot(matrices, LIMB_PIVOT_X, LIMB_TOP_PIVOT_Y, LIMB_PIVOT_Z, 0f, 0f, limbTopRoll);
        submitPart(topState, matrices, submitNodeCollector, light, overlay, outlineColor);
        rotateAroundPivot(matrices, STRING_TOP_PIVOT_X, STRING_TOP_PIVOT_Y, STRING_TOP_PIVOT_Z, 0f, 0f, -stringTopRoll);
        submitPart(stringTopState, matrices, submitNodeCollector, light, overlay, outlineColor);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(-stringCenterPull * MODEL_UNIT, 0f, 0f);
        rotateAroundPivot(matrices, LIMB_PIVOT_X, LIMB_BOTTOM_PIVOT_Y, LIMB_PIVOT_Z, 0f, 0f, limbBottomRoll);
        submitPart(bottomState, matrices, submitNodeCollector, light, overlay, outlineColor);
        rotateAroundPivot(matrices, STRING_BOTTOM_PIVOT_X, STRING_BOTTOM_PIVOT_Y, STRING_BOTTOM_PIVOT_Z, 0f, 0f, stringBottomRoll);
        submitPart(stringBottomState, matrices, submitNodeCollector, light, overlay, outlineColor);
        matrices.popPose();

        // String center
        matrices.pushPose();
        matrices.translate(-stringCenterPull * MODEL_UNIT, 0f, 0f);
        submitPart(stringMidState, matrices, submitNodeCollector, light, overlay, outlineColor);
        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    private static void updateRenderState(ItemStackRenderState renderState, ItemModel model, ItemStack stack,
                                          ItemModelResolver resolver, @Nullable ClientLevel level, @Nullable ItemOwner owner) {
        renderState.clear();
        model.update(renderState, stack, resolver, ItemDisplayContext.NONE, level, owner, 0);
    }

    private static void submitPart(ItemStackRenderState state, PoseStack matrices, SubmitNodeCollector collector,
                                   int light, int overlay, int outlineColor) {
        if (state.isEmpty()) return;
        state.submit(matrices, collector, light, overlay, outlineColor);
    }

    private static void rotateAroundPivot(PoseStack matrices,
                                          float pivotX, float pivotY, float pivotZ,
                                          float rotX, float rotY, float rotZ) {
        matrices.translate( pivotX * MODEL_UNIT,  pivotY * MODEL_UNIT,  pivotZ * MODEL_UNIT);
        if (rotX != 0f) matrices.mulPose(Axis.XP.rotation(rotX));
        if (rotY != 0f) matrices.mulPose(Axis.YP.rotation(rotY));
        if (rotZ != 0f) matrices.mulPose(Axis.ZP.rotation(rotZ));
        matrices.translate(-pivotX * MODEL_UNIT, -pivotY * MODEL_UNIT, -pivotZ * MODEL_UNIT);
    }

    private static float computePullProgress(ItemStack renderedStack) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !client.player.isUsingItem()) {
            return 0f;
        }

        ItemStack usingStack = client.player.getUseItem();
        if (!usingStack.is(ModItems.POLARITY_BOW)) {
            return 0f;
        }

        if (!ItemStack.isSameItemSameComponents(usingStack, renderedStack)) {
            return 0f;
        }

        float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float usedTicks   = client.player.getTicksUsingItem(partialTick);
        return calculateBowPull(usedTicks);
    }

    private static float calculateBowPull(float usedTicks) {
        float progress = usedTicks / 20.0f;
        progress = (progress * progress + progress * 2.0f) / 3.0f;
        return Math.min(progress, 1.0f);
    }

    public record RenderData(ItemStack stack, float pullProgress) {}

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<RenderData> bake(BakingContext context) {
            return new PolarityBowSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}