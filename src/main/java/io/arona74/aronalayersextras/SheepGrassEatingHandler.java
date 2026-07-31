package io.arona74.aronalayersextras;

import io.arona74.aronalayersextras.Compat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SheepGrassEatingHandler {
    private static final ResourceLocation GRASS_LAYER_ID = Compat.id("conquest", "grass_block_layer");
    private static final ResourceLocation LOAMY_DIRT_SLAB_ID = Compat.id("conquest", "loamy_dirt_slab");
    private static final ResourceLocation VLP_GRASS_LAYER_ID = Compat.id("vanillalayerplus", "grass_layer");
    private static final ResourceLocation VLP_DIRT_LAYER_ID = Compat.id("vanillalayerplus", "dirt_layer");

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
    public static boolean tryEatGrassLayer(Sheep sheep) {
        Level world = sheep.level();
        BlockPos pos = sheep.blockPosition();

        // Check the block at sheep's position
        BlockState state = world.getBlockState(pos);

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (blockId.equals(GRASS_LAYER_ID)) {
            world.setBlock(pos, copyProperties(state, BuiltInRegistries.BLOCK.get(LOAMY_DIRT_SLAB_ID).defaultBlockState()), 2);
            return true;
        }
        if (blockId.equals(VLP_GRASS_LAYER_ID)) {
            world.setBlock(pos, copyProperties(state, BuiltInRegistries.BLOCK.get(VLP_DIRT_LAYER_ID).defaultBlockState()), 2);
            return true;
        }

        return false;
    }
}
