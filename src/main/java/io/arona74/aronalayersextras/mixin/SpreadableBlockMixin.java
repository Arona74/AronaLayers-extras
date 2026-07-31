package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyDirtBlock.class)
public class SpreadableBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventGrassDecay(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (ModConfig.getInstance().preventGrassDecay && state.is(Blocks.GRASS_BLOCK)) {
            ci.cancel();
        }
    }
}
