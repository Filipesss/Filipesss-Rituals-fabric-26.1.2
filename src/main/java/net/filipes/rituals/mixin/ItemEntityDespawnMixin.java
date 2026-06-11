package net.filipes.rituals.mixin;

import net.filipes.rituals.item.ModTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public class ItemEntityDespawnMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
            )
    )
    private void rituals$skipDespawnDiscard(ItemEntity self) {
        if (!isPersistentDrop(self.getItem())) {
            self.discard();
        }
    }

    private static boolean isPersistentDrop(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.PERSISTENT_DROP);
    }
}