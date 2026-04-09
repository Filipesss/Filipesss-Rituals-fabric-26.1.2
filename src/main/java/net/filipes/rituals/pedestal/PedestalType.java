package net.filipes.rituals.pedestal;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record PedestalType(
        String id,
        Map<Item, Integer> requirements,
        Function<RegistryAccess, ItemStack> rewardSupplier
) {
    public ItemStack createReward(RegistryAccess access) {
        return rewardSupplier.apply(access);
    }

    /** Returns true if the given item list satisfies all requirements. */
    public boolean isSatisfied(List<ItemStack> items) {
        for (Map.Entry<Item, Integer> entry : requirements.entrySet()) {
            int found = 0;
            for (ItemStack stack : items)
                if (!stack.isEmpty() && stack.is(entry.getKey()))
                    found += stack.getCount();
            if (found < entry.getValue()) return false;
        }
        return true;
    }

    public boolean isRequiredItem(Item item) {
        return requirements.containsKey(item);
    }
}