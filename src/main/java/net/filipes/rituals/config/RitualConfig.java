package net.filipes.rituals.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;

public class RitualConfig {

    public static int PEDESTAL_SPAWN_RADIUS = 5000;

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("rituals.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
            if (obj.has("pedestal_spawn_radius"))
                PEDESTAL_SPAWN_RADIUS = obj.get("pedestal_spawn_radius").getAsInt();
        } catch (Exception e) {
            System.err.println("[Rituals] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("pedestal_spawn_radius", PEDESTAL_SPAWN_RADIUS);
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(obj, w);
        } catch (Exception e) {
            System.err.println("[Rituals] Failed to save config: " + e.getMessage());
        }
    }
}