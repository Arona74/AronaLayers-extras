package io.arona74.aronalayersextras.client;

import io.arona74.aronalayersextras.AronaLayersExtras;
import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.client.model.PlantLayerModelPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public class AronaLayersExtrasClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        checkRenderer();
        ModelLoadingPlugin.register(new PlantLayerModelPlugin());
    }

    /**
     * Enforces the renderer requirement on the client only.
     *
     * It is not in fabric.mod.json "depends" because the loader applies that to
     * dedicated servers as well, where the renderer mod is never installed. A
     * dedicated server does not run this entrypoint, so it is unaffected.
     */
    private static void checkRenderer() {
        FabricLoader loader = FabricLoader.getInstance();
        VersionPredicate required;
        try {
            required = VersionPredicate.parse(Compat.RENDERER_VERSION);
        } catch (VersionParsingException e) {
            throw new IllegalStateException(e);
        }

        boolean ok = loader.getModContainer(Compat.RENDERER_MOD_ID)
                .map(mod -> required.test(mod.getMetadata().getVersion()))
                .orElse(false);
        if (ok) {
            return;
        }

        String message = "Arona Layers Extras requires " + Compat.RENDERER_NAME
                + " on the client (" + Compat.RENDERER_URL + "). It is not needed on a dedicated server.";
        if (loader.isDevelopmentEnvironment()) {
            AronaLayersExtras.LOGGER.warn("{} Continuing because this is a development environment.", message);
            return;
        }
        throw new IllegalStateException(message);
    }
}
