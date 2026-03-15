package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.SheepGrassEatingHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.arona74.aronalayersextras.ModConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EatGrassGoal.class)
public class EatGrassGoalMixin {
    private static final Identifier GRASS_LAYER_ID = new Identifier("conquest", "grass_block_layer");
    private static final Identifier LOAMY_DIRT_SLAB_ID = new Identifier("conquest", "loamy_dirt_slab");
    private static final Identifier VLP_GRASS_LAYER_ID = new Identifier("vanillalayerplus", "grass_layer");
    private static final Identifier VLP_DIRT_LAYER_ID = new Identifier("vanillalayerplus", "dirt_layer");

    @Shadow
    @Final
    private MobEntity mob;

    @Shadow
    private World world;

    @Shadow
    private int timer;

    /**
     * Inject into canStart() to also detect grass_block_layer
     * We handle the complete check (including random) only when grass_block_layer is present
     * This prevents vanilla from doing a second random check
     */
    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void canStartWithGrassLayer(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.getInstance().enableSheepEatingGrassLayers) return;

        BlockPos pos = this.mob.getBlockPos();
        BlockState state = this.world.getBlockState(pos);

        // Only handle if a modded grass layer is present
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
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
        if (this.timer == 4) {
            BlockPos pos = this.mob.getBlockPos();
            BlockState state = this.world.getBlockState(pos);

            Identifier blockId = Registries.BLOCK.getId(state.getBlock());
            boolean isCRGrass = blockId.equals(GRASS_LAYER_ID);
            boolean isVLPGrass = blockId.equals(VLP_GRASS_LAYER_ID);

            if (isCRGrass || isVLPGrass) {
                if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    Identifier dirtId = isCRGrass ? LOAMY_DIRT_SLAB_ID : VLP_DIRT_LAYER_ID;
                    BlockState dirtState = SheepGrassEatingHandler.copyPropertiesPublic(state, Registries.BLOCK.get(dirtId).getDefaultState());
                    this.world.setBlockState(pos, dirtState, 2);
                }
                this.mob.onEatingGrass();
            }
        }
    }
}
