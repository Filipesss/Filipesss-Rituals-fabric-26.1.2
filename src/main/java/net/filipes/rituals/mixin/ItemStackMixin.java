package net.filipes.rituals.mixin;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void rituals$gateEnchantingTable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() instanceof RitualsEnchantable enchantable) {
            int stage = ModDataComponents.getStage(self);
            cir.setReturnValue(enchantable.getEnchantmentPolicy().isEnchantable(stage));
        }
    }
}