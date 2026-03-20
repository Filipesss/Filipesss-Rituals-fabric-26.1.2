package net.filipes.rituals.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PulseBlasterGunModel {

    public static final EntityModelLayer LAYER = new EntityModelLayer(
            Identifier.of("rituals", "pulse_blaster_gun"), "main");

    private static final Identifier TEXTURE =
            Identifier.of("rituals", "textures/item/pulse_blaster.png");

    private final ModelPart bb_main;
    private final ModelPart cylinder;

    public PulseBlasterGunModel(ModelPart root) {
        this.cylinder = root.getChild("cylinder");
        this.bb_main  = root.getChild("bb_main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // ── CYLINDER ──────────────────────────────────────────────────────────
        ModelPartData cylinder = modelPartData.addChild("cylinder", ModelPartBuilder.create()
                        .uv(16, 31).cuboid(-1.5F, -0.375F, -1.5F, 3.0F, 7.0F, 3.0F, new Dilation(0.0F))
                        .uv(36, 8).cuboid(-2.0F, -0.975F, -0.5F, 4.0F, 8.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 0.0F, 0.0F));  // pivot zeroed out

        cylinder.addChild("cube_r1", ModelPartBuilder.create()
                        .uv(0, 38).cuboid(-2.0F, -1.0F, -0.5F, 4.0F, 8.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        cylinder.addChild("cube_r2", ModelPartBuilder.create()
                        .uv(46, 28).cuboid(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 7.525F, 0.0F, 0.0F, 0.7854F, 0.0F));

        // ── BASE ──────────────────────────────────────────────────────────────
        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create()
                        .uv(0, 31).cuboid(-2.0F, -13.0F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(-3.0F, -12.0F, -3.0F, 6.0F, 1.0F, 6.0F, new Dilation(0.0F))
                        .uv(32, 46).cuboid(-1.0F, -11.6F, -3.5F, 1.0F, 7.0F, 1.0F, new Dilation(0.0F))
                        .uv(50, 13).cuboid(-1.0F, -11.6F, 2.5F, 1.0F, 7.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 23).cuboid(-1.0F, -12.6F, -3.5F, 1.0F, 1.0F, 7.0F, new Dilation(0.0F))
                        .uv(28, 46).cuboid(-3.4F, -12.3F, -0.5F, 1.0F, 9.0F, 1.0F, new Dilation(0.0F))
                        .uv(36, 17).cuboid(-3.0F, -8.0F, 2.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(36, 19).cuboid(-3.0F, -8.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(38, 40).cuboid(2.0F, -8.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(36, 21).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(10, 41).cuboid(2.0F, -2.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(28, 38).cuboid(-3.0F, -2.0F, 2.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(42, 30).cuboid(-3.0F, -2.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(20, 41).cuboid(2.5F, -11.6F, 0.5F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F))
                        .uv(24, 41).cuboid(2.5F, -11.6F, -1.5F, 1.0F, 10.0F, 1.0F, new Dilation(0.0F))
                        .uv(46, 50).cuboid(3.0F, -8.0F, -1.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F))
                        .uv(46, 45).cuboid(2.8F, 2.0F, -2.0F, 0.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(0, 7).cuboid(-3.0F, -4.0F, -3.0F, 6.0F, 1.0F, 6.0F, new Dilation(0.0F))
                        .uv(42, 35).cuboid(-3.0F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(40, 0).cuboid(-3.0F, 5.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(40, 2).cuboid(-3.0F, 5.0F, 2.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(46, 8).cuboid(2.0F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(10, 38).cuboid(1.9F, 5.5F, -0.45F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                        .uv(12, 51).cuboid(-2.9F, 5.5F, -0.45F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                        .uv(46, 13).cuboid(-3.0F, -11.0F, -3.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
                        .uv(48, 40).cuboid(-3.0F, -11.0F, 2.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 47).cuboid(2.8F, 0.0F, -2.0F, 0.0F, 1.0F, 4.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 0.0F, 0.0F));  // pivot zeroed out

        bb_main.addChild("cube_r3", ModelPartBuilder.create()
                        .uv(28, 40).cuboid(2.0F, -1.0F, -2.0F, 0.0F, 1.0F, 5.0F, new Dilation(0.0F)),
                ModelTransform.of(0.3F, 2.0F, 0.5F, 0.0F, -1.5708F, 0.0F));

        bb_main.addChild("cube_r4", ModelPartBuilder.create()
                        .uv(46, 23).cuboid(2.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(40, 6).cuboid(-3.0F, -1.0F, 2.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(10, 46).cuboid(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new Dilation(0.0F))
                        .uv(40, 4).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(0.0F, 8.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addChild("cube_r5", ModelPartBuilder.create()
                        .uv(24, 8).cuboid(2.0F, -1.0F, -2.0F, 0.0F, 1.0F, 5.0F, new Dilation(0.0F)),
                ModelTransform.of(0.3F, 2.0F, -4.5F, 0.0F, -1.5708F, 0.0F));

        bb_main.addChild("cube_r6", ModelPartBuilder.create()
                        .uv(18, 14).cuboid(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.of(-2.5F, 1.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addChild("cube_r7", ModelPartBuilder.create()
                        .uv(0, 14).cuboid(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.of(-2.5F, 1.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addChild("cube_r8", ModelPartBuilder.create()
                        .uv(24, 0).cuboid(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 7.0F, new Dilation(0.0F)),
                ModelTransform.of(2.5F, 1.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addChild("cube_r9", ModelPartBuilder.create()
                        .uv(16, 23).cuboid(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 7.0F, new Dilation(0.0F)),
                ModelTransform.of(2.5F, 1.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addChild("cube_r10", ModelPartBuilder.create()
                        .uv(8, 51).cuboid(-1.0F, -6.0F, -1.0F, 1.0F, 6.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(4.5F, -3.0F, 0.7F, 0.0F, -0.7854F, 0.0F));

        bb_main.addChild("cube_r11", ModelPartBuilder.create()
                        .uv(38, 45).cuboid(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(4.5F, -3.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addChild("cube_r12", ModelPartBuilder.create()
                        .uv(32, 23).cuboid(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(2.5F, -6.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addChild("cube_r13", ModelPartBuilder.create()
                        .uv(28, 31).cuboid(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 6.0F, new Dilation(0.0F)),
                ModelTransform.of(2.5F, -6.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue,
                       int light, int overlay, float cylinderAngle) {
        cylinder.yaw = cylinderAngle;

        queue.submitModelPart(bb_main, matrices,
                RenderLayers.entityCutoutNoCull(TEXTURE), light, overlay, null);
        queue.submitModelPart(cylinder, matrices,
                RenderLayers.entityCutoutNoCull(TEXTURE), light, overlay, null);
    }
}