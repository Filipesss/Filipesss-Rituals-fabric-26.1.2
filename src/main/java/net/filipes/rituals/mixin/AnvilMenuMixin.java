package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.BlightspearItem; // Your new item
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.component.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
// Import your custom enchantment registry class here
// import net.filipes.rituals.enchantment.ModEnchantments;

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

        // Rosegold: block all enchants below stage 2
        if (left.getItem() instanceof RosegoldPickaxeItem
                && RosegoldPickaxeItem.getStage(left) < 2) {
            self.getSlot(2).set(ItemStack.EMPTY);
            return;
        }

        // Shadowguard: enforce per-stage enchant gates
        if (left.getItem() instanceof ShadowguardItem) {
            int stage = ModDataComponents.getStage(left);
            ItemStack result = self.getSlot(2).getItem();
            if (result.isEmpty()) return;

            ItemEnchantments enchantments = result.getEnchantments();
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                boolean forbidden = false;

                // Wind Burst requires stage 3
                if (enchantment.is(Enchantments.WIND_BURST) && stage < 3) forbidden = true;
                // Density and Breach require stage 5
                if (enchantment.is(Enchantments.DENSITY) && stage < 5) forbidden = true;
                if (enchantment.is(Enchantments.BREACH) && stage < 5) forbidden = true;

                if (forbidden) {
                    self.getSlot(2).set(ItemStack.EMPTY);
                    return;
                }
            }
        }

        // Blightspear: Enchants allowed from Stage 1, but LUNGE is gated behind Stage 4+
        if (left.getItem() instanceof BlightspearItem) {
            int stage = ModDataComponents.getStage(left);
            ItemStack result = self.getSlot(2).getItem();
            if (result.isEmpty()) return;

            ItemEnchantments enchantments = result.getEnchantments();
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                // Change 'ModEnchantments.LUNGE' to match your actual custom registry reference
                if (enchantment.is(Enchantments.LUNGE) && stage < 4) {
                    self.getSlot(2).set(ItemStack.EMPTY);
                    return;
                }
            }
        }
    }
}