package net.filipes.rituals.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*; import java.nio.file.*;

public class RitualConfig {

    public static int PEDESTAL_SPAWN_RADIUS = 5000;


    public static float MAX_MACE_DAMAGE = 12.0f;
    public static boolean MACE_DAMAGE_CAP_ENABLED = true;
    public static float MAX_TNT_MINECART_DAMAGE = 14.0f;
    public static boolean TNT_MINECART_DAMAGE_CAP_ENABLED = true;

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("rituals.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
            if (obj.has("pedestal_spawn_radius"))
                PEDESTAL_SPAWN_RADIUS = obj.get("pedestal_spawn_radius").getAsInt();
            if (obj.has("mace_damage_cap_enabled"))
                MACE_DAMAGE_CAP_ENABLED = obj.get("mace_damage_cap_enabled").getAsBoolean();
            if (obj.has("max_mace_damage"))
                MAX_MACE_DAMAGE = obj.get("max_mace_damage").getAsFloat();
            if (obj.has("tnt_minecart_damage_cap_enabled"))
                TNT_MINECART_DAMAGE_CAP_ENABLED = obj.get("tnt_minecart_damage_cap_enabled").getAsBoolean();
            if (obj.has("max_tnt_minecart_damage"))
                MAX_TNT_MINECART_DAMAGE = obj.get("max_tnt_minecart_damage").getAsFloat();
        } catch (Exception e) {
            System.err.println("[Rituals] Failed to load config: " + e.getMessage());
        }
    }


    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("pedestal_spawn_radius", PEDESTAL_SPAWN_RADIUS);
        obj.addProperty("mace_damage_cap_enabled", MACE_DAMAGE_CAP_ENABLED);
        obj.addProperty("max_mace_damage", MAX_MACE_DAMAGE);
        obj.addProperty("tnt_minecart_damage_cap_enabled", TNT_MINECART_DAMAGE_CAP_ENABLED);
        obj.addProperty("max_tnt_minecart_damage", MAX_TNT_MINECART_DAMAGE);
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(obj, w);
        } catch (Exception e) {
            System.err.println("[Rituals] Failed to save config: " + e.getMessage());
        }
    }
}