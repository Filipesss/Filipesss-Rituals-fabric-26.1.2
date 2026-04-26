package net.filipes.rituals.item.custom;

import net.filipes.rituals.entity.custom.CinderArrowEntity;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CinderboltItem extends CrossbowItem implements RitualsTooltipStyle {

    public CinderboltItem(Properties properties) {
        super(properties);
    }

    @Override
    public void performShooting(Level level, LivingEntity shooter, InteractionHand hand,
                                ItemStack stack, float speed, float divergence,
                                @Nullable LivingEntity target) {
        if (level.isClientSide()) return;

        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) return;

        for (ItemStack ammo : charged.itemCopies()) {
            if (ammo.isEmpty()) continue;

            CinderArrowEntity arrow = new CinderArrowEntity(level, shooter, ammo);

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
        }

        stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
    }

    @Override public int getNameColor()                { return 0; }
    @Override public int getTooltipBorderColorTop()    { return 0; }
    @Override public int getTooltipBorderColorBottom() { return 0; }
    @Override public int getTooltipBackgroundColor()   { return 0; }
}