package net.filipes.rituals.pedestal;

import java.util.Map;

public class PedestalTextHeights {

    private static final float DEFAULT_HEIGHT = 1.8f;

    private static final Map<String, Float> HEIGHTS = Map.ofEntries(
            Map.entry("rosegold_pickaxe_pedestal", 2.2f),
            Map.entry("rosegold_helmet_pedestal",  2.2f),
            Map.entry("rosegold_chestplate_pedestal",  2.2f),
            Map.entry("rosegold_leggings_pedestal",  2.2f),
            Map.entry("rosegold_boots_pedestal",  2.2f),
            Map.entry("lunar_blade_pedestal",  4.0f),
            Map.entry("solar_blade_pedestal",  4.0f),
            Map.entry("lightning_rapier_pedestal",  3.5f),
            Map.entry("vortex_edge_pedestal",  3.75f),
            Map.entry("polarity_bow_pedestal",  3.0f),
            Map.entry("cinderbolt_pedestal",  2.4f),
            Map.entry("shadowguard_pedestal",  3.0f),
            Map.entry("shadeshatter_pedestal",  4.0f),
            Map.entry("temporal_glassreaver_pedestal",  4.0f),
            Map.entry("blightspear_pedestal",  4.0f),
            Map.entry("depthstrike_pedestal",  3.75f),
            Map.entry("pulse_blaster_pedestal",  3.0f),
            Map.entry("pharathorn_pedestal",  4.0f)

    );

    public static float get(String pedestalTypeId) {
        return HEIGHTS.getOrDefault(pedestalTypeId, DEFAULT_HEIGHT);
    }
}