package net.filipes.rituals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.client.PulseBlasterCylinderState;
import net.filipes.rituals.client.PulseBlasterGunModel;
import net.filipes.rituals.client.PulseBlasterHudOverlay;
import net.filipes.rituals.client.PulseBlasterSpecialRenderer;
import net.filipes.rituals.client.render.RitualPedestalBlockEntityRenderer;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.PulseBlasterBeamModel;
import net.filipes.rituals.entity.client.PulseBlasterBeamRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.filipes.rituals.util.TooltipStyleHolder;

public class RitualsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Rituals custom tooltip API — sets active style before border is drawn
        // In RitualsClient.onInitializeClient() — replace the previous line:
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            TooltipStyleHolder.clear();   // wipe previous item's style first
            TooltipStyleHolder.set(stack); // apply this item's style
        });
        // ── Block entity renderers ─────────────────────────────────────────────
        BlockEntityRendererFactories.register(
                ModBlockEntities.RITUAL_PEDESTAL_BE,
                RitualPedestalBlockEntityRenderer::new
        );

        // ── Beam projectile entity ─────────────────────────────────────────────
        EntityRendererRegistry.register(
                ModEntities.PULSE_BLASTER_BEAM,
                PulseBlasterBeamRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                PulseBlasterBeamModel.LAYER,
                PulseBlasterBeamModel::getTexturedModelData
        );

        // ── Pulse Blaster gun item — special renderer with rotating cylinder ───

        // 1. Register the model layer so the baking pipeline knows about the gun geometry
        EntityModelLayerRegistry.registerModelLayer(
                PulseBlasterGunModel.LAYER,
                PulseBlasterGunModel::getTexturedModelData
        );

        // 2. Register the special model type via ID_MAPPER (the correct API in 1.21.11)
        //    Referenced from items/pulse_blaster.json as "type": "rituals:pulse_blaster"
        SpecialModelTypes.ID_MAPPER.put(
                Identifier.of("rituals", "pulse_blaster"),
                PulseBlasterSpecialRenderer.Unbaked.CODEC
        );

        // ── HUD overlay ────────────────────────────────────────────────────────
        PulseBlasterHudOverlay.register();

        // ── Cylinder spin tick ─────────────────────────────────────────────────
        // Advances the spin physics every client tick (friction-based decay)
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> PulseBlasterCylinderState.tick()
        );
    }
}