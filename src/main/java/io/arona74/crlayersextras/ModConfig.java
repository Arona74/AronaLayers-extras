package io.arona74.crlayersextras;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static ModConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "crlayers-extras.json";

    public boolean enableGrassSpreading = true;
    public boolean enableMyceliumSpreading = true;
    public boolean enableSheepEatingGrassLayers = true;
    public boolean preventGrassDecay = true;
    public boolean enableLayersFallWithSand = true;
    public boolean enableBlockOffset = true;
    // If true, vanilla plants (flowers, grass, saplings, etc.) also receive the Y-offset treatment.
    // If false, only blocks listed in AdditionalOffsetBlocks get the offset.
    // Requires resource reload (F3+T) to take effect.
    public boolean VanillaBlockOffset = true;
    // Extra plant/decoration block IDs from other mods (e.g. Conquest Reforged) that should
    // also receive the Y-offset treatment, in addition to the built-in vanilla plant list.
    // Example: "conquest:seagrass", "conquest:tall_seagrass"
    public List<String> AdditionalOffsetBlocks = new ArrayList<>(List.of(
            "conquest:seagrass",
            "conquest:tall_seagrass",
            "minecraft:pink_petals"
    ));

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                INSTANCE.save();
                return INSTANCE;
            } catch (Exception e) {
                CRLayersExtras.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        INSTANCE = new ModConfig();
        INSTANCE.save();
        return INSTANCE;
    }

    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            CRLayersExtras.LOGGER.error("Failed to save config", e);
        }
    }
}
