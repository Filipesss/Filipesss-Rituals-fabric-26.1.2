package net.filipes.rituals.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LayeredPolicy extends EnchantmentPolicy {

    private record Layer(
            int min,
            int max,
            boolean enchantable,
            @Nullable Set<ResourceKey<Enchantment>> whitelist
    ) {
        boolean covers(int stage) {
            return stage >= min && stage <= max;
        }

        boolean permits(Holder<Enchantment> e) {
            if (whitelist == null) return true;
            return whitelist.stream().anyMatch(e::is);
        }
    }


    private final List<Layer> layers;

    LayeredPolicy(List<Layer> layers) {
        this.layers = List.copyOf(layers);
    }

    private @Nullable Layer layerFor(int stage) {
        for (Layer l : layers) {
            if (l.covers(stage)) return l;
        }
        return null;
    }

    @Override
    public boolean isEnchantable(int stage) {
        Layer l = layerFor(stage);
        return l != null && l.enchantable();
    }

    @Override
    public boolean isAllowed(Holder<Enchantment> enchantment, int stage) {
        Layer l = layerFor(stage);
        if (l == null) return false;
        return l.permits(enchantment);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Layer> layers = new ArrayList<>();

        public LayerDef stage(int from, int to) {
            return new LayerDef(from, to);
        }

        public LayeredPolicy build() {
            return new LayeredPolicy(layers);
        }

        public final class LayerDef {
            private final int from, to;

            LayerDef(int from, int to) {
                this.from = from;
                this.to = to;
            }

            public Builder deny() {
                layers.add(new Layer(from, to, false, Set.of()));
                return Builder.this;
            }

            @SafeVarargs
            public final Builder allow(ResourceKey<Enchantment>... keys) {
                layers.add(new Layer(from, to, false, Set.of(keys)));
                return Builder.this;
            }

            public Builder allowAll() {
                layers.add(new Layer(from, to, true, null));
                return Builder.this;
            }
        }
    }
}