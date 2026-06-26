package io.arona74.aronalayersextras.mixin.compat;

import io.arona74.aronalayersextras.AronaLayersExtras;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes a crash in Conquest Reforged's MushroomVanilla block (and any other mod
 * that has a broken getStateForNeighborUpdate implementation).
 *
 * CR's MushroomVanilla.getStateForNeighborUpdate calls super (which can return AIR)
 * and then tries to copy the 'layers' property onto that AIR state via BlockState.with(),
 * throwing IllegalArgumentException.
 *
 * Rather than injecting into the CR class directly (which is fragile with @Pseudo),
 * we intercept the dispatch in vanilla's AbstractBlock.AbstractBlockState.getStateForNeighborUpdate
 * (method_26191), which is the BlockState-level wrapper that delegates to the Block's
 * getStateForNeighborUpdate on all blocks.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateUpdateShapeMixin {

    @Redirect(
            method = "getStateForNeighborUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/AbstractBlock;getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
            )
    )
    private BlockState safeGetStateForNeighborUpdate(
            AbstractBlock block, BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        try {
            return block.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        } catch (IllegalArgumentException e) {
            AronaLayersExtras.LOGGER.debug(
                    "[AronaLayersExtras] Caught IAE in {}.getStateForNeighborUpdate, returning AIR: {}",
                    block.getClass().getSimpleName(), e.getMessage());
            return Blocks.AIR.getDefaultState();
        }
    }
}
