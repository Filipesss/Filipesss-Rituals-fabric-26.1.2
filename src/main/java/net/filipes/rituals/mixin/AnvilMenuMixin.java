package net.filipes.rituals.mixin;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("TAIL"))
    private void rituals$gateAnvilEnchanting(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu)(Object)this;

        ItemStack left = self.getSlot(0).getItem();
        if (left.isEmpty() || !(left.getItem() instanceof RitualsEnchantable enchantable)) return;

        ItemStack result = self.getSlot(2).getItem();
        if (result.isEmpty()) return;

        int stage = ModDataComponents.getStage(left);
        EnchantmentPolicy policy = enchantable.getEnchantmentPolicy();

        ItemEnchantments leftEnchants   = left.getEnchantments();
        ItemEnchantments resultEnchants = result.getEnchantments();

        for (Holder<Enchantment> ench : resultEnchants.keySet()) {
            if (leftEnchants.getLevel(ench) > 0) continue;

            if (!policy.isAllowed(ench, stage)) {
                self.getSlot(2).set(ItemStack.EMPTY);
                return;
            }
        }
    }
}