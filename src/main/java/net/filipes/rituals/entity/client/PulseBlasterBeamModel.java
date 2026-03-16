package net.filipes.rituals.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class PulseBlasterBeamModel {

    public static final EntityModelLayer LAYER = new EntityModelLayer(
            Identifier.of("rituals", "pulse_blaster_beam"), "main");

    private final ModelPart bone;

    public PulseBlasterBeamModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData bone = root.addChild("bone",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-8.001F, -7.0F, 7.0F, 0.001F, 7.0F, 2.0F, Dilation.NONE)
                        .uv(4, 16).cuboid(-8.05F, -7.025F, 7.05F, 0.1F, 7.05F, 1.9F, Dilation.NONE)
                        .uv(0, 14).cuboid(-8.125F, -7.05F, 7.15F, 0.25F, 7.1F, 1.7F, Dilation.NONE)
                        .uv(3, 10).mirrored().cuboid(-8.025F, -7.5F, 7.5F, 0.05F, 8.0F, 1.0F, Dilation.NONE).mirrored(false)
                        .uv(4, 12).cuboid(-8.075F, -7.5751F, 7.55F, 0.15F, 8.1501F, 0.9F, Dilation.NONE),
                ModelTransform.origin(8.0F, 24.0F, -8.0F));

        bone.addChild("cube_r1",
                ModelPartBuilder.create()
                        .uv(12, 8).cuboid(-1.075F, -7.575F, 0.55F, 0.15F, 8.15F, 0.9F, Dilation.NONE)
                        .uv(8, 14).cuboid(-1.025F, -7.5F, 0.5F, 0.05F, 8.0F, 1.0F, Dilation.NONE)
                        .uv(10, 4).cuboid(-1.125F, -7.05F, 0.175F, 0.25F, 7.1F, 1.65F, Dilation.NONE)
                        .uv(8, 0).cuboid(-1.05F, -7.025F, 0.075F, 0.1F, 7.05F, 1.85F, Dilation.NONE)
                        .uv(4, 0).cuboid(-1.0F, -7.0F, 0.0F, 0.0F, 7.0F, 2.0F, Dilation.NONE),
                ModelTransform.of(-7.0F, 0.0F, 9.0F, 0.0F, -1.5708F, 0.0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    public ModelPart getBone() {
        return bone;
    }
}