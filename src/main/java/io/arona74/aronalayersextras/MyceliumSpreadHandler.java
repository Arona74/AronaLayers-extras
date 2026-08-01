package io.arona74.aronalayersextras;

import io.arona74.aronalayersextras.Compat;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Random;

public class MyceliumSpreadHandler {
    private static final String MYCELIUM_LAYER_ID = "conquest:mycelium_layer";
    private static final String LOAMY_DIRT_SLAB_ID = "conquest:loamy_dirt_slab";
    private static final String VLP_MYCELIUM_LAYER_ID = "vanillalayerplus:mycelium_layer";
    private static final String VLP_DIRT_LAYER_ID = "vanillalayerplus:dirt_layer";

    private static final Random RANDOM = new Random();

    public static void register() {
        Compat.onEndLevelTick(MyceliumSpreadHandler::onWorldTick);
        AronaLayersExtras.LOGGER.info("Registered mycelium spreading handler");
    }

    private static void onWorldTick(ServerLevel world) {
        if (!ModConfig.getInstance().enableMyceliumSpreading) return;

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

                        // Smart Y selection: focus on surface blocks where mycelium is more likely
                        // Check from top down to find the highest solid block
                        int y = Compat.topYExclusive(world);
                        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, y, z);

                        // Scan down to find surface (where mycelium would be)
                        for (int checkY = Compat.topYExclusive(world) - 1; checkY > Compat.bottomY(world); checkY--) {
                            mutablePos.setY(checkY);
                            BlockState checkState = world.getBlockState(mutablePos);

                            String checkId = Compat.blockId(checkState.getBlock());
                            if (checkId.equals(MYCELIUM_LAYER_ID) || checkId.equals(VLP_MYCELIUM_LAYER_ID)) {
                                trySpreadMycelium(world, mutablePos.immutable());
                                break; // Found mycelium, try to spread it
                            } else if (!checkState.isAir() && checkState.canOcclude()) {
                                // Hit a non-mycelium solid block, stop searching this column
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

    private static void trySpreadMycelium(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        String sourceId = Compat.blockId(state.getBlock());

        boolean isCRMycelium = sourceId.equals(MYCELIUM_LAYER_ID);
        boolean isVLPMycelium = sourceId.equals(VLP_MYCELIUM_LAYER_ID);

        if (!isCRMycelium && !isVLPMycelium) return;

        // Mycelium spreads regardless of light level (unlike grass)
        for (int i = 0; i < 4; i++) {
            BlockPos targetPos = pos.offset(
                RANDOM.nextInt(3) - 1,
                RANDOM.nextInt(5) - 3,
                RANDOM.nextInt(3) - 1
            );

            BlockState targetState = world.getBlockState(targetPos);
            String targetId = Compat.blockId(targetState.getBlock());

            if (targetId.equals(LOAMY_DIRT_SLAB_ID) && isCRMycelium) {
                world.setBlock(targetPos, copyProperties(targetState, Compat.blockFromId(MYCELIUM_LAYER_ID).defaultBlockState()), 3);
            } else if (targetId.equals(VLP_DIRT_LAYER_ID) && isVLPMycelium) {
                world.setBlock(targetPos, copyProperties(targetState, Compat.blockFromId(VLP_MYCELIUM_LAYER_ID).defaultBlockState()), 3);
            } else if (targetId.equals("minecraft:dirt")) {
                world.setBlock(targetPos, Blocks.MYCELIUM.defaultBlockState(), 3);
            }
        }
    }
}
