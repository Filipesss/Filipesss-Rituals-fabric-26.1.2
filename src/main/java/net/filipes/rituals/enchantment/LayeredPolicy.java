package net.filipes.rituals.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An {@link EnchantmentPolicy} that changes behaviour depending on the item's
 * upgrade stage. Use {@link EnchantmentPolicy#layered()} to obtain a builder.
 *
 * <h3>Layer semantics</h3>
 * <table border="1">
 *   <tr><th>Builder call</th><th>isEnchantable</th><th>isAllowed</th><th>Typical use</th></tr>
 *   <tr><td>{@code .deny()}</td>
 *       <td>false</td><td>false for all</td>
 *       <td>Completely block enchanting (table + anvil)</td></tr>
 *   <tr><td>{@code .allow(WIND_BURST)}</td>
 *       <td>false (table blocked)</td><td>true only for listed keys</td>
 *       <td>Anvil-book-only whitelist (table stays locked)</td></tr>
 *   <tr><td>{@code .allowAll()}</td>
 *       <td>true</td><td>true for everything</td>
 *       <td>Full enchanting unlocked (still gated by vanilla item tags)</td></tr>
 * </table>
 */
public final class LayeredPolicy extends EnchantmentPolicy {

    // ─── Internal layer record ────────────────────────────────────────────

    /**
     * @param enchantable whether the enchanting TABLE may use this item
     * @param whitelist   {@code null} → all enchantments pass;
     *                    empty set   → no enchantments pass (deny);
     *                    non-empty   → only listed keys pass
     */
    private record Layer(
            int min,
            int max,
            boolean enchantable,
            @Nullable Set<ResourceKey<Enchantment>> whitelist
    ) {
        boolean covers(int stage) {
            return stage >= min && stage <= max;
        }

        /** Returns true if this enchantment is permitted by this layer's whitelist. */
        boolean permits(Holder<Enchantment> e) {
            if (whitelist == null) return true;         // null → allow all
            return whitelist.stream().anyMatch(e::is);  // empty → none; else check
        }
    }

    // ─── Policy ───────────────────────────────────────────────────────────

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

    // ─── Builder ──────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Layer> layers = new ArrayList<>();

        /**
         * Define rules for stages {@code from} through {@code to} (inclusive).
         * Pass {@code Integer.MAX_VALUE} as {@code to} to mean "this stage and beyond".
         *
         * <pre>{@code
         * .stage(1, 2).deny()
         * .stage(3, 5).allow(Enchantments.WIND_BURST)
         * .stage(6, Integer.MAX_VALUE).allowAll()
         * }</pre>
         */
        public LayerDef stage(int from, int to) {
            return new LayerDef(from, to);
        }

        /** Finalises the policy. */
        public LayeredPolicy build() {
            return new LayeredPolicy(layers);
        }

        // ── Layer definition ──────────────────────────────────────────────

        public final class LayerDef {
            private final int from, to;

            LayerDef(int from, int to) {
                this.from = from;
                this.to = to;
            }

            /**
             * Block ALL enchanting in this stage range – neither table nor anvil will work.
             */
            public Builder deny() {
                // enchantable=false, empty whitelist → permits() always returns false
                layers.add(new Layer(from, to, false, Set.of()));
                return Builder.this;
            }

            /**
             * Allow ONLY the listed enchantments in this stage range, exclusively via
             * the anvil (enchanted book). The enchanting table remains locked.
             */
            @SafeVarargs
            public final Builder allow(ResourceKey<Enchantment>... keys) {
                // enchantable=false → table locked; non-empty whitelist → anvil-only
                layers.add(new Layer(from, to, false, Set.of(keys)));
                return Builder.this;
            }

            /**
             * Allow all enchantments in this stage range (enchanting table + anvil),
             * still subject to vanilla item-tag restrictions.
             */
            public Builder allowAll() {
                // enchantable=true, null whitelist → permits() always returns true
                layers.add(new Layer(from, to, true, null));
                return Builder.this;
            }
        }
    }
}