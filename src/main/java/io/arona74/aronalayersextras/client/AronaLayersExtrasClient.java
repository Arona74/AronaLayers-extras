package io.arona74.aronalayersextras.client;

import io.arona74.aronalayersextras.client.model.PlantLayerModelPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class AronaLayersExtrasClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new PlantLayerModelPlugin());
    }
}
