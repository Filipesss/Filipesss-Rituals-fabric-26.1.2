package net.filipes.rituals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.client.PulseBlasterHudOverlay;

import net.filipes.rituals.client.render.RitualPedestalBlockEntityRenderer;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.PulseBlasterBeamModel;
import net.filipes.rituals.entity.client.PulseBlasterBeamRenderer;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;


public class RitualsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        BlockEntityRendererFactories.register(ModBlockEntities.RITUAL_PEDESTAL_BE, RitualPedestalBlockEntityRenderer::new);

            // Register the entity renderer
        EntityRendererRegistry.register(
                ModEntities.PULSE_BLASTER_BEAM,
                PulseBlasterBeamRenderer::new);

            // Register the model layer
        EntityModelLayerRegistry.registerModelLayer(
                PulseBlasterBeamModel.LAYER,
                PulseBlasterBeamModel::getTexturedModelData);

        PulseBlasterHudOverlay.register();




    }
}