package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.ModConfig;
import io.arona74.aronalayersextras.SheepGrassEatingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public class SheepEntityMixin {

    @Inject(method = "ate", at = @At("HEAD"))
    private void onEatingGrass(CallbackInfo ci) {
        if (!ModConfig.getInstance().enableSheepEatingGrassLayers) return;

        Sheep sheep = (Sheep) (Object) this;
        SheepGrassEatingHandler.tryEatGrassLayer(sheep.level(), sheep.blockPosition());
    }
}
