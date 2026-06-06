package net.filipes.rituals.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Set;

public abstract class EnchantmentPolicy {


    public abstract boolean isEnchantable(int stage);

    public abstract boolean isAllowed(Holder<Enchantment> enchantment, int stage);


    public static EnchantmentPolicy normal() {
        return Normal.INSTANCE;
    }


    @SafeVarargs
    public static EnchantmentPolicy restricted(ResourceKey<Enchantment>... blocked) {
        return new Restricted(Set.of(blocked));
    }

    public static LayeredPolicy.Builder layered() {
        return LayeredPolicy.builder();
    }


    private static final class Normal extends EnchantmentPolicy {
        static final Normal INSTANCE = new Normal();

        @Override
        public boolean isEnchantable(int stage) { return true; }

        @Override
        public boolean isAllowed(Holder<Enchantment> e, int stage) { return true; }
    }

    private static final class Restricted extends EnchantmentPolicy {
        private final Set<ResourceKey<Enchantment>> blocked;

        Restricted(Set<ResourceKey<Enchantment>> blocked) { this.blocked = blocked; }

        @Override
        public boolean isEnchantable(int stage) { return true; }

        @Override
        public boolean isAllowed(Holder<Enchantment> e, int stage) {

            return blocked.stream().noneMatch(e::is);
        }
    }
}