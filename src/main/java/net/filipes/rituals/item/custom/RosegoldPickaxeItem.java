package net.filipes.rituals.item.custom;

import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.enchantment.EnchantmentPolicy;
import net.filipes.rituals.enchantment.RitualsEnchantable;
import net.filipes.rituals.item.ModToolMaterials;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class RosegoldPickaxeItem extends Item implements RitualsTooltipStyle, RitualsEnchantable {

    public enum MiningMode { NONE, FLAT_3X3, CUBE_3X3X3 }
    private static final EnchantmentPolicy POLICY =
            EnchantmentPolicy.layered()
                    .stage(1, Integer.MAX_VALUE).allow(Enchantments.EFFICIENCY)
                    .stage(2, Integer.MAX_VALUE).allow(Enchantments.SILK_TOUCH)
                    .stage(2, Integer.MAX_VALUE).allow(Enchantments.FORTUNE)
                    .build();

    public RosegoldPickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.pickaxe(material, attackDamage, attackSpeed)
                .enchantable(ModToolMaterials.ROSEGOLD.enchantmentValue()));
    }

    public static int getStage(ItemStack stack) {
        return ModDataComponents.getStage(stack);
    }
    public static boolean isMiningEnabled(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MINING_ENABLED, true);
    }
    public static MiningMode getMiningMode(ItemStack stack) {
        if (!isMiningEnabled(stack)) return MiningMode.NONE;
        return switch (getStage(stack)) {
            case 5 -> MiningMode.CUBE_3X3X3;
            case 4 -> MiningMode.FLAT_3X3;
            default -> MiningMode.NONE;
        };
    }

    public static boolean hasDoubleDrops(ItemStack stack) {
        return getStage(stack) >= 3;
    }

    public static List<BlockPos> getBlocksToDestroy(MiningMode mode, BlockPos center, ServerPlayer player) {
        List<BlockPos> result = new ArrayList<>();
        if (mode == MiningMode.NONE) return result;

        HitResult hit = player.pick(20, 0, false);
        if (!(hit instanceof BlockHitResult blockHit)) return result;

        if (mode == MiningMode.FLAT_3X3) {
            Direction.Axis axis = blockHit.getDirection().getAxis();
            for (int u = -1; u <= 1; u++) {
                for (int v = -1; v <= 1; v++) {
                    BlockPos offset = switch (axis) {
                        case X -> center.offset(0, u, v);
                        case Y -> center.offset(u, 0, v);
                        case Z -> center.offset(u, v, 0);
                    };
                    if (!offset.equals(center)) result.add(offset);
                }
            }
        } else {
            for (int x = -1; x <= 1; x++)
                for (int y = -1; y <= 1; y++)
                    for (int z = -1; z <= 1; z++) {
                        BlockPos p = center.offset(x, y, z);
                        if (!p.equals(center)) result.add(p);
                    }
        }
        return result;
    }

    @Override public int getNameColor()                { return 0xFFFFB6C1; }
    @Override public int getTooltipBorderColorTop()    { return 0xFFFF80AA; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF99004D; }
    @Override public int getTooltipBackgroundColor()   { return 0xE5420d29; }

    @Override
    public EnchantmentPolicy getEnchantmentPolicy() {
        return POLICY;
    }
}