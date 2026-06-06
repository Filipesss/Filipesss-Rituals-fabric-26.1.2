package net.filipes.rituals.mixin;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filters the enchantment list that the enchanting TABLE may offer/apply,
 * according to the item's {@link EnchantmentPolicy}.
 *
 * This runs after vanilla's own tag-based filtering, so vanilla restrictions
 * still apply first — we only narrow it further.
 *
 * ── Target method ─────────────────────────────────────────────────────────
 * Mojang mapping (26.1+):  EnchantmentHelper.getAvailableEnchantmentResults
 * Signature:               (int level, ItemStack stack, Stream<Holder<Enchantment>> possible)
 *
 * If your IDE shows a different name or signature, do the following:
 *   1. Open EnchantmentHelper in IntelliJ (Ctrl+N → type EnchantmentHelper)
 *   2. Find the method that takes an ItemStack and returns List<EnchantmentInstance>
 *   3. Update the `method` string in @Inject below and match the parameter list here.
 *
 * ── Common alternative names ──────────────────────────────────────────────
 *  • getAvailableEnchantmentResults  (1.21.x – 26.1 Mojang mappings)
 *  • selectEnchantment               (some 1.21.x builds / older Mojang names)
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), cancellable = true)
    private static void rituals$filterTableEnchantments(
            int level,
            ItemStack stack,
            Stream<Holder<Enchantment>> possibleEnchantments,
            CallbackInfoReturnable<List<EnchantmentInstance>> cir
    ) {
        if (!(stack.getItem() instanceof RitualsEnchantable enchantable)) return;

        int stage = ModDataComponents.getStage(stack);
        EnchantmentPolicy policy = enchantable.getEnchantmentPolicy();

        List<EnchantmentInstance> filtered = cir.getReturnValue().stream()
                .filter(ei -> policy.isAllowed(ei.enchantment(), stage))
                .collect(Collectors.toList());

        cir.setReturnValue(filtered);
    }
}