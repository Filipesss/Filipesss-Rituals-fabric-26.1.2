package net.filipes.rituals.mixin;

import net.filipes.rituals.item.ModTags;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackGlintMixin {

    @Inject(method = "hasFoil", at = @At("HEAD"), cancellable = true)
    private void rituals$suppressGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.is(ModTags.Items.PERSISTENT_DROP)) {
            cir.setReturnValue(false);
        }
    }
}