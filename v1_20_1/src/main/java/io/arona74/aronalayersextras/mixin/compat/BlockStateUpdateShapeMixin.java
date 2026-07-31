package io.arona74.aronalayersextras.mixin.compat;

import io.arona74.aronalayersextras.AronaLayersExtras;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
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
 * we intercept the dispatch in vanilla's BlockBehaviour.BlockStateBase.getStateForNeighborUpdate
 * (method_26191), which is the BlockState-level wrapper that delegates to the Block's
 * getStateForNeighborUpdate on all blocks.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateUpdateShapeMixin {

    @Redirect(
            method = "updateShape",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour;updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState safeGetStateForNeighborUpdate(
            BlockBehaviour block, BlockState state, Direction direction,
            BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        try {
            return block.updateShape(state, direction, neighborState, world, pos, neighborPos);
        } catch (IllegalArgumentException e) {
            AronaLayersExtras.LOGGER.debug(
                    "[AronaLayersExtras] Caught IAE in {}.getStateForNeighborUpdate, returning AIR: {}",
                    block.getClass().getSimpleName(), e.getMessage());
            return Blocks.AIR.defaultBlockState();
        }
    }
}
