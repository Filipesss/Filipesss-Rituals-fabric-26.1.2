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
            boolean deny,
            boolean allowAll,
            Set<ResourceKey<Enchantment>> whitelist
    ) {
        boolean covers(int stage) {
            return stage >= min && stage <= max;
        }
    }

    private final List<Layer> layers;

    LayeredPolicy(List<Layer> layers) {
        this.layers = List.copyOf(layers);
    }

    private List<Layer> layersFor(int stage) {
        List<Layer> matching = new ArrayList<>();
        for (Layer l : layers) {
            if (l.covers(stage)) matching.add(l);
        }
        return matching;
    }

    @Override
    public boolean isEnchantable(int stage) {
        List<Layer> covering = layersFor(stage);
        if (covering.isEmpty()) return false;

        // An explicit deny() layer in range blocks enchanting entirely for that stage,
        // regardless of any other allow()/allowAll() layers overlapping the same range.
        if (covering.stream().anyMatch(Layer::deny)) return false;

        return true;
    }

    @Override
    public boolean isAllowed(Holder<Enchantment> enchantment, int stage) {
        List<Layer> covering = layersFor(stage);
        if (covering.isEmpty()) return false;
        if (covering.stream().anyMatch(Layer::deny)) return false;

        // allowAll() in any covering layer opens the gate completely for this stage.
        if (covering.stream().anyMatch(Layer::allowAll)) return true;

        // Otherwise: union of every covering layer's whitelist.
        return covering.stream()
                .flatMap(l -> l.whitelist().stream())
                .anyMatch(enchantment::is);
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
                layers.add(new Layer(from, to, true, false, Set.of()));
                return Builder.this;
            }

            @SafeVarargs
            public final Builder allow(ResourceKey<Enchantment>... keys) {
                layers.add(new Layer(from, to, false, false, Set.of(keys)));
                return Builder.this;
            }

            public Builder allowAll() {
                layers.add(new Layer(from, to, false, true, Set.of()));
                return Builder.this;
            }
        }
    }
}