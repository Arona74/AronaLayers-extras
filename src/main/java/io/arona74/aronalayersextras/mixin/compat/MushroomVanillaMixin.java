package io.arona74.aronalayersextras.mixin.compat;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes a crash in Conquest Reforged's MushroomVanilla block.
 *
 * CR's getStateForNeighborUpdate calls super (which can return AIR for unsupported blocks)
 * and then tries to copy the 'layers' property onto that AIR state, throwing
 * IllegalArgumentException. This mixin intercepts the call early and returns AIR
 * safely when the block can no longer be placed at its position.
 */
@Pseudo
@Mixin(targets = "com.conquestrefabricated.content.blocks.block.vanilla.MushroomVanilla", remap = false)
public class MushroomVanillaMixin {

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true, remap = true)
    private void fixLayersOnAirCrash(
            BlockState state, Direction direction, BlockState neighborState,
            WorldAccess world, BlockPos pos, BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir) {
        // If the block can no longer be placed at its current position (e.g. support below
        // was removed), return AIR directly. This prevents CR's code from reaching the
        // .with(LAYERS, value) call on an AIR BlockState, which would throw IAE.
        if (!state.canPlaceAt(world, pos)) {
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }
}
