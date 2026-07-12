package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.ShadeshatterAnimTracker;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

    @Unique
    private static Level rituals$capturedLevel;
    @Unique
    private static ItemOwner rituals$capturedOwner;

    @Inject(method = "updateForTopItem", at = @At("HEAD"))
    private void rituals$captureContext(ItemStackRenderState output, ItemStack item,
                                        ItemDisplayContext displayContext, Level level,
                                        ItemOwner owner, int seed, CallbackInfo ci) {
        rituals$capturedLevel = level;
        rituals$capturedOwner = owner;
    }

    @ModifyVariable(
            method = "updateForTopItem",
            at = @At("HEAD")
    )
    private ItemStack rituals$injectAnimatedFrame(ItemStack item) {
        if (item.isEmpty()) return item;

        boolean isShadeshatter = item.is(ModItems.SHADESHATTER);
        boolean isVortex       = item.is(ModItems.VORTEX_EDGE);
        if (!isShadeshatter && !isVortex) return item;

        Level level = rituals$capturedLevel;
        if (level == null) return item;

        long globalTick = level.getGameTime();

        float frameValue;
        if (isShadeshatter) {
            // Only LivingEntity owners have a stable id we track animation
            // state by. Non-living owners (e.g. item frames) just get the
            // idle-loop frame with entity id 0.
            int entityId = rituals$capturedOwner instanceof LivingEntity living ? living.getId() : 0;
            frameValue = ShadeshatterAnimTracker.computeFrame(entityId, globalTick);
        } else {
            frameValue = (float) ((globalTick / 4) % 9);
        }

        ItemStack rendered = item.copy();
        rendered.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(frameValue), List.of(), List.of(), List.of())
        );
        return rendered;
    }
}