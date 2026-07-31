package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.SheepGrassEatingHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.arona74.aronalayersextras.ModConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EatBlockGoal.class)
public class EatGrassGoalMixin {
    private static final String GRASS_LAYER_ID = "conquest:grass_block_layer";
    private static final String LOAMY_DIRT_SLAB_ID = "conquest:loamy_dirt_slab";
    private static final String VLP_GRASS_LAYER_ID = "vanillalayerplus:grass_layer";
    private static final String VLP_DIRT_LAYER_ID = "vanillalayerplus:dirt_layer";

    @Shadow
    @Final
    private Mob mob;

    @Shadow
    private Level level;

    @Shadow
    private int eatAnimationTick;

    /**
     * Inject into canStart() to also detect grass_block_layer
     * We handle the complete check (including random) only when grass_block_layer is present
     * This prevents vanilla from doing a second random check
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void canStartWithGrassLayer(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.getInstance().enableSheepEatingGrassLayers) return;

        BlockPos pos = this.mob.blockPosition();
        BlockState state = this.level.getBlockState(pos);

        // Only handle if a modded grass layer is present
        String blockId = Compat.blockId(state.getBlock());
        if (!blockId.equals(GRASS_LAYER_ID) && !blockId.equals(VLP_GRASS_LAYER_ID)) {
            return;
        }

        // We have grass_block_layer, do the random check ourselves (same as vanilla)
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }

    /**
     * Inject into tick() to handle grass_block_layer consumption
     * This is where the actual eating happens when timer reaches 4
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", shift = At.Shift.AFTER))
    private void tickWithGrassLayer(CallbackInfo ci) {
        if (!ModConfig.getInstance().enableSheepEatingGrassLayers) return;

        // Inject AFTER timer decrement. At timer == 4, the eating happens
        if (this.eatAnimationTick == 4) {
            BlockPos pos = this.mob.blockPosition();
            BlockState state = this.level.getBlockState(pos);

            String blockId = Compat.blockId(state.getBlock());
            boolean isCRGrass = blockId.equals(GRASS_LAYER_ID);
            boolean isVLPGrass = blockId.equals(VLP_GRASS_LAYER_ID);

            if (isCRGrass || isVLPGrass) {
                if (Compat.doMobGriefing(this.level)) {
                    String dirtId = isCRGrass ? LOAMY_DIRT_SLAB_ID : VLP_DIRT_LAYER_ID;
                    BlockState dirtState = SheepGrassEatingHandler.copyPropertiesPublic(state, Compat.blockFromId(dirtId).defaultBlockState());
                    this.level.setBlock(pos, dirtState, 2);
                }
                this.mob.ate();
            }
        }
    }
}
