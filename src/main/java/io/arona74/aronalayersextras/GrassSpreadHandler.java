package io.arona74.aronalayersextras;

import io.arona74.aronalayersextras.Compat;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Random;

public class GrassSpreadHandler {
    private static final String GRASS_LAYER_ID = "conquest:grass_block_layer";
    private static final String LOAMY_DIRT_SLAB_ID = "conquest:loamy_dirt_slab";
    private static final String VLP_GRASS_LAYER_ID = "vanillalayerplus:grass_layer";
    private static final String VLP_DIRT_LAYER_ID = "vanillalayerplus:dirt_layer";

    private static final Random RANDOM = new Random();

    public static void register() {
        Compat.onEndLevelTick(GrassSpreadHandler::onWorldTick);
        AronaLayersExtras.LOGGER.info("Registered grass spreading handler");
    }

    private static void onWorldTick(ServerLevel world) {
        if (!ModConfig.getInstance().enableGrassSpreading) return;

        // Get the randomTickSpeed value (default is 3)
        int randomTickSpeed = Compat.randomTickSpeed(world);

        if (randomTickSpeed <= 0) {
            return;
        }

        // Process chunks around players - much more aggressively than before
        world.players().forEach(player -> {
            BlockPos playerPos = player.blockPosition();
            int chunkX = playerPos.getX() >> 4;
            int chunkZ = playerPos.getZ() >> 4;

            // Process chunks around players
            int chunkRadius = 1; // 3x3 chunk area (matches vanilla simulation distance)

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    LevelChunk chunk = world.getChunk(chunkX + dx, chunkZ + dz);

                    // Reduce tick rate to match vanilla spreading speed
                    // We're more efficient due to smart Y scanning, so we need fewer ticks
                    int ticksPerChunk = Math.max(1, randomTickSpeed / 3);

                    for (int i = 0; i < ticksPerChunk; i++) {
                        // 80% probability to skip this tick (only execute 20% of the time)
                        if (RANDOM.nextInt(5) != 0) {
                            continue;
                        }

                        int x = chunk.getPos().getMinBlockX() + RANDOM.nextInt(16);
                        int z = chunk.getPos().getMinBlockZ() + RANDOM.nextInt(16);

                        // Smart Y selection: focus on surface blocks where grass is more likely
                        // Check from top down to find the highest solid block
                        int y = Compat.topYExclusive(world);
                        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, y, z);

                        // Scan down to find surface (where grass would be)
                        for (int checkY = Compat.topYExclusive(world) - 1; checkY > Compat.bottomY(world); checkY--) {
                            mutablePos.setY(checkY);
                            BlockState checkState = world.getBlockState(mutablePos);

                            String checkId = Compat.blockId(checkState.getBlock());
                            if (checkId.equals(GRASS_LAYER_ID)
                                    || checkId.equals(VLP_GRASS_LAYER_ID)
                                    || checkState.is(Blocks.GRASS_BLOCK)) {
                                trySpreadGrass(world, mutablePos.immutable());
                                break; // Found grass, try to spread it
                            } else if (!checkState.isAir() && checkState.canOcclude()) {
                                // Hit a non-grass solid block, stop searching this column
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperties(BlockState source, BlockState target) {
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

    private static void trySpreadGrass(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        String sourceId = Compat.blockId(state.getBlock());

        boolean isCRGrass = sourceId.equals(GRASS_LAYER_ID);
        boolean isVLPGrass = sourceId.equals(VLP_GRASS_LAYER_ID);
        boolean isVanillaGrass = state.is(Blocks.GRASS_BLOCK);

        if (!isCRGrass && !isVLPGrass && !isVanillaGrass) return;

        // Check if there's enough light (same as vanilla grass)
        if (world.getMaxLocalRawBrightness(pos.above()) < 9) return;

        // Try to spread to neighboring blocks
        for (int i = 0; i < 4; i++) {
            BlockPos targetPos = pos.offset(
                RANDOM.nextInt(3) - 1,
                RANDOM.nextInt(5) - 3,
                RANDOM.nextInt(3) - 1
            );

            BlockState targetState = world.getBlockState(targetPos);
            String targetId = Compat.blockId(targetState.getBlock());

            if (world.getMaxLocalRawBrightness(targetPos.above()) < 9) continue;

            if (targetId.equals(LOAMY_DIRT_SLAB_ID) && (isCRGrass || isVanillaGrass)) {
                BlockState grassLayerState = copyProperties(targetState, Compat.blockFromId(GRASS_LAYER_ID).defaultBlockState());
                world.setBlock(targetPos, grassLayerState, 3);
            } else if (targetId.equals(VLP_DIRT_LAYER_ID) && (isVLPGrass || isVanillaGrass)) {
                BlockState grassLayerState = copyProperties(targetState, Compat.blockFromId(VLP_GRASS_LAYER_ID).defaultBlockState());
                world.setBlock(targetPos, grassLayerState, 3);
            } else if (targetId.equals("minecraft:dirt")) {
                BlockState aboveState = world.getBlockState(targetPos.above());
                if (aboveState.isAir()) {
                    world.setBlock(targetPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                }
            }
        }
    }
}
