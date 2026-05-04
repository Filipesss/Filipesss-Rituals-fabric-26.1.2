package net.filipes.rituals;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.client.*;
import net.filipes.rituals.client.cooldown.CooldownHudOverlay;
import net.filipes.rituals.client.cooldown.CooldownManager;
import net.filipes.rituals.client.render.RitualPedestalBlockEntityRenderer;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.*;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.LightningRapierItem;
import net.filipes.rituals.item.custom.PharathornItem;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.filipes.rituals.network.*;
import net.filipes.rituals.particle.*;
import net.filipes.rituals.screen.AmethystHourglassScreen;
import net.filipes.rituals.screen.ModMenuTypes;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RitualsClient implements ClientModInitializer {

    public static final KeyMapping.Category RITUALS_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("rituals", "category")
    );

    public static KeyMapping actionOne;
    public static KeyMapping actionTwo;
    public static KeyMapping actionThree;


    @Override
    public void onInitializeClient() {

        actionOne = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rituals.action_one",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                RITUALS_CATEGORY
        ));

        actionTwo = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rituals.action_two",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                RITUALS_CATEGORY
        ));

        actionThree = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.rituals.action_three",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                RITUALS_CATEGORY
        ));

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            TooltipStyleHolder.clear();
            TooltipStyleHolder.set(stack);
        });

        BlockEntityRenderers.register(
                ModBlockEntities.RITUAL_PEDESTAL_BE,
                RitualPedestalBlockEntityRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.PULSE_BLASTER_BEAM,
                PulseBlasterBeamRenderer::new
        );
        EntityRendererRegistry.register(
                ModEntities.SCREEN_SHAKE,
                ScreenShakeEntityRenderer::new
        );
        EntityRendererRegistry.register(
                ModEntities.ELECTRIC_BOLT,
                ElectricBoltEntityRenderer::new
        );
        ModelLayerRegistry.registerModelLayer(
                PulseBlasterBeamModel.LAYER,
                PulseBlasterBeamModel::createBodyLayer
        );

        ModelLayerRegistry.registerModelLayer(
                PulseBlasterGunModel.LAYER,
                PulseBlasterGunModel::getTexturedModelData
        );
        ModelLayerRegistry.registerModelLayer(
                PolarityTornadoBlueModel.LAYER,
                PolarityTornadoBlueModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(
                PolarityTornadoRedModel.LAYER,
                PolarityTornadoRedModel::getTexturedModelData);
        EntityRendererRegistry.register(
                ModEntities.POLARITY_TORNADO_BLUE,
                PolarityTornadoBlueEntityRenderer::new);
        EntityRendererRegistry.register(
                ModEntities.POLARITY_TORNADO_RED,
                PolarityTornadoRedEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.POLARITY_ARROW, PolarityArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDER_ARROW, CinderArrowEntityRenderer::new);


        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rituals", "pulse_blaster"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PulseBlasterSpecialRenderer.Unbaked.CODEC
        );
        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rituals", "polarity_bow"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PolarityBowSpecialRenderer.Unbaked.CODEC
        );
        EntityRendererRegistry.register(ModEntities.THROWN_DEPTHSTRIKE, ThrownDepthstrikeRenderer::new);




        MenuScreens.register(ModMenuTypes.AMETHYST_HOURGLASS, AmethystHourglassScreen::new);
        RosegoldPickaxeHudOverlay.register();
        CooldownManager.register("pickaxe_test", "Test Ability", 15_000, 0xFF66FF);
        CooldownManager.register("pharathorn_dash", "Pharathorn Dash", 25_000, 0xFFAA00);
        CooldownManager.register("lightning_rapier_teleport", "Lightning Dash", 20_000, 0x50C8FF);

        CooldownHudOverlay.register();
        PulseBlasterHudOverlay.register();
        ShadowguardHudOverlay.register();
        EntityRendererRegistry.register(ModEntities.DEATH_LASER, DeathLaserEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDERBOLT_BEAM, CinderboltBeamEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPIRAL_STAB, SpiralStabEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DASH_STAB, DashStabEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_RAPIER_TELEPORT, TeleportTrailEntityRenderer::new);
        EntityRendererRegistry.register(
                ModEntities.LIGHTNING_STRIKE,
                LightningStrikeEntityRenderer::new
        );
        EntityRendererRegistry.register(ModEntities.LIGHTNING_TRAIL, LightningTrailEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPARK, SparkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BURST_SPARK, SparkEntityRenderer::new);




        ClientPlayNetworking.registerGlobalReceiver(
                ShadowguardInvisiblePacket.TYPE,
                (packet, context) -> ShadowguardHudOverlay.trigger()
        );
        ParticleProviderRegistry.getInstance().register(
                ModParticles.LIGHTNING_BOLT_MINI,
                spriteSet -> new LightningBoltMiniParticle.Factory(spriteSet)
        );
        ParticleProviderRegistry.getInstance().register(
                ModParticles.LIGHTNING_TRAIL,
                spriteSet -> new LightningTrailParticle.Factory(spriteSet)
        );
        ParticleProviderRegistry.getInstance().register(
                ModParticles.LIGHTNING_EXPLOSION,
                spriteSet -> new LightningExplosionParticle.Factory(spriteSet)
        );
        ParticleProviderRegistry.getInstance().register(
                ModParticles.LIGHTNING_SPARK,
                spriteSet -> new LightningSparkParticle.Factory(spriteSet)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                CooldownManager.tick();
            }
            PulseBlasterCylinderState.tick();


            while (actionOne.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    if (held.getItem() instanceof RosegoldPickaxeItem
                            && RosegoldPickaxeItem.getStage(held) >= 4) {
                        ClientPlayNetworking.send(new TogglePickaxeMiningPacket());
                    }

                    else if (held.getItem() instanceof net.filipes.rituals.item.custom.PulseBlasterItem) {
                        ClientPlayNetworking.send(new FireDeathLaserPacket());
                    }

                    else if (held.getItem() instanceof PharathornItem
                            && ModDataComponents.getStage(held) >= 2) {
                        if (!CooldownManager.isOnCooldown("pharathorn_dash")) {
                            ClientPlayNetworking.send(new PharathornDashPacket());
                            CooldownManager.trigger("pharathorn_dash");
                        }
                    }
                }
            }
            while (actionTwo.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    if (held.getItem() instanceof RosegoldPickaxeItem
                            && RosegoldPickaxeItem.getStage(held) >= 4) {
                        CooldownManager.trigger("pickaxe_test");
                    }
                }
            }
            while (actionThree.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    if (held.getItem() instanceof LightningRapierItem) {
                        if (!CooldownManager.isOnCooldown("lightning_rapier_teleport")) {
                            ClientPlayNetworking.send(new LightningRapierTeleportPacket());
                            CooldownManager.trigger("lightning_rapier_teleport");
                        }
                    }
                }
            }
        });
    }
}