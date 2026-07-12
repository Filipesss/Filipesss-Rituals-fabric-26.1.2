package net.filipes.rituals.mixin;

import net.filipes.rituals.item.ModItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Item.class)
public class BundleExclusionMixin {

    private static final Set<Item> RITUALS_BUNDLE_EXCLUDED = Set.of(
            ModItems.LIGHTNING_RAPIER,
            ModItems.SOLAR_BLADE,
            ModItems.LUNAR_BLADE,
            ModItems.VORTEX_EDGE,
            ModItems.PULSE_BLASTER,
            ModItems.SHADOWGUARD,
            ModItems.BLIGHTSPEAR,
            ModItems.POLARITY_BOW,
            ModItems.CINDERBOLT,
            ModItems.DEPTHSTRIKE,
            ModItems.PHARATHORN,
            ModItems.TEMPORAL_GLASSREAVER,
            ModItems.ROSEGOLD_PICKAXE,
            ModItems.ROSEGOLD_HELMET,
            ModItems.ROSEGOLD_CHESTPLATE,
            ModItems.ROSEGOLD_LEGGINGS,
            ModItems.ROSEGOLD_BOOTS,
            ModItems.SHADESHATTER
    );

    @Inject(method = "canFitInsideContainerItems", at = @At("HEAD"), cancellable = true)
    private void rituals$excludeFromBundles(CallbackInfoReturnable<Boolean> cir) {
        Item self = (Item) (Object) this;
        if (RITUALS_BUNDLE_EXCLUDED.contains(self)) {
            cir.setReturnValue(false);
        }
    }
}