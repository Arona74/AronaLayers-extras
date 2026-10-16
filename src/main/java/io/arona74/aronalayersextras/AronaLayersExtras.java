package io.arona74.aronalayersextras;

import io.arona74.aronalayersextras.ore.SmallOreItems;
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

        // Items are synced to clients, so they are registered whatever the config says.
        SmallOreItems.register();

        // Register event handlers
        GrassSpreadHandler.register();
        MyceliumSpreadHandler.register();
        SheepGrassEatingHandler.register();
        LayerFallHandler.register();
        WetSandHandler.register();
    }
}
