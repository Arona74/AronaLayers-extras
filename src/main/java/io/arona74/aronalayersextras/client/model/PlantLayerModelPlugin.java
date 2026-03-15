package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;

import java.util.Set;

public class PlantLayerModelPlugin implements ModelLoadingPlugin {

    // Vanilla plant block names that should receive the Y-offset treatment
    private static final Set<String> TARGET_PLANTS = Set.of(
            "grass", "tall_grass", "fern", "large_fern", "dead_bush",
            "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet",
            "red_tulip", "orange_tulip", "white_tulip", "pink_tulip",
            "oxeye_daisy", "cornflower", "lily_of_the_valley", "wither_rose",
            "torchflower", "pitcher_plant",
            "sunflower", "lilac", "rose_bush", "peony",
            "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling",
            "acacia_sapling", "dark_oak_sapling", "cherry_sapling", "mangrove_propagule",
            "seagrass", "tall_seagrass",
            "brown_mushroom", "red_mushroom"
    );

    @Override
    public void onInitializeModelLoader(Context ctx) {
        // Snapshot the additional blocks list at model-load time
        Set<String> additional = Set.copyOf(ModConfig.getInstance().AdditionalOffsetBlocks);

        ctx.modifyModelAfterBake().register((original, context) -> {
            if (original == null) return null;
            if (context.id() instanceof ModelIdentifier id) {
                // Vanilla plants — only if VanillaBlockOffset is enabled
                if (ModConfig.getInstance().VanillaBlockOffset
                        && "minecraft".equals(id.getNamespace())
                        && TARGET_PLANTS.contains(id.getPath())) {
                    return new LayerAwareBakedModel(original);
                }
                // Extra blocks from config (e.g. conquest:seagrass)
                if (additional.contains(id.getNamespace() + ":" + id.getPath())) {
                    return new LayerAwareBakedModel(original);
                }
            }
            return original;
        });
    }
}
