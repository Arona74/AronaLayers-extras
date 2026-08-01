package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import java.util.Set;

public class PlantLayerModelPlugin implements ModelLoadingPlugin {

    /**
     * Canonical block ids rather than the bare model paths the older modules
     * match on. 1.21.11's after-bake hook hands us the BlockState directly
     * instead of a model identifier, so the block id is both available and a
     * more precise thing to match.
     */
    private static final Set<String> TARGET_PLANTS = Set.of(
            "minecraft:short_grass", "minecraft:tall_grass", "minecraft:fern",
            "minecraft:large_fern", "minecraft:dead_bush",
            "minecraft:dandelion", "minecraft:poppy", "minecraft:blue_orchid",
            "minecraft:allium", "minecraft:azure_bluet",
            "minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip",
            "minecraft:pink_tulip", "minecraft:oxeye_daisy", "minecraft:cornflower",
            "minecraft:lily_of_the_valley", "minecraft:wither_rose",
            "minecraft:torchflower", "minecraft:pitcher_plant",
            "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony",
            "minecraft:oak_sapling", "minecraft:spruce_sapling", "minecraft:birch_sapling",
            "minecraft:jungle_sapling", "minecraft:acacia_sapling", "minecraft:dark_oak_sapling",
            "minecraft:cherry_sapling", "minecraft:mangrove_propagule",
            "minecraft:seagrass", "minecraft:tall_seagrass",
            "minecraft:brown_mushroom", "minecraft:red_mushroom"
    );

    @Override
    public void initialize(Context ctx) {
        Set<String> additional = Set.copyOf(ModConfig.getInstance().AdditionalOffsetBlocks);

        ctx.modifyBlockModelAfterBake().register((model, context) -> {
            if (model == null) return null;
            String id = Compat.blockId(context.state().getBlock());
            if (ModConfig.getInstance().VanillaBlockOffset && TARGET_PLANTS.contains(id)) {
                return new LayerAwareBakedModel(model);
            }
            if (additional.contains(id)) {
                return new LayerAwareBakedModel(model);
            }
            return model;
        });
    }
}
