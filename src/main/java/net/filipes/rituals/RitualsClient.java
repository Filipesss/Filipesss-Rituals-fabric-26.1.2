package net.filipes.rituals;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.client.*;
import net.filipes.rituals.client.cooldown.CooldownHudOverlay;
import net.filipes.rituals.client.cooldown.CooldownManager;
import net.filipes.rituals.client.render.RitualPedestalBlockEntityRenderer;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.*;
import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.item.custom.*;
import net.filipes.rituals.network.*;
import net.filipes.rituals.particle.*;
import net.filipes.rituals.screen.AmethystHourglassScreen;
import net.filipes.rituals.screen.ModMenuTypes;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RitualsClient implements ClientModInitializer {

    public static final KeyMapping.Category RITUALS_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("rituals", "category")
    );

    public static KeyMapping actionOne;
    public static KeyMapping actionTwo;
    public static KeyMapping actionThree;
    private static int laserAnimTicks = 0;
    private static int solarChargeTicks      = -1;
    private static final int SOLAR_CHARGE_DURATION = 12;
    private static int     solarActiveTicks   = 0;
    private static boolean resonanceActivated = false;
    private static int clientBlightDashCount = 0;
    private static long clientBlightDashTime = 0;
    private static boolean temporalRecallCloneActive = false;
    private static int shadeshatterFrame  = 1;
    private static int shadeshatterTicker = 0;
    private static int shadeshatterSpellFrame  = -1;
    private static int shadeshatterSpellTicker = 0;
    private static final int SPELL_TICKS_PER_FRAME = 3;
    private static int shadeshatterMimicFrame  = -1;
    private static int shadeshatterMimicTicker = 0;
    private static final int MIMIC_TICKS_PER_FRAME = 3;
    private static final int TICKS_PER_FRAME = 3;
    private static int     shadeshatterWormholeFrame  = -1;
    private static int     shadeshatterWormholeTicker = 0;
    private static net.minecraft.core.BlockPos shadeshatterWormholeTarget = null;
    private static final int WORMHOLE_TICKS_PER_FRAME = 3;
    public static boolean isShadeshatterAbilityAnimationPlaying() {
        return shadeshatterMimicFrame >= 0 || shadeshatterWormholeFrame >= 0;
    }

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


        BlockEntityRenderers.register(ModBlockEntities.RITUAL_PEDESTAL_BE, RitualPedestalBlockEntityRenderer::new);

        ModelLayerRegistry.registerModelLayer(CinderboltShieldModel.LAYER, CinderboltShieldModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PulseBlasterGunModel.LAYER, PulseBlasterGunModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(PolarityTornadoBlueModel.LAYER, PolarityTornadoBlueModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(DepthstrikeGroundModel.LAYER, DepthstrikeGroundModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PharathornGroundSmashModel.LAYER, PharathornGroundSmashModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PolarityTornadoRedModel.LAYER, PolarityTornadoRedModel::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(LunarFragmentModel.LAYER, LunarFragmentModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DepthstrikeChargedBallModel.LAYER, DepthstrikeChargedBallModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SolarStormcellModel.LAYER, SolarStormcellModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PolarityShieldModel.LAYER, PolarityShieldModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TemporalShieldModel.LAYER, TemporalShieldModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TemporalRecallModel.LAYER, TemporalRecallModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ShadeshatterSpellModel.LAYER, ShadeshatterSpellModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.PULSE_BLASTER_BEAM, PulseBlasterBeamRenderer::new);
        EntityRendererRegistry.register(ModEntities.SCREEN_SHAKE, ScreenShakeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.ELECTRIC_BOLT, ElectricBoltEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_ARROW_BLUE, PolarityArrowBlueEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_TORNADO_BLUE, PolarityTornadoBlueEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_ARROW_RED, PolarityArrowRedEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_TORNADO_RED, PolarityTornadoRedEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDER_ARROW, CinderArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEATH_LASER, DeathLaserEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDERBOLT_BEAM, CinderboltBeamEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPIRAL_STAB, SpiralStabEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DASH_STAB, DashStabEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_RAPIER_TELEPORT, TeleportTrailEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_STRIKE, LightningStrikeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_TRAIL, LightningTrailEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPARK, SparkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BURST_SPARK, SparkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDERBOLT_SHIELD, CinderboltShieldEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_SPARK, LightningSparkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_EXPLOSION, LightningExplosionEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_DEPTHSTRIKE, ThrownDepthstrikeRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_TORNADO_BLUE, PolarityTornadoBlueEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.PHARATHORN_GROUND_SMASH, PharathornGroundSmashEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEPTHSTRIKE_GROUND, DepthstrikeGroundEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEPTHSTRIKE_CHARGED_BALL, DepthstrikeChargedBallEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.WIND_GUST_BIG, WindGustBigEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHADOWGUARD_GRAPPLE, ShadowguardGrappleEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIFESTEAL_MARK, LifestealMarkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.PHARATHORN_MARK, PharathornMarkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.PHARATHORN_GROUND_SMASH, PharathornGroundSmashEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CINDERBOLT_SHIELD, CinderboltShieldEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUNAR_FRAGMENT, LunarFragmentEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOLAR_STORMCELL, SolarStormcellEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUNAR_MARK, LunarMarkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOLAR_MARK, SolarMarkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUNAR_STAR, LunarStarEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SOLAR_STAR, SolarStarEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.MULTI_BURST_SPARK, MultiBurstSparkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BLIGHTED_PUDDLE, BlightedPuddleEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.REVERSE_POLARITY_ARROW, ReversePolarityArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.POLARITY_SHIELD, PolarityShieldEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.VORTEX_BOOM, VortexBoomEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.TEMPORAL_SLOW_ZONE_GROUND, TemporalSlowZoneGroundEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.TEMPORAL_SHIELD, TemporalShieldEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.TEMPORAL_MUTE_MARK, TemporalMuteMarkEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.TEMPORAL_RECALL, TemporalRecallEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHADESHATTER_SPELL, ShadeshatterSpellEntityRenderer::new);



        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rituals", "pulse_blaster"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PulseBlasterSpecialRenderer.Unbaked.CODEC
        );


        MenuScreens.register(ModMenuTypes.AMETHYST_HOURGLASS, AmethystHourglassScreen::new);
        RosegoldPickaxeHudOverlay.register();

        //COOLDOWNS
        CooldownManager.register("pickaxe_test", "Test Ability", 15_000, 0xFF66FF);
        CooldownManager.register("pharathorn_dash", "Pharathorn Dash", 25_000, 0xFFAA00);
        CooldownManager.register("pharathorn_reveal", "Pharathorn Reveal", 30_000, 0xFF4400);
        CooldownManager.register("pharathorn_fortify", "Fortify", 20_000, 0xFFAA00);
        CooldownManager.register("pharathorn_ground_smash", "Ground Smash", 20_000, 0xFFAA00);
        CooldownManager.register("pharathorn_spiral_stab", "Spiral Stab", 12_000, 0xFFAA00);
        CooldownManager.register("lightning_rapier_teleport", "Lightning Dash", 20_000, 0x50C8FF);
        CooldownManager.register("lightning_rapier_charge",     "Instant Charge",     15_000, 0xFFFF44);
        CooldownManager.register("depthstrike_ability", "Ground Shock", 30_000, 0x50FF90);
        CooldownManager.register("depthstrike_ground_ability", "Ground Spikes", 25_000, 0x50FF90);
        CooldownManager.register("depthstrike_recall", "Recall", 25_000, 0x50FF90);
        CooldownManager.register("depthstrike_charged_ball", "Charged Ball", 20_000, 0x50CFFF);
        CooldownManager.register("shadowguard_launch", "Shadow Launch", 12_000, 0x9B6DFF);
        CooldownManager.register("shadowguard_grapple", "Grapple", 15_000, 0x9B6DFF);
        CooldownManager.register("lifesteal_mark", "Lifesteal Mark", 8_000, 0x9B00FF);
        CooldownManager.register("pulse_blaster_shotgun",   "Shotgun Blast", 8_000,  0xFF6600);
        CooldownManager.register("pulse_blaster_overcharge","Overcharge",    30_000, 0xFFCC00);
        CooldownManager.register("pulse_blaster_death_laser", "Death Laser", 20_000, 0xFF2200);
        CooldownManager.register("cinderbolt_triple", "Triple Load", 20_000, 0xFF4400);
        CooldownManager.register("fire_cinderbolt_beam", "Triple Load", 20_000, 0xFF4400);
        CooldownManager.register("cinderbolt_death_save", "Last Stand", 1_200_000, 0xFF4400);
        CooldownManager.register("twins_action_two", "Lunar Fragments", 20_000, 0xAADDFF);
        CooldownManager.register("lunar_mark", "Lunar Mark", 15_000, 0xAADDFF);
        CooldownManager.register("solar_mark", "Solar Mark", 15_000, 0xAADDFF);
        CooldownManager.register("solar_blade_charge", "Solar Charge", 15_000, 0xFFDD00);
        CooldownManager.register("lunar_blade_on_hit", "Lunar Burst", 15_000, 0xAADDFF);
        CooldownManager.register("vortex_swap", "Vortex Swap", 15_000, 0x550000);
        CooldownManager.register("vortex_shockwave", "Vortex Shockwave", 22_000, 0x110022);
        CooldownManager.register("vortex_slam", "Vortex Slam", 18_000, 0xAA0000);
        CooldownManager.register("vortex_beam", "Void Beam", 15_000, 0x220033);
        CooldownManager.register("blight_web", "Blight Trap", 16_000, 0x2E4D2A);
        CooldownManager.register("blight_drain", "Life Drain", 50_000, 0x880000);
        CooldownManager.register("blight_dash", "Blight Shift", 15_000, 0x1C3318);
        CooldownManager.register("blight_tether", "Shadow Tether", 24_000, 0x4B2A5E);
        CooldownManager.register("polarity_bow_switch", "Polarity Switch", 6_000, 0x6644FF);
        CooldownManager.register("polarity_reverse_charge", "Reverse Shot", 20_000, 0xFFDD00);
        CooldownManager.register("polarity_tornado", "Polarity Tornado", 12_000, 0x6644FF);
        CooldownManager.register("polarity_bow_dash", "Polarity Dash", 10_000, 0x6644FF);
        CooldownManager.register("temporal_slow_zone", "Chronos Field", 25_000, 0x66CCFF);
        CooldownManager.register("temporal_shield_barrier", "Temporal Barrier", 25_000, 0x66CCFF);
        CooldownManager.register("temporal_mute", "Chronos Silence", 30_000, 0x66CCFF);
        CooldownManager.register("temporal_recall", "Temporal Recall", 30_000, 0x66CCFF);
        CooldownManager.register("shadeshatter_morph", "Shade Mimic", 25_000, 0xBB55FF);
        CooldownManager.register("shadeshatter_wormhole", "Wormhole", 25_000, 0xBB55FF);




        CooldownHudOverlay.register();
        PulseBlasterHudOverlay.register();
        ShadowguardHudOverlay.register();
        LunarBladeHudOverlay.register();
        SolarBladeHudOverlay.register();
        TemporalMuteHudOverlay.register();
        WarpedHudOverlay.register();
        LunarBladeActivePacket.registerClient();
        SolarBladeActivePacket.registerClient();
        TemporalMuteActivePacket.registerClient();

        ClientPlayNetworking.registerGlobalReceiver(
                ShadowguardInvisiblePacket.TYPE,
                (packet, context) -> ShadowguardHudOverlay.trigger()
        );
        ClientPlayNetworking.registerGlobalReceiver(
                TemporalRecallStartCooldownPacket.TYPE,
                (pkt, ctx) -> {
                    CooldownManager.trigger("temporal_recall");
                    temporalRecallCloneActive = false;
                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ReverseControlsPacket.TYPE,
                (packet, context) -> ReverseControlsHandler.trigger(packet.durationTicks)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                CinderboltSaveTriggeredPacket.TYPE,
                (packet, context) -> {
                    CooldownManager.trigger("cinderbolt_death_save");
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TwinsStartCooldownPacket.TYPE,
                (packet, context) -> CooldownManager.trigger("twins_action_two")
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ShadeshatterSpellStartPacket.TYPE,
                (pkt, ctx) -> {
                    shadeshatterSpellFrame  = 0;
                    shadeshatterSpellTicker = 0;
                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ShadeshatterHastePacket.TYPE,
                (packet, ctx) -> CooldownManager.setTickRate(packet.tickRate())
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ShadeshatterAbilityResetPacket.TYPE,
                (packet, ctx) -> CooldownManager.clearAll()
        );

        //PARTICLES
        ParticleProviderRegistry.getInstance().register(ModParticles.LIGHTNING_BOLT_MINI, spriteSet -> new LightningBoltMiniParticle.Factory(spriteSet));
        ParticleProviderRegistry.getInstance().register(ModParticles.LIGHTNING_TRAIL, spriteSet -> new LightningTrailParticle.Factory(spriteSet));
        ParticleProviderRegistry.getInstance().register(ModParticles.LIGHTNING_EXPLOSION, spriteSet -> new LightningExplosionParticle.Factory(spriteSet));
        ParticleProviderRegistry.getInstance().register(ModParticles.LIGHTNING_SPARK, spriteSet -> new LightningSparkParticle.Factory(spriteSet));
        ParticleProviderRegistry.getInstance().register(ModParticles.BLIGHTED, BlightedParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.GLASSREAVER_CRIT, GlassreaverCritParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.TEMPORAL_HOURGLASS, TemporalHourglassParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.MOON, MoonParticle.Factory::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                CooldownManager.tick();
                ReverseControlsHandler.tick();
            }
            if (client.player == null) return;
            PulseBlasterCylinderState.tick();
            shadeshatterTicker++;
            if (shadeshatterTicker % TICKS_PER_FRAME == 0) {
                shadeshatterFrame = (shadeshatterFrame % 4) + 1;
            }

            ItemStack shadeshatterStack = client.player != null
                    ? client.player.getMainHandItem() : ItemStack.EMPTY;

            if (shadeshatterStack.is(ModItems.SHADESHATTER)) {
                float frameValue;

                if (shadeshatterMimicFrame >= 0) {
                    frameValue = shadeshatterMimicFrame + 19f;
                    shadeshatterMimicTicker++;
                    if (shadeshatterMimicTicker >= MIMIC_TICKS_PER_FRAME) {
                        shadeshatterMimicTicker = 0;
                        shadeshatterMimicFrame++;
                        if (shadeshatterMimicFrame >= 14) {
                            shadeshatterMimicFrame = -1;
                            ClientPlayNetworking.send(new ShadeshatterMorphPacket());
                        }
                    }
                } else if (shadeshatterSpellFrame >= 0) {
                    frameValue = shadeshatterSpellFrame + 5f;
                    shadeshatterSpellTicker++;
                    if (shadeshatterSpellTicker >= TICKS_PER_FRAME) {
                        shadeshatterSpellTicker = 0;
                        shadeshatterSpellFrame++;
                        if (shadeshatterSpellFrame >= 14) {
                            shadeshatterSpellFrame = -1;
                        }
                    }
                } else if (shadeshatterWormholeFrame >= 0) {
                    frameValue = shadeshatterWormholeFrame + 33f;
                    shadeshatterWormholeTicker++;
                    if (shadeshatterWormholeTicker >= WORMHOLE_TICKS_PER_FRAME) {
                        shadeshatterWormholeTicker = 0;
                        shadeshatterWormholeFrame++;
                        if (shadeshatterWormholeFrame >= 14) {
                            shadeshatterWormholeFrame = -1;
                            if (shadeshatterWormholeTarget != null) {
                                ClientPlayNetworking.send(new ShadeshatterWormholePacket(shadeshatterWormholeTarget));
                                shadeshatterWormholeTarget = null;
                            }
                        }
                    }
                } else {
                    frameValue = shadeshatterFrame;
                }

                shadeshatterStack.set(
                        DataComponents.CUSTOM_MODEL_DATA,
                        new CustomModelData(List.of(frameValue), List.of(), List.of(), List.of())
                );
            }
            if (solarChargeTicks > 0) {
                if (client.player != null) {
                    var heldDuringCharge = client.player.getMainHandItem().getItem();
                    if (heldDuringCharge instanceof SolarBladeItem
                            || heldDuringCharge instanceof LunarBladeItem) {
                        solarChargeTicks--;
                        if (solarChargeTicks == 0) {
                            solarChargeTicks = -1;
                            ItemStack offhand = client.player.getOffhandItem();
                            boolean dualCombo = offhand.getItem() instanceof LunarBladeItem
                                    && ModDataComponents.getStage(offhand) >= 5;
                            ClientPlayNetworking.send(new TwinsActionTwoPacket(true, dualCombo));
                            solarActiveTicks = 120;
                        }
                    } else {
                        solarChargeTicks = -1;
                    }
                } else {
                    solarChargeTicks = -1;
                }
            }
            if (clientBlightDashCount == 1) {
                long now = System.currentTimeMillis();

                if (now - clientBlightDashTime > 10000L) {
                    clientBlightDashCount = 0;
                    CooldownManager.trigger("blight_dash");
                }
            }

            if (solarActiveTicks > 0) solarActiveTicks--;


            while (actionOne.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    int stage = ModDataComponents.getStage(held);

                    if (held.getItem() == ModItems.DEPTHSTRIKE && mc.player.isShiftKeyDown()) {
                        if (stage >= 5 && !CooldownManager.isOnCooldown("depthstrike_charged_ball")) {
                            ClientPlayNetworking.send(new DepthstrikeChargedBallPacket());
                            CooldownManager.trigger("depthstrike_charged_ball");
                        }
                    }  else if (held.getItem() == ModItems.DEPTHSTRIKE && !mc.player.isShiftKeyDown()) {
                        if (stage >= 4 && !CooldownManager.isOnCooldown("depthstrike_recall")) {
                            ClientPlayNetworking.send(new DepthstrikeRecallPacket());
                            CooldownManager.trigger("depthstrike_recall");
                        }
                    } else if (held.getItem() instanceof PolarityBowItem) {
                        if (mc.player.isShiftKeyDown()) {
                            if (stage >= 6 && !CooldownManager.isOnCooldown("polarity_bow_dash")) {
                                ClientPlayNetworking.send(new PolarityBowDashPacket());
                                CooldownManager.trigger("polarity_bow_dash");
                            }
                        } else {
                            if (!CooldownManager.isOnCooldown("polarity_bow_switch")) {
                                ClientPlayNetworking.send(new PolarityBowSwitchPacket());
                                CooldownManager.trigger("polarity_bow_switch");
                            }
                        }
                    } else if (held.getItem() instanceof LunarBladeItem) {
                        if (stage >= 2 && !CooldownManager.isOnCooldown("lunar_mark")) {
                            ClientPlayNetworking.send(new LunarMarkPacket());
                            CooldownManager.trigger("lunar_mark");
                        }
                    } else if (held.getItem() instanceof TemporalGlassreaverItem) {
                        if (mc.player.isShiftKeyDown()) {

                            if (stage >= 7 && !CooldownManager.isOnCooldown("temporal_slow_zone")) {
                                ClientPlayNetworking.send(new TemporalSlowZonePacket());
                                CooldownManager.trigger("temporal_slow_zone");
                            }
                        } else {
                            if (stage >= 2 && !CooldownManager.isOnCooldown("temporal_shield_barrier")) {
                                ClientPlayNetworking.send(new TemporalShieldPacket());
                                CooldownManager.trigger("temporal_shield_barrier");
                            }
                        }
                    } else if (held.getItem() instanceof SolarBladeItem) {
                        if (stage >= 2 && !CooldownManager.isOnCooldown("solar_mark")) {
                            ClientPlayNetworking.send(new SolarMarkPacket());
                            CooldownManager.trigger("solar_mark");
                        }
                    } else if (held.getItem() instanceof BlightspearItem) {
                        if (mc.player != null) {
                            if (mc.player.isShiftKeyDown()) {
                                if (!CooldownManager.isOnCooldown("blight_tether") && ModDataComponents.getStage(held) >= 6) {
                                    double maxDist = 20.0;

                                    net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition(1.0F);
                                    net.minecraft.world.phys.Vec3 lookDir = mc.player.getLookAngle();
                                    net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookDir.scale(maxDist));
                                    net.minecraft.world.phys.AABB searchBox = mc.player.getBoundingBox().expandTowards(lookDir.scale(maxDist)).inflate(1.0D);

                                    net.minecraft.world.phys.EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                            mc.player, eyePos, endPos, searchBox, entity -> !entity.isSpectator() && entity.isAlive(), maxDist * maxDist
                                    );

                                    if (entityHit != null) {
                                        ClientPlayNetworking.send(new BlightTetherPacket(entityHit.getEntity().getId()));
                                    } else {
                                        mc.player.playSound(SoundEvents.CHAIN_BREAK, 0.6f, 1.5f);
                                    }

                                    CooldownManager.trigger("blight_tether");
                                }
                            }
                            else {
                                if (!CooldownManager.isOnCooldown("blight_web") && ModDataComponents.getStage(held) >= 2) {
                                    double maxDist = 20.0;

                                    net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition(1.0F);
                                    net.minecraft.world.phys.Vec3 lookDir = mc.player.getLookAngle();
                                    net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookDir.scale(maxDist));
                                    net.minecraft.world.phys.AABB searchBox = mc.player.getBoundingBox().expandTowards(lookDir.scale(maxDist)).inflate(1.0D);

                                    net.minecraft.world.phys.EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                            mc.player, eyePos, endPos, searchBox, entity -> !entity.isSpectator() && entity.isAlive(), maxDist * maxDist
                                    );

                                    if (entityHit != null) {
                                        net.minecraft.world.entity.Entity target = entityHit.getEntity();
                                        ClientPlayNetworking.send(new BlightWebPacket(target.getId()));
                                    } else {
                                        mc.player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 1.5f);
                                    }

                                    CooldownManager.trigger("blight_web");
                                }
                            }
                        }
                    } else if (held.getItem() instanceof ShadowguardItem) {
                        if (stage >= 4 && !CooldownManager.isOnCooldown("shadowguard_launch")) {
                            ClientPlayNetworking.send(new ShadowguardLaunchPacket());
                            CooldownManager.trigger("shadowguard_launch");
                        }
                    } else if (held.getItem() instanceof CinderboltItem
                            && ModDataComponents.getStage(held) >= 4) {
                        if (!CooldownManager.isOnCooldown("cinderbolt_triple")) {
                            ClientPlayNetworking.send(new CinderboltTriplePacket());
                            CooldownManager.trigger("cinderbolt_triple");
                        }
                    }  else if (held.getItem() instanceof PulseBlasterItem) {
                        if (!mc.player.isShiftKeyDown()
                                && ModDataComponents.getStage(held) >= 4
                                && !CooldownManager.isOnCooldown("pulse_blaster_shotgun")) {
                            ClientPlayNetworking.send(new PulseBlasterShotgunPacket());
                            CooldownManager.trigger("pulse_blaster_shotgun");
                        }
                    } else if (held.getItem() instanceof VortexEdgeItem) {
                        if (mc.player.isShiftKeyDown()) {
                            if (stage >= 3 && !CooldownManager.isOnCooldown("vortex_beam")) {
                                ClientPlayNetworking.send(new VortexBeamPacket());
                                CooldownManager.trigger("vortex_beam");
                            }
                        } else {
                            if (!CooldownManager.isOnCooldown("vortex_swap")
                                    && ModDataComponents.getStage(held) >= 2) {

                                if (mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                                    net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) mc.hitResult;
                                    net.minecraft.world.entity.Entity target = entityHit.getEntity();

                                    ClientPlayNetworking.send(new VortexSwapPacket(target.getId()));
                                } else {
                                    if (mc.player != null) {
                                        mc.player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 1.5f);
                                    }
                                }
                                CooldownManager.trigger("vortex_swap");
                            }
                        }
                    } else if (held.getItem() instanceof ShadeshatterItem) {
                        if (shadeshatterMimicFrame == -1
                                && shadeshatterSpellFrame == -1
                                && !client.player.getCooldowns().isOnCooldown(held)
                                && !CooldownManager.isOnCooldown("shadeshatter_morph")) {
                            shadeshatterMimicFrame  = 0;
                            shadeshatterMimicTicker = 0;
                            CooldownManager.trigger("shadeshatter_morph");
                        }
                    } else if (held.getItem() instanceof RosegoldPickaxeItem
                            && RosegoldPickaxeItem.getStage(held) >= 4) {
                        ClientPlayNetworking.send(new TogglePickaxeMiningPacket());
                    } else if (held.getItem() instanceof LightningRapierItem
                            && ModDataComponents.getStage(held) >= 4) {
                        if (!CooldownManager.isOnCooldown("lightning_rapier_charge")) {
                            ClientPlayNetworking.send(new LightningRapierInstantChargePacket());
                            CooldownManager.trigger("lightning_rapier_charge");
                        }
                    } else if (held.getItem() instanceof PharathornItem) {
                        if (mc.player.isShiftKeyDown() && stage >= 6) {
                            if (!CooldownManager.isOnCooldown("pharathorn_spiral_stab")) {
                                ClientPlayNetworking.send(new SpiralStabPacket());
                                CooldownManager.trigger("pharathorn_spiral_stab");
                            }
                        } else if (!mc.player.isShiftKeyDown() && stage >= 2) {
                            if (!CooldownManager.isOnCooldown("pharathorn_fortify")) {
                                ClientPlayNetworking.send(new PharathornFortifyPacket());
                                CooldownManager.trigger("pharathorn_fortify");
                            }
                        }
                    }
                }
            }

            while (actionTwo.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    int stage = ModDataComponents.getStage(held);

                    if (held.getItem() instanceof ShadowguardItem) {
                        if (stage >= 5 && !CooldownManager.isOnCooldown("lifesteal_mark")) {
                            ClientPlayNetworking.send(new LifestealMarkPacket());
                            CooldownManager.trigger("lifesteal_mark");
                        }

                    } else if (held.getItem() instanceof SolarBladeItem) {
                        if (stage >= 4 && !CooldownManager.isOnCooldown("solar_blade_charge")) {
                            ClientPlayNetworking.send(new SolarBladeChargePacket());
                            CooldownManager.trigger("solar_blade_charge");
                        }
                    } else if (held.getItem() instanceof LunarBladeItem) {
                        if (stage >= 4 && !CooldownManager.isOnCooldown("lunar_blade_on_hit")) {
                            ClientPlayNetworking.send(new LunarBladeOnHitPacket());
                            CooldownManager.trigger("lunar_blade_on_hit");
                        }
                    } else if (held.getItem() instanceof BlightspearItem) {
                        if (!CooldownManager.isOnCooldown("blight_dash") && ModDataComponents.getStage(held) >= 3) {

                            ClientPlayNetworking.send(new BlightDashPacket());

                            if (clientBlightDashCount == 0) {
                                clientBlightDashCount = 1;
                                clientBlightDashTime = System.currentTimeMillis();
                            } else {
                                clientBlightDashCount = 0;
                                CooldownManager.trigger("blight_dash");
                            }
                        }
                    } else if (held.getItem() instanceof TemporalGlassreaverItem) {
                        if (stage >= 3 && !CooldownManager.isOnCooldown("temporal_mute")) {
                            ClientPlayNetworking.send(new TemporalMutePacket());
                            CooldownManager.trigger("temporal_mute");
                        }
                    } else if (held.getItem() instanceof VortexEdgeItem
                            && ModDataComponents.getStage(held) >= 3) {
                        if (!CooldownManager.isOnCooldown("vortex_shockwave")) {
                            ClientPlayNetworking.send(new VortexShockwavePacket());
                            CooldownManager.trigger("vortex_shockwave");
                        }
                    } else if (held.getItem() instanceof PolarityBowItem
                            && ModDataComponents.getStage(held) >= 2) {
                        if (!CooldownManager.isOnCooldown("polarity_reverse_charge")) {
                            ClientPlayNetworking.send(new ReversePolarityChargePacket());
                            CooldownManager.trigger("polarity_reverse_charge");
                        }
                    } else if (held.getItem() instanceof ShadeshatterItem) {
                        if (shadeshatterWormholeFrame == -1
                                && shadeshatterMimicFrame == -1
                                && shadeshatterSpellFrame == -1
                                && !CooldownManager.isOnCooldown("shadeshatter_wormhole")
                                && !client.player.getCooldowns().isOnCooldown(held)) {

                            var longRay = mc.player.pick(24.0, 1.0f, false);
                            if (longRay instanceof net.minecraft.world.phys.BlockHitResult blockHit
                                    && blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                                ClientPlayNetworking.send(new ShadeshatterWormholePacket(blockHit.getBlockPos()));
                                CooldownManager.trigger("shadeshatter_wormhole");
                                shadeshatterWormholeFrame  = 0;
                                shadeshatterWormholeTicker = 0;
                            }
                        }
                    } else if (held.getItem() instanceof CinderboltItem
                            && ModDataComponents.getStage(held) >= 5) {
                        if (!CooldownManager.isOnCooldown("fire_cinderbolt_beam")) {
                            ClientPlayNetworking.send(new FireCinderboltBeamPacket());
                            CooldownManager.trigger("fire_cinderbolt_beam");
                        }
                    } else if (held.getItem() instanceof PulseBlasterItem) {
                        if (ModDataComponents.getStage(held) >= 5
                                && !CooldownManager.isOnCooldown("pulse_blaster_overcharge")) {
                            ClientPlayNetworking.send(new PulseBlasterOverchargePacket());
                            CooldownManager.trigger("pulse_blaster_overcharge");
                            PulseBlasterHudOverlay.triggerOvercharge();
                        }
                    } else if (held.getItem() instanceof RosegoldPickaxeItem
                            && RosegoldPickaxeItem.getStage(held) >= 4) {
                        CooldownManager.trigger("pickaxe_test");
                    } else if (held.getItem() == ModItems.DEPTHSTRIKE) {
                        if (stage >= 3 && !CooldownManager.isOnCooldown("depthstrike_ability")) {
                            ClientPlayNetworking.send(new DepthstrikeAbilityPacket());
                            CooldownManager.trigger("depthstrike_ability");
                        }
                    } else if (held.getItem() instanceof LightningRapierItem
                            && ModDataComponents.getStage(held) >= 5) {
                        if (!CooldownManager.isOnCooldown("lightning_rapier_teleport")) {
                            ClientPlayNetworking.send(new LightningRapierTeleportPacket());
                            CooldownManager.trigger("lightning_rapier_teleport");
                        }
                    } else if (held.getItem() instanceof PharathornItem) {
                        if (mc.player.isShiftKeyDown() && stage >= 7) {
                            if (!CooldownManager.isOnCooldown("pharathorn_ground_smash")) {
                                ClientPlayNetworking.send(new PharathornGroundSmashPacket());
                                CooldownManager.trigger("pharathorn_ground_smash");
                            }
                        } else if (!mc.player.isShiftKeyDown() && stage >= 3) {
                            if (!CooldownManager.isOnCooldown("pharathorn_reveal")) {
                                ClientPlayNetworking.send(new PharathornRevealPacket());
                                CooldownManager.trigger("pharathorn_reveal");
                            }
                        }
                    }
                }
            }

            while (actionThree.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    var held = mc.player.getMainHandItem();
                    int stage = ModDataComponents.getStage(held);

                    if (held.getItem() instanceof ShadowguardItem) {
                        if (stage >= 7 && !CooldownManager.isOnCooldown("shadowguard_grapple")) {
                            ClientPlayNetworking.send(new ShadowguardGrapplePacket());
                            CooldownManager.trigger("shadowguard_grapple");
                        }

                    } else if (held.getItem() instanceof LunarBladeItem) {
                        if (stage >= 5 && !CooldownManager.isOnCooldown("twins_action_two")) {
                            ClientPlayNetworking.send(new TwinsActionTwoPacket());
                        }

                    } else if (held.getItem() instanceof PolarityBowItem
                            && ModDataComponents.getStage(held) >= 5) {
                        if (!CooldownManager.isOnCooldown("polarity_tornado")) {
                            ClientPlayNetworking.send(new PolarityTornadoLaunchPacket());
                            CooldownManager.trigger("polarity_tornado");
                        }
                    }  else if (held.getItem() instanceof BlightspearItem) {
                        if (!CooldownManager.isOnCooldown("blight_drain") && ModDataComponents.getStage(held) >= 5) {
                            CooldownManager.trigger("blight_drain");

                            if (mc.player != null) {
                                double maxDist = 20.0;

                                net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition(1.0F);
                                net.minecraft.world.phys.Vec3 lookDir = mc.player.getLookAngle();
                                net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookDir.scale(maxDist));
                                net.minecraft.world.phys.AABB searchBox = mc.player.getBoundingBox().expandTowards(lookDir.scale(maxDist)).inflate(1.0D);

                                net.minecraft.world.phys.EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                        mc.player, eyePos, endPos, searchBox, entity -> !entity.isSpectator() && entity.isAlive(), maxDist * maxDist
                                );

                                if (entityHit != null) {
                                    ClientPlayNetworking.send(new BlightDrainPacket(entityHit.getEntity().getId()));
                                } else {
                                    mc.player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 1.2f);
                                }
                            }
                        }
                    } else if (held.getItem() instanceof SolarBladeItem) {
                        if (stage >= 5 && !CooldownManager.isOnCooldown("twins_action_two")
                                && solarChargeTicks == -1) {
                            solarChargeTicks   = SOLAR_CHARGE_DURATION;
                            resonanceActivated = false;
                        }
                    } else if (held.getItem() instanceof PulseBlasterItem) {
                        if (ModDataComponents.getStage(held) >= 6
                                && !CooldownManager.isOnCooldown("pulse_blaster_death_laser")) {
                            ClientPlayNetworking.send(new FireDeathLaserPacket());
                            CooldownManager.trigger("pulse_blaster_death_laser");
                        }
                    } else if (held.getItem() instanceof VortexEdgeItem
                            && ModDataComponents.getStage(held) >= 5) {
                        if (!CooldownManager.isOnCooldown("vortex_slam")) {
                            if (mc.player != null && !mc.player.onGround()) {
                                ClientPlayNetworking.send(new VortexSlamPacket());
                                CooldownManager.trigger("vortex_slam");
                            } else if (mc.player != null) {
                                mc.player.playSound(SoundEvents.DISPENSER_FAIL, 1.0f, 1.2f);
                            }
                        }
                    }  else if (held.getItem() == ModItems.DEPTHSTRIKE) {
                        if (stage >= 6 && !CooldownManager.isOnCooldown("depthstrike_ground_ability")) {
                            ClientPlayNetworking.send(new DepthstrikeGroundAbilityPacket());
                            CooldownManager.trigger("depthstrike_ground_ability");
                        }
                    } else if (held.getItem() instanceof PharathornItem && stage >= 5) {
                        if (!CooldownManager.isOnCooldown("pharathorn_dash")) {
                            ClientPlayNetworking.send(new PharathornDashPacket());
                            CooldownManager.trigger("pharathorn_dash");
                        }
                    } else if (held.getItem() instanceof TemporalGlassreaverItem) {
                        if (stage >= 5 && !CooldownManager.isOnCooldown("temporal_recall")) {
                            ClientPlayNetworking.send(new TemporalRecallPacket());
                            temporalRecallCloneActive = !temporalRecallCloneActive;
                        }
                    }
                }
            }
        });
    }
}