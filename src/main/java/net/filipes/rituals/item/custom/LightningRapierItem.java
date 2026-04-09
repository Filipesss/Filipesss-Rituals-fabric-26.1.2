package net.filipes.rituals.item.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class LightningRapierItem extends Item implements RitualsTooltipStyle {

    private static final int STUN_DURATION_TICKS = 10;
    private static final double CHAIN_RADIUS = 8.0;
    private static final float CHAIN_DAMAGE = 3.0f;

    public LightningRapierItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyStun(target);

        Level world = target.level();
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;

            List<LivingEntity> nearby = world.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(CHAIN_RADIUS),
                    entity -> entity != target && entity != attacker && entity.isAlive()
            );

            nearby.sort((a, b) -> Double.compare(
                    a.distanceToSqr(target),
                    b.distanceToSqr(target)
            ));

            if (nearby.isEmpty()) {
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK2,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK1,
                        SoundSource.PLAYERS, 1.0f, 1.0f);

                LivingEntity previous = target;
                for (LivingEntity chained : nearby) {
                    spawnLightningChain(serverWorld, previous, chained);
                    applyStun(chained);
                    chained.hurt(serverWorld.damageSources().lightningBolt(), CHAIN_DAMAGE);
                    previous = chained;
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    private void spawnLightningChain(ServerLevel world, LivingEntity from, LivingEntity to) {
        double x1 = from.getX(), y1 = from.getY(0.5), z1 = from.getZ();
        double x2 = to.getX(),   y2 = to.getY(0.5),   z2 = to.getZ();

        List<double[]> points = new ArrayList<>();
        points.add(new double[]{x1, y1, z1});
        points.add(new double[]{x2, y2, z2});

        int subdivisions = 7;
        for (int s = 0; s < subdivisions; s++) {
            List<double[]> next = new ArrayList<>();
            double displacement = 0.9 / sqrt(s + 1); //wideness
            for (int i = 0; i < points.size() - 1; i++) {
                double[] a = points.get(i);
                double[] b = points.get(i + 1);
                next.add(a);
                next.add(new double[]{
                        (a[0] + b[0]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement,
                        (a[1] + b[1]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement,
                        (a[2] + b[2]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement
                });
            }
            next.add(points.get(points.size() - 1));
            points = next;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            double[] a = points.get(i);
            double[] b = points.get(i + 1);
            drawSegment(world, a[0], a[1], a[2], b[0], b[1], b[2]);
        }
    }

    private void drawSegment(ServerLevel world, double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double dist = sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int)(dist / 0.15));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;

            world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0.0);
            world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void applyStun(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                ModStatusEffects.STUN,
                STUN_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(s -> s.withColor(getNameColor()).withItalic(false));
    }


    @Override
    public int getNameColor() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xFF550000;
    }
}