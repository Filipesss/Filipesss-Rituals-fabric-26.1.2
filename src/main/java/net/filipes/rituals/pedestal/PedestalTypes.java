package net.filipes.rituals.pedestal;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PedestalTypes {

    private static final Map<String, PedestalType> REGISTRY_INTERNAL = new LinkedHashMap<>();
    public  static final Map<String, PedestalType> REGISTRY =
            Collections.unmodifiableMap(REGISTRY_INTERNAL);

    public static final PedestalType ROSEGOLD_PICKAXE = register(new PedestalType(
            "rosegold_pickaxe",
            Map.of(
                    Items.GOLD_INGOT,     8,
                    Items.DIAMOND,        3,
                    Items.AMETHYST_SHARD, 4
            ),
            access -> {

                ItemStack stack = new ItemStack(net.filipes.rituals.item.ModItems.ROSEGOLD_PICKAXE);

                Registry<Enchantment> enchReg = access.lookupOrThrow(Registries.ENCHANTMENT);
                ItemEnchantments.Mutable ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                ench.set(enchReg.getOrThrow(Enchantments.EFFICIENCY), 5);
                stack.set(DataComponents.ENCHANTMENTS, ench.toImmutable());

                return stack;
            }
    ));

    private static PedestalType register(PedestalType type) {
        REGISTRY_INTERNAL.put(type.id(), type);
        return type;
    }

    public static @Nullable PedestalType byId(String id) {
        return REGISTRY_INTERNAL.get(id);
    }
}