package io.arona74.crlayersextras;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRLayersExtras implements ModInitializer {
    public static final String MOD_ID = "crlayers-extras";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing CRLayers Extras - Making Conquest Reforged layers behave naturally!");

        // Load config
        ModConfig.load();

        // Register event handlers
        GrassSpreadHandler.register();
        MyceliumSpreadHandler.register();
        SheepGrassEatingHandler.register();
        LayerFallHandler.register();
    }
}
