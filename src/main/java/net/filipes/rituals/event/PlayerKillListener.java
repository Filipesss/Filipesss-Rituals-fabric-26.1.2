package net.filipes.rituals.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.upgrade.KillUpgradeRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class PlayerKillListener {

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killed, damageSource) -> {
            if (!(killer instanceof ServerPlayer killerPlayer)) return;
            if (!(killed instanceof ServerPlayer) && !(killed instanceof Mannequin)) return;

            if (killed instanceof ServerPlayer killedPlayer) {
                GameProfile gameProfile = killedPlayer.getGameProfile();
                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));
                killedPlayer.spawnAtLocation(world, head);
            }

            ItemStack weapon = killerPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            if (weapon.isEmpty() || !KillUpgradeRegistry.isKillUpgradeable(weapon.getItem())) return;

            KillUpgradeRegistry.getRecipe(weapon).ifPresent(recipe -> {
                int currentStage = ModDataComponents.getStage(weapon);
                int newKills = ModDataComponents.getKillCount(weapon) + 1;

                ItemStack updated = ModDataComponents.withKillCount(weapon, newKills);

                if (newKills >= recipe.getKillsRequired()) {
                    updated = ModDataComponents.withStage(updated, recipe.getResultStage());
                    killerPlayer.setItemInHand(InteractionHand.MAIN_HAND, updated);
                    killerPlayer.sendSystemMessage(
                            Component.translatable("item.rituals.upgrade.kill_upgrade")
                                    .withStyle(style -> style.withColor(0xFF0000))
                    );
                } else {
                    killerPlayer.setItemInHand(InteractionHand.MAIN_HAND, updated);
                }
            });
        });
    }
}