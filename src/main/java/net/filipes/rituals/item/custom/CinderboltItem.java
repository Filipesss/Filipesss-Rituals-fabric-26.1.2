package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.CinderArrowEntity;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CinderboltItem extends CrossbowItem implements RitualsTooltipStyle {

    private static final Map<UUID, Integer> cycleIndex = new HashMap<>();
    private static final Map<UUID, Integer> burstCharges = new HashMap<>();


    public CinderboltItem(Properties properties) {
        super(properties);
    }
    private static final String BURST_KEY = "BurstCharges";


    public static void markTriple(UUID id) {
        burstCharges.put(id, 3);
    }

    public static int getCycleIndex(UUID id) {
        return cycleIndex.getOrDefault(id, 0);
    }

    public static void advanceCycle(UUID id, int maxCycle) {
        int next = (cycleIndex.getOrDefault(id, 0) + 1) % maxCycle;
        cycleIndex.put(id, next);
    }

    @Override
    public void performShooting(Level level, LivingEntity shooter, InteractionHand hand,
                                ItemStack stack, float speed, float divergence,
                                @Nullable LivingEntity target) {
        if (level.isClientSide()) return;

        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        boolean fromBurst = (charged == null || charged.isEmpty());
        if (!fromBurst) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }

        int stage = ModDataComponents.getStage(stack);
        int maxCycle = switch (stage) {
            case 1  -> 1;
            case 2  -> 2;
            default -> 3;
        };

        UUID uuid = shooter.getUUID();
        int currentIndex = getCycleIndex(uuid) % maxCycle;
        int arrowType = switch (currentIndex) {
            case 0  -> CinderArrowEntity.TYPE_FIRE;
            case 1  -> CinderArrowEntity.TYPE_PIERCE;
            default -> CinderArrowEntity.TYPE_EXPLODE;
        };

        CinderArrowEntity arrow = new CinderArrowEntity(level, shooter,
                new ItemStack(Items.ARROW));
        arrow.setArrowType(arrowType);

        if (shooter instanceof Player player) {
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, speed, divergence);
        } else {
            Vec3 look = shooter.getLookAngle();
            arrow.shoot(look.x, look.y, look.z, speed, divergence);
        }

        level.addFreshEntity(arrow);

        float pitch = 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F;
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pitch);

        advanceCycle(uuid, maxCycle);
    }
    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            UUID uuid = user.getUUID();
            int remaining = burstCharges.getOrDefault(uuid, 0);
            if (remaining > 0) {
                ItemStack stack = user.getItemInHand(hand);
                performShooting(world, user, hand, stack, 3.15f, 1.0f, null);
                burstCharges.put(uuid, remaining - 1);
                if (remaining - 1 <= 0) burstCharges.remove(uuid);
                return InteractionResult.CONSUME;
            }
        }
        return super.use(world, user, hand);
    }


    @Override public int getNameColor()                { return 0; }
    @Override public int getTooltipBorderColorTop()    { return 0; }
    @Override public int getTooltipBorderColorBottom() { return 0; }
    @Override public int getTooltipBackgroundColor()   { return 0; }
}