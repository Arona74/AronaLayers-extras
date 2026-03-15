package io.arona74.aronalayersextras;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AronaLayersExtras implements ModInitializer {
    public static final String MOD_ID = "aronalayersextras";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Arona Layers Extras - Making Conquest Reforged or Vanilla Plus layers behave naturally!");

        // Load config
        ModConfig.load();

        // Register event handlers
        GrassSpreadHandler.register();
        MyceliumSpreadHandler.register();
        SheepGrassEatingHandler.register();
        LayerFallHandler.register();
    }
}
