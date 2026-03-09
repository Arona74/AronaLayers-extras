package io.arona74.crlayersextras;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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
        CRLayersExtras.LOGGER.info("Registered layer fall handler");
    }

    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!ModConfig.getInstance().enableLayersFallWithSand) return;
        if (!(world instanceof ServerWorld)) return;

        // Only trigger when sand or gravel is broken directly by a player
        if (!state.isOf(Blocks.SAND) && !state.isOf(Blocks.RED_SAND) && !state.isOf(Blocks.GRAVEL)) return;

        // Cascade CR layer/slab blocks above the broken sand/gravel
        BlockPos checkPos = pos.up();
        for (int i = 0; i < 32; i++) {
            BlockState above = world.getBlockState(checkPos);
            if (isConquestLayerBlock(above)) {
                FallingBlockEntity.spawnFromBlock(world, checkPos, above);
                checkPos = checkPos.up();
            } else {
                break;
            }
        }
    }

    public static boolean isConquestLayerBlock(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        return "conquest".equals(id.getNamespace())
                && (id.getPath().contains("layer") || id.getPath().contains("slab"));
    }
}
