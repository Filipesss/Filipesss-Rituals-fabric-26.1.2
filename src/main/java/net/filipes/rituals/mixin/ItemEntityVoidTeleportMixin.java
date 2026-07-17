package net.filipes.rituals.mixin;

import net.filipes.rituals.item.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class ItemEntityVoidTeleportMixin {

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    private void rituals$teleportPersistentDropFromVoid(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;

        if (!(self instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || !stack.is(ModTags.Items.PERSISTENT_DROP)) return;

        if (!(self.level() instanceof ServerLevel serverLevel)) {
            ci.cancel();
            return;
        }

        double x;
        double y;
        double z;

        if (serverLevel.dimension() == Level.END) {
            x = 100.5;
            y = 50.0;
            z = 0.5;
        } else {
            // Covers Overworld and Nether alike
            x = 0.5;
            z = 0.5;
            y = rituals$findSafeSurfaceY(serverLevel, 0, 0) + 0.0;
        }

        self.setDeltaMovement(0, 0, 0);
        self.fallDistance = 0.0F;
        self.teleportTo(x, y, z);

        ci.cancel();
    }

    private static int rituals$findSafeSurfaceY(ServerLevel level, int x, int z) {
        int top = level.getMaxY();
        int bottom = level.getMinY();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        for (int y = top; y > bottom; y--) {
            pos.setY(y);
            if (!level.getBlockState(pos).isAir()) {
                boolean spaceAbove = level.getBlockState(pos.above()).isAir()
                        && level.getBlockState(pos.above(2)).isAir();
                if (spaceAbove) {
                    return y + 1;
                }
            }
        }

        // Fallback: nothing safe found (shouldn't normally happen), just use build height top
        return top;
    }
}