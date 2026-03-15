package io.arona74.aronalayersextras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Random;

public class MyceliumSpreadHandler {
    private static final Identifier MYCELIUM_LAYER_ID = new Identifier("conquest", "mycelium_layer");
    private static final Identifier LOAMY_DIRT_SLAB_ID = new Identifier("conquest", "loamy_dirt_slab");
    private static final Identifier VLP_MYCELIUM_LAYER_ID = new Identifier("vanillalayerplus", "mycelium_layer");
    private static final Identifier VLP_DIRT_LAYER_ID = new Identifier("vanillalayerplus", "dirt_layer");

    private static final Random RANDOM = new Random();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MyceliumSpreadHandler::onWorldTick);
        AronaLayersExtras.LOGGER.info("Registered mycelium spreading handler");
    }

    private static void onWorldTick(ServerWorld world) {
        if (!ModConfig.getInstance().enableMyceliumSpreading) return;

        // Get the randomTickSpeed value (default is 3)
        int randomTickSpeed = world.getGameRules().getInt(net.minecraft.world.GameRules.RANDOM_TICK_SPEED);

        if (randomTickSpeed <= 0) {
            return;
        }

        // Process chunks around players - much more aggressively than before
        world.getPlayers().forEach(player -> {
            BlockPos playerPos = player.getBlockPos();
            int chunkX = playerPos.getX() >> 4;
            int chunkZ = playerPos.getZ() >> 4;

            // Process chunks around players
            int chunkRadius = 1; // 3x3 chunk area (matches vanilla simulation distance)

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    WorldChunk chunk = world.getChunk(chunkX + dx, chunkZ + dz);

                    // Reduce tick rate to match vanilla spreading speed
                    // We're more efficient due to smart Y scanning, so we need fewer ticks
                    int ticksPerChunk = Math.max(1, randomTickSpeed / 3);

                    for (int i = 0; i < ticksPerChunk; i++) {
                        // 80% probability to skip this tick (only execute 20% of the time)
                        if (RANDOM.nextInt(5) != 0) {
                            continue;
                        }

                        int x = chunk.getPos().getStartX() + RANDOM.nextInt(16);
                        int z = chunk.getPos().getStartZ() + RANDOM.nextInt(16);

                        // Smart Y selection: focus on surface blocks where mycelium is more likely
                        // Check from top down to find the highest solid block
                        int y = world.getTopY();
                        BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, y, z);

                        // Scan down to find surface (where mycelium would be)
                        for (int checkY = world.getTopY() - 1; checkY > world.getBottomY(); checkY--) {
                            mutablePos.setY(checkY);
                            BlockState checkState = world.getBlockState(mutablePos);

                            Identifier checkId = Registries.BLOCK.getId(checkState.getBlock());
                            if (checkId.equals(MYCELIUM_LAYER_ID) || checkId.equals(VLP_MYCELIUM_LAYER_ID)) {
                                trySpreadMycelium(world, mutablePos.toImmutable());
                                break; // Found mycelium, try to spread it
                            } else if (!checkState.isAir() && checkState.isOpaque()) {
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

    private static void trySpreadMycelium(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Identifier sourceId = Registries.BLOCK.getId(state.getBlock());

        boolean isCRMycelium = sourceId.equals(MYCELIUM_LAYER_ID);
        boolean isVLPMycelium = sourceId.equals(VLP_MYCELIUM_LAYER_ID);

        if (!isCRMycelium && !isVLPMycelium) return;

        // Mycelium spreads regardless of light level (unlike grass)
        for (int i = 0; i < 4; i++) {
            BlockPos targetPos = pos.add(
                RANDOM.nextInt(3) - 1,
                RANDOM.nextInt(5) - 3,
                RANDOM.nextInt(3) - 1
            );

            BlockState targetState = world.getBlockState(targetPos);
            Identifier targetId = Registries.BLOCK.getId(targetState.getBlock());

            if (targetId.equals(LOAMY_DIRT_SLAB_ID) && isCRMycelium) {
                world.setBlockState(targetPos, copyProperties(targetState, Registries.BLOCK.get(MYCELIUM_LAYER_ID).getDefaultState()), 3);
            } else if (targetId.equals(VLP_DIRT_LAYER_ID) && isVLPMycelium) {
                world.setBlockState(targetPos, copyProperties(targetState, Registries.BLOCK.get(VLP_MYCELIUM_LAYER_ID).getDefaultState()), 3);
            } else if (targetId.toString().equals("minecraft:dirt")) {
                world.setBlockState(targetPos, Blocks.MYCELIUM.getDefaultState(), 3);
            }
        }
    }
}
