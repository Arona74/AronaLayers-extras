package io.arona74.aronalayersextras;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LayerFallHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(LayerFallHandler::onBlockBroken);
        AronaLayersExtras.LOGGER.info("Registered layer fall handler");
    }

    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!ModConfig.getInstance().enableLayersFallWithSand) return;
        if (!(world instanceof ServerWorld)) return;
        if (!state.isOf(Blocks.SAND) && !state.isOf(Blocks.RED_SAND) && !state.isOf(Blocks.GRAVEL)) return;

        BlockPos checkPos = pos.up();
        for (int i = 0; i < 32; i++) {
            BlockState above = world.getBlockState(checkPos);
            if (isConquestLayerBlock(above)) {
                world.setBlockState(checkPos, above.getFluidState().getBlockState(), Block.NOTIFY_LISTENERS);
                FallingBlockEntity.spawnFromBlock(world, checkPos, above);
                checkPos = checkPos.up();
            } else {
                break;
            }
        }
    }

    public static boolean isConquestLayerBlock(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String ns = id.getNamespace();
        String path = id.getPath();
        if ("conquest".equals(ns)) {
            // Exclude mushroom layer blocks: CR's MushroomVanilla has a buggy
            // getStateForNeighborUpdate that crashes when the block is removed.
            if (path.contains("mushroom")) return false;
            return path.contains("layer") || path.contains("slab");
        }
        if ("vanillalayerplus".equals(ns)) {
            return path.endsWith("_layer");
        }
        return false;
    }
}
