package net.filipes.rituals.item.custom;


import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class RosegoldPickaxeItem extends Item implements RitualsTooltipStyle {

    public RosegoldPickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        // apply the pickaxe properties to the settings and pass the resulting Settings to super
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }
    public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initialBlockPos, ServerPlayerEntity player) {
        List<BlockPos> positions = new ArrayList<>();
        HitResult hit = player.raycast(20, 0, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return positions;
        }

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    positions.add(new BlockPos(
                            initialBlockPos.getX() + x,
                            initialBlockPos.getY() + y,
                            initialBlockPos.getZ() + z
                    ));
                }
            }
        }
        return positions;
    }
    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey())
                .styled(s -> s.withColor(getNameColor()).withItalic(false));
    }
    @Override public int getNameColor()              { return 0xFFFFB6C1; } // rose pink
    @Override public int getTooltipBorderColor()     { return 0xFFFF80AA; } // bright rose border
    @Override public int getTooltipBackgroundColor() { return 0xE51A0510; } // dark red, slightly transparent (E5 = ~90% opaque)
}