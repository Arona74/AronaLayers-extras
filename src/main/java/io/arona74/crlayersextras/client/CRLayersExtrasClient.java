package io.arona74.crlayersextras.client;

import io.arona74.crlayersextras.client.model.PlantLayerModelPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class CRLayersExtrasClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new PlantLayerModelPlugin());
    }
}
