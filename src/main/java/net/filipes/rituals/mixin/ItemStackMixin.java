package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void rituals$gateRosegoldEnchanting(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() instanceof RosegoldPickaxeItem) {
            if (RosegoldPickaxeItem.getStage(self) < 2) {
                cir.setReturnValue(false);
            }
        }
    }
}
