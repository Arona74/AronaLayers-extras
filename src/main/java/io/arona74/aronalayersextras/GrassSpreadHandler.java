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

public class GrassSpreadHandler {
    private static final Identifier GRASS_LAYER_ID = new Identifier("conquest", "grass_block_layer");
    private static final Identifier LOAMY_DIRT_SLAB_ID = new Identifier("conquest", "loamy_dirt_slab");
    private static final Identifier VLP_GRASS_LAYER_ID = new Identifier("vanillalayerplus", "grass_layer");
    private static final Identifier VLP_DIRT_LAYER_ID = new Identifier("vanillalayerplus", "dirt_layer");

    private static final Random RANDOM = new Random();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(GrassSpreadHandler::onWorldTick);
        AronaLayersExtras.LOGGER.info("Registered grass spreading handler");
    }

    private static void onWorldTick(ServerWorld world) {
        if (!ModConfig.getInstance().enableGrassSpreading) return;

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

                        // Smart Y selection: focus on surface blocks where grass is more likely
                        // Check from top down to find the highest solid block
                        int y = world.getTopY();
                        BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, y, z);

                        // Scan down to find surface (where grass would be)
                        for (int checkY = world.getTopY() - 1; checkY > world.getBottomY(); checkY--) {
                            mutablePos.setY(checkY);
                            BlockState checkState = world.getBlockState(mutablePos);

                            Identifier checkId = Registries.BLOCK.getId(checkState.getBlock());
                            if (checkId.equals(GRASS_LAYER_ID)
                                    || checkId.equals(VLP_GRASS_LAYER_ID)
                                    || checkState.isOf(Blocks.GRASS_BLOCK)) {
                                trySpreadGrass(world, mutablePos.toImmutable());
                                break; // Found grass, try to spread it
                            } else if (!checkState.isAir() && checkState.isOpaque()) {
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

    private static void trySpreadGrass(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Identifier sourceId = Registries.BLOCK.getId(state.getBlock());

        boolean isCRGrass = sourceId.equals(GRASS_LAYER_ID);
        boolean isVLPGrass = sourceId.equals(VLP_GRASS_LAYER_ID);
        boolean isVanillaGrass = state.isOf(Blocks.GRASS_BLOCK);

        if (!isCRGrass && !isVLPGrass && !isVanillaGrass) return;

        // Check if there's enough light (same as vanilla grass)
        if (world.getLightLevel(pos.up()) < 9) return;

        // Try to spread to neighboring blocks
        for (int i = 0; i < 4; i++) {
            BlockPos targetPos = pos.add(
                RANDOM.nextInt(3) - 1,
                RANDOM.nextInt(5) - 3,
                RANDOM.nextInt(3) - 1
            );

            BlockState targetState = world.getBlockState(targetPos);
            Identifier targetId = Registries.BLOCK.getId(targetState.getBlock());

            if (world.getLightLevel(targetPos.up()) < 9) continue;

            if (targetId.equals(LOAMY_DIRT_SLAB_ID) && (isCRGrass || isVanillaGrass)) {
                BlockState grassLayerState = copyProperties(targetState, Registries.BLOCK.get(GRASS_LAYER_ID).getDefaultState());
                world.setBlockState(targetPos, grassLayerState, 3);
            } else if (targetId.equals(VLP_DIRT_LAYER_ID) && (isVLPGrass || isVanillaGrass)) {
                BlockState grassLayerState = copyProperties(targetState, Registries.BLOCK.get(VLP_GRASS_LAYER_ID).getDefaultState());
                world.setBlockState(targetPos, grassLayerState, 3);
            } else if (targetId.toString().equals("minecraft:dirt")) {
                BlockState aboveState = world.getBlockState(targetPos.up());
                if (aboveState.isAir()) {
                    world.setBlockState(targetPos, Blocks.GRASS_BLOCK.getDefaultState(), 3);
                }
            }
        }
    }
}
