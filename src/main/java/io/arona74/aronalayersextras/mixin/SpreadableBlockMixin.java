package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventGrassDecay(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (ModConfig.getInstance().preventGrassDecay && state.isOf(Blocks.GRASS_BLOCK)) {
            ci.cancel();
        }
    }
}
