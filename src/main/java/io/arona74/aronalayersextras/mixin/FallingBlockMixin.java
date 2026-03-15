package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.LayerFallHandler;
import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public class FallingBlockMixin {

    @Inject(method = "spawnFromBlock", at = @At("TAIL"))
    private static void onSpawnFromBlock(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir) {
        if (!ModConfig.getInstance().enableLayersFallWithSand) return;
        if (!state.isOf(Blocks.SAND) && !state.isOf(Blocks.RED_SAND) && !state.isOf(Blocks.GRAVEL)) return;

        BlockPos checkPos = pos.up();
        for (int i = 0; i < 32; i++) {
            BlockState above = world.getBlockState(checkPos);
            if (LayerFallHandler.isConquestLayerBlock(above)) {
                world.setBlockState(checkPos, above.getFluidState().getBlockState(), Block.NOTIFY_LISTENERS);
                FallingBlockEntity.spawnFromBlock(world, checkPos, above);
                checkPos = checkPos.up();
            } else {
                break;
            }
        }
    }
}
