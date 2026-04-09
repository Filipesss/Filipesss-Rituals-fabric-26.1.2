package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("TAIL"))
    private void rituals$gateRosegoldAnvilEnchanting(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu)(Object)this;
        ItemStack left = self.getSlot(0).getItem();

        if (left.getItem() instanceof RosegoldPickaxeItem
                && RosegoldPickaxeItem.getStage(left) < 2) {
            self.getSlot(2).set(ItemStack.EMPTY);
        }
    }
}
