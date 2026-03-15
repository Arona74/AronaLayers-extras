package io.arona74.aronalayersextras;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SheepGrassEatingHandler {
    private static final Identifier GRASS_LAYER_ID = new Identifier("conquest", "grass_block_layer");
    private static final Identifier LOAMY_DIRT_SLAB_ID = new Identifier("conquest", "loamy_dirt_slab");
    private static final Identifier VLP_GRASS_LAYER_ID = new Identifier("vanillalayerplus", "grass_layer");
    private static final Identifier VLP_DIRT_LAYER_ID = new Identifier("vanillalayerplus", "dirt_layer");

    public static void register() {
        // We'll use a mixin instead for better integration
        AronaLayersExtras.LOGGER.info("Registered sheep grass eating handler");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState copyPropertiesPublic(BlockState source, BlockState target) {
        try {
            for (var property : source.getProperties()) {
                if (target.contains(property)) {
                    target = target.with((net.minecraft.state.property.Property<T>) property,
                                        (T) source.get(property));
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
     * Called from the SheepEntity mixin when a sheep eats grass
     * Returns true if we handled the eating, false otherwise
     */
    public static boolean tryEatGrassLayer(SheepEntity sheep) {
        World world = sheep.getWorld();
        BlockPos pos = sheep.getBlockPos();

        // Check the block at sheep's position
        BlockState state = world.getBlockState(pos);

        Identifier blockId = Registries.BLOCK.getId(state.getBlock());

        if (blockId.equals(GRASS_LAYER_ID)) {
            world.setBlockState(pos, copyProperties(state, Registries.BLOCK.get(LOAMY_DIRT_SLAB_ID).getDefaultState()), 2);
            return true;
        }
        if (blockId.equals(VLP_GRASS_LAYER_ID)) {
            world.setBlockState(pos, copyProperties(state, Registries.BLOCK.get(VLP_DIRT_LAYER_ID).getDefaultState()), 2);
            return true;
        }

        return false;
    }
}
