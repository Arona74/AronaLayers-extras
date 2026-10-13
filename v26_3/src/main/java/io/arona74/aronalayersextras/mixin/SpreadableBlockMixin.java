package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyBlock.class)
// The 26.x line renamed SpreadingSnowyDirtBlock to SpreadingSnowyBlock, already
// in 26.1. It is a rename, not a split: GrassBlock and MyceliumBlock are still
// its subclasses, and randomTick keeps the same signature there, so the guard is
// unchanged.
public class SpreadableBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventGrassDecay(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (ModConfig.getInstance().preventGrassDecay && state.is(Blocks.GRASS_BLOCK)) {
            ci.cancel();
        }
    }
}
