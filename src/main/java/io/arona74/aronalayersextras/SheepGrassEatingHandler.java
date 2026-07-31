package io.arona74.aronalayersextras;

import io.arona74.aronalayersextras.Compat;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SheepGrassEatingHandler {
    private static final String GRASS_LAYER_ID = "conquest:grass_block_layer";
    private static final String LOAMY_DIRT_SLAB_ID = "conquest:loamy_dirt_slab";
    private static final String VLP_GRASS_LAYER_ID = "vanillalayerplus:grass_layer";
    private static final String VLP_DIRT_LAYER_ID = "vanillalayerplus:dirt_layer";

    public static void register() {
        // We'll use a mixin instead for better integration
        AronaLayersExtras.LOGGER.info("Registered sheep grass eating handler");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState copyPropertiesPublic(BlockState source, BlockState target) {
        try {
            for (var property : source.getProperties()) {
                if (target.hasProperty(property)) {
                    target = target.setValue((net.minecraft.world.level.block.state.properties.Property<T>) property,
                                        (T) source.getValue(property));
                }
            }
        } catch (Exception e) {
            // If property copying fails, just return the target state as-is
        }
        return target;
    }

    private static <T extends Comparable<T>> BlockState copyProperties(BlockState source, BlockState target) {
        return copyPropertiesPublic(source, target);
    }

    /**
     * Called from the Sheep mixin when a sheep eats grass
     * Returns true if we handled the eating, false otherwise
     */
    /**
     * Takes the level and position rather than the sheep: 1.21.11 moved Sheep
     * to net.minecraft.world.entity.animal.sheep, and shared code cannot name
     * a type whose package differs per version.
     */
    public static boolean tryEatGrassLayer(Level world, BlockPos pos) {
        // Check the block at the sheep's position
        BlockState state = world.getBlockState(pos);

        String blockId = Compat.blockId(state.getBlock());

        if (blockId.equals(GRASS_LAYER_ID)) {
            world.setBlock(pos, copyProperties(state, Compat.blockFromId(LOAMY_DIRT_SLAB_ID).defaultBlockState()), 2);
            return true;
        }
        if (blockId.equals(VLP_GRASS_LAYER_ID)) {
            world.setBlock(pos, copyProperties(state, Compat.blockFromId(VLP_DIRT_LAYER_ID).defaultBlockState()), 2);
            return true;
        }

        return false;
    }
}
