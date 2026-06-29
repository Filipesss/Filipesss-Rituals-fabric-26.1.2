package net.filipes.rituals.client.render;

import com.mojang.blaze3d.PrimitiveTopology; // Imported for draw modes
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ModRenderTypes {

    private static final RenderPipeline ADDITIVE_TEXTURE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("rituals", "additive_texture"))
                    .withVertexShader("core/rendertype_eyes")
                    .withFragmentShader("core/rendertype_eyes")
                    .withColorTargetState(new ColorTargetState(
                            new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO)
                    ))
                    // FIXES: Binds the format to buffer index 0 and sets the topology type explicitly
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .build()
    );

    private static final Map<Identifier, RenderType> CACHE = new HashMap<>();

    public static RenderType additiveTexture(Identifier texture) {
        return CACHE.computeIfAbsent(texture, t ->
                RenderType.create(
                        "rituals_additive_texture",
                        RenderSetup.builder(ADDITIVE_TEXTURE_PIPELINE)
                                .withTexture("Sampler0", t) // Texture binding remains inside Setup
                                .setOutputTarget(OutputTarget.WEATHER_TARGET)
                                .createRenderSetup()
                )
        );
    }
}