package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;

import java.util.Set;

public class PlantLayerModelPlugin implements ModelLoadingPlugin {

    private static final Set<String> TARGET_PLANTS = Set.of(
            "short_grass", "tall_grass", "fern", "large_fern", "dead_bush",
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
        Set<String> additional = Set.copyOf(ModConfig.getInstance().AdditionalOffsetBlocks);

        ctx.modifyModelAfterBake().register((original, context) -> {
            if (original == null) return null;
            ModelIdentifier topId = context.topLevelId();
            if (topId != null) {
                if (ModConfig.getInstance().VanillaBlockOffset
                        && "minecraft".equals(Compat.modelIdNamespace(topId))
                        && TARGET_PLANTS.contains(Compat.modelIdPath(topId))) {
                    return new LayerAwareBakedModel(original);
                }
                if (additional.contains(Compat.modelIdNamespace(topId) + ":" + Compat.modelIdPath(topId))) {
                    return new LayerAwareBakedModel(original);
                }
            }
            return original;
        });
    }
}
