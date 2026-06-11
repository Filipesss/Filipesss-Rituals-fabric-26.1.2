package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.filipes.rituals.entity.custom.LightningStrikeEntity;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public class LightningRapierItem extends Item implements RitualsTooltipStyle, RitualsEnchantable {

    private static final float  WEATHER_BONUS_DAMAGE = 3.0f;
    private static final double CHAIN_RADIUS         = 8.0;
    private static final float  CHAIN_DAMAGE         = 3.0f;
    private static final int    STUN_DURATION_TICKS  = 60;
    private static final int    STREAK_NEEDED        = 6;

    public LightningRapierItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }
    private static final EnchantmentPolicy POLICY =
            EnchantmentPolicy.restricted(Enchantments.SHARPNESS);


    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int stage = ModDataComponents.getStage(stack);
        Level world = attacker.level();

        if (!world.isClientSide() && attacker instanceof ServerPlayer player) {
            ServerLevel serverLevel = (ServerLevel) world;

            if (hasWeatherBonus(player, serverLevel)) {
                target.hurt(serverLevel.damageSources().lightningBolt(), WEATHER_BONUS_DAMAGE);
            }

            if (stage >= 3) {
                handleChargeSystem(stack, target, player, serverLevel, stage);
            }

            List<LivingEntity> chainedTargets = List.of();
            if (stage >= 2) {
                chainedTargets = doChainLightning(world, serverLevel, target, attacker, stage);
            }

            boolean didChain = !chainedTargets.isEmpty();
            float pitch = 0.6f + world.getRandom().nextFloat() * 0.7f;

            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    didChain ? ModSounds.LIGHTNING_BOLT_3 : ModSounds.LIGHTNING_BOLT,
                    SoundSource.PLAYERS, 1.0f, pitch);

            if (stage >= 6) {
                applyStun(target);
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }


    private void handleChargeSystem(ItemStack stack, LivingEntity target,
                                    ServerPlayer player, ServerLevel serverLevel, int stage) {
        int charge = getCharge(stack);

        if (charge >= 6) {
            float bonusDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            target.hurt(serverLevel.damageSources().playerAttack(player), bonusDmg);

            spawnLightningStrike(serverLevel, target);

            setCharge(stack, 0);
            LightningRapierStreakTracker.reset(player.getUUID());

            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.LIGHTNING_BOLT_2, SoundSource.PLAYERS, 1.4f, 0.7f);

        } else {
            int streak = LightningRapierStreakTracker.onHit(
                    player.getUUID(), target.getUUID(), serverLevel.getGameTime());

            setCharge(stack, streak);

            if (streak >= STREAK_NEEDED) {
                LightningRapierStreakTracker.reset(player.getUUID());

                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.LIGHTNING_BOLT_3, SoundSource.PLAYERS, 1.0f, 0.4f);
            }
        }
    }


    private List<LivingEntity> doChainLightning(Level world, ServerLevel serverLevel,
                                                LivingEntity primaryTarget,
                                                LivingEntity attacker, int stage) {
        List<LivingEntity> nearby = world.getEntitiesOfClass(
                LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(CHAIN_RADIUS),
                e -> e != primaryTarget && e != attacker && e.isAlive()
        );

        nearby.sort((a, b) -> Double.compare(
                a.distanceToSqr(primaryTarget),
                b.distanceToSqr(primaryTarget)
        ));

        if (nearby.isEmpty()) return List.of();

        LivingEntity previous = primaryTarget;
        for (LivingEntity chainTarget : nearby) {
            ElectricBoltEntity.spawn(serverLevel,
                    previous.getEyePosition(), chainTarget.getEyePosition(),
                    2.0f, 0.14f, 0x98E8FF);
            chainTarget.hurt(serverLevel.damageSources().lightningBolt(), CHAIN_DAMAGE);
            if (stage >= 6) applyStun(chainTarget);
            previous = chainTarget;
        }

        return nearby;
    }

    private static boolean hasWeatherBonus(ServerPlayer player, ServerLevel level) {
        if (player.isInWater()) return true;
        if (level.isRaining() && level.canSeeSky(player.blockPosition())) return true;
        return level.isThundering();
    }

    private static void applyStun(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                ModStatusEffects.STUN, STUN_DURATION_TICKS, 0, false, true, true));
    }

    private static void spawnLightningStrike(ServerLevel level, LivingEntity target) {
        LightningStrikeEntity.spawnAt(level,
                target.getX(), target.getY(), target.getZ(),
                14f, 15,
                80, 160, 255,
                0f, 0f,
                18);
    }

    public static int getCharge(ItemStack stack) {
        Integer c = stack.get(ModDataComponents.LIGHTNING_RAPIER_CHARGE);
        return c != null ? c : 0;
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.LIGHTNING_RAPIER_CHARGE, Math.min(6, Math.max(0, charge)));
    }

    public static int addCharge(ItemStack stack, int delta) {
        int next = Math.min(6, Math.max(0, getCharge(stack) + delta));
        stack.set(ModDataComponents.LIGHTNING_RAPIER_CHARGE, next);
        return next;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ModDataComponents.getStage(stack) >= 3 && getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((getCharge(stack) / 6f) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float t = getCharge(stack) / 6f;
        int r = 0xFF;
        int g = (int) (t * 0xFF);
        int b = 0;
        return (r << 16) | (g << 8) | b;
    }
    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }



    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(s -> s.withColor(getNameColor()).withItalic(false));
    }

    @Override public int getNameColor()               { return 0x50C8FF; }
    @Override public int getTooltipBorderColorTop()   { return 0x50C8FF; }
    @Override public int getTooltipBorderColorBottom(){ return 0x0A2244; }
    @Override public int getTooltipBackgroundColor()  { return 0xFF001133; }
}