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

/**
 * Intercepts the anvil result slot and removes it if any *newly added*
 * enchantment violates the item's {@link EnchantmentPolicy}.
 *
 * "Newly added" = present on the result but absent from the left input.
 * Existing enchantments (renames, repairs, same-enchant level-ups) are untouched.
 *
 * NOTE: We deliberately avoid Holder.getKey() / Holder.unwrapKey() here because
 * the Holder API changed in 26.1.  Instead we use ItemEnchantments.getLevel(),
 * which returns 0 when the enchantment is absent — no key lookup needed.
 */
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
            // getLevel() returns 0 if the enchantment is absent on the left item.
            // If it returns > 0, the enchantment was already there — skip the check
            // so that renames and same-enchantment upgrades still work fine.
            if (leftEnchants.getLevel(ench) > 0) continue;

            // Newly introduced enchantment — check against the policy.
            if (!policy.isAllowed(ench, stage)) {
                self.getSlot(2).set(ItemStack.EMPTY);
                return;
            }
        }
    }
}