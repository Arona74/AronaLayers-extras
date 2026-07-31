package io.arona74.aronalayersextras;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LayerFallHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(LayerFallHandler::onBlockBroken);
        AronaLayersExtras.LOGGER.info("Registered layer fall handler");
    }

    private static void onBlockBroken(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!ModConfig.getInstance().enableLayersFallWithSand) return;
        if (!(world instanceof ServerLevel)) return;
        if (!state.is(Blocks.SAND) && !state.is(Blocks.RED_SAND) && !state.is(Blocks.GRAVEL)) return;

        BlockPos checkPos = pos.above();
        for (int i = 0; i < 32; i++) {
            BlockState above = world.getBlockState(checkPos);
            if (isConquestLayerBlock(above)) {
                world.setBlock(checkPos, above.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS);
                FallingBlockEntity.fall(world, checkPos, above);
                checkPos = checkPos.above();
            } else {
                break;
            }
        }
    }

    public static boolean isConquestLayerBlock(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
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
