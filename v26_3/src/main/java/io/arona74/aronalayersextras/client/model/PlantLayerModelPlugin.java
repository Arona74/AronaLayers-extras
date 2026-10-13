package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.world.level.block.VegetationBlock;

import java.util.Set;

public class PlantLayerModelPlugin implements ModelLoadingPlugin {

    @Override
    public void initialize(Context ctx) {
        Set<String> additional = Set.copyOf(ModConfig.getInstance().AdditionalOffsetBlocks);

        ctx.modifyBlockModelAfterBake().register((model, context) -> {
            if (model == null) return null;

            String blockId = Compat.blockId(context.state().getBlock());

            // Match vanilla plants on the block's type rather than on a
            // hand-maintained id list. The older modules carry such a list, and
            // by 1.21.11 it had silently rotted: bush, firefly_bush,
            // leaf_litter, wildflowers, short_dry_grass, tall_dry_grass,
            // cactus_flower, pale_oak_sapling, pale_hanging_moss and
            // pale_moss_carpet all render without the offset and float above
            // layer blocks. A list cannot be kept correct across versions; the
            // type can.
            //
            // VegetationBlock, NOT BushBlock. 1.21.11 renamed the old
            // BushBlock to VegetationBlock and then reused the name BushBlock
            // for a narrow subclass of it -- matching BushBlock compiles and
            // silently catches only a fraction of plants, minecraft:bush among
            // them but almost nothing else.
            //
            // The namespace check is load-bearing and must not be dropped: this
            // is the VanillaBlockOffset toggle, and modded plants belong to
            // AdditionalOffsetBlocks. Conquest Reforged's plants extend
            // VegetationBlock too, but already place themselves on layer blocks
            // via their own 'layers' blockstate property, so wrapping them
            // applies the shift a second time and sinks them into the block
            // below. The offset being self-gating (computeOffset returns 0
            // unless the block below has a partial-height shape) does not help
            // here: the block below is exactly the case that gates it open.
            if (ModConfig.getInstance().VanillaBlockOffset
                    && blockId.startsWith("minecraft:")
                    && context.state().getBlock() instanceof VegetationBlock) {
                return new LayerAwareBakedModel(model);
            }
            if (additional.contains(blockId)) {
                return new LayerAwareBakedModel(model);
            }
            return model;
        });
    }
}
