package net.filipes.rituals.item.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LightningRapierItem extends Item implements RitualsTooltipStyle {

    private static final int STUN_DURATION_TICKS = 10;
    private static final double CHAIN_RADIUS = 8.0;
    private static final float CHAIN_DAMAGE = 3.0f;

    public LightningRapierItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyStun(target);

        World world = target.getEntityWorld();
        if (!world.isClient()) {
            ServerWorld serverWorld = (ServerWorld) world;

            List<LivingEntity> nearby = world.getEntitiesByClass(
                    LivingEntity.class,
                    target.getBoundingBox().expand(CHAIN_RADIUS),
                    entity -> entity != target && entity != attacker && entity.isAlive()
            );

            nearby.sort((a, b) -> Double.compare(
                    a.squaredDistanceTo(target),
                    b.squaredDistanceTo(target)
            ));

            if (nearby.isEmpty()) {
                // No chain — play attack2
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK2,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
            } else {
                // Chain triggered — play attack1
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK1,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);

                LivingEntity previous = target;
                for (LivingEntity chained : nearby) {
                    spawnLightningChain(serverWorld, previous, chained);
                    applyStun(chained);
                    chained.damage(serverWorld, serverWorld.getDamageSources().lightningBolt(), CHAIN_DAMAGE);
                    previous = chained;
                }
            }
        }

        super.postHit(stack, target, attacker);
    }

    private void spawnLightningChain(ServerWorld world, LivingEntity from, LivingEntity to) {
        double x1 = from.getX(), y1 = from.getBodyY(0.5), z1 = from.getZ();
        double x2 = to.getX(),   y2 = to.getBodyY(0.5),   z2 = to.getZ();

        List<double[]> points = new ArrayList<>();
        points.add(new double[]{x1, y1, z1});
        points.add(new double[]{x2, y2, z2});

        int subdivisions = 7;       // was 5 — more waypoints
        for (int s = 0; s < subdivisions; s++) {
            List<double[]> next = new ArrayList<>();
            double displacement = 4.0 / (s + 1); // was 0.9 — much wilder initial bends
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

    private void drawSegment(ServerWorld world, double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int)(dist / 0.15));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;

            // Single bright core, no extra spread
            world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0.0);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void applyStun(LivingEntity entity) {
        entity.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.STUN,
                STUN_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }



    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey())
                .styled(s -> s.withColor(getNameColor()).withItalic(false));
    }

    @Override public int getNameColor()              { return 0xFF9B6DFF; }
    @Override public int getTooltipBorderColor()     { return 0xFFBB99FF; }
    @Override public int getTooltipBackgroundColor() { return 0xE5080020; }
}