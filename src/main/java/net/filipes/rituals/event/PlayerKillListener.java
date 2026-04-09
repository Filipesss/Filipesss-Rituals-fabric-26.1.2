package net.filipes.rituals.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class PlayerKillListener {

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, killer, killed, damageSource) -> {
            if (!(killer instanceof ServerPlayer killerPlayer)) return;
            if (!(killed instanceof ServerPlayer killedPlayer)) return;

            GameProfile gameProfile = killedPlayer.getGameProfile();

            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));

            killedPlayer.spawnAtLocation(world, head);
        });
    }
}