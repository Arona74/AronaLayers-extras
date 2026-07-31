package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.LayerFallHandler;
import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public class FallingBlockMixin {

    @Inject(method = "fall", at = @At("TAIL"))
    private static void onSpawnFromBlock(Level world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir) {
        if (!ModConfig.getInstance().enableLayersFallWithSand) return;
        if (!state.is(Blocks.SAND) && !state.is(Blocks.RED_SAND) && !state.is(Blocks.GRAVEL)) return;

        BlockPos checkPos = pos.above();
        for (int i = 0; i < 32; i++) {
            BlockState above = world.getBlockState(checkPos);
            if (LayerFallHandler.isConquestLayerBlock(above)) {
                world.setBlock(checkPos, above.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS);
                FallingBlockEntity.fall(world, checkPos, above);
                checkPos = checkPos.above();
            } else {
                break;
            }
        }
    }
}
