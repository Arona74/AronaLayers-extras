package io.arona74.aronalayersextras.mixin.compat;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes a crash in Conquest Reforged's MushroomVanilla block.
 *
 * CR's getStateForNeighborUpdate calls super (which can return AIR) and then calls
 * BlockState.with(LAYERS, value) on the result. If the result is AIR, that throws
 * IllegalArgumentException because AIR has no LAYERS property.
 *
 * Rather than guarding at HEAD (which has descriptor-matching issues with @Pseudo),
 * we redirect the exact .with() call that crashes and make it a no-op when the
 * property doesn't exist on the state.
 */
@Pseudo
@Mixin(targets = "com.conquestrefabricated.content.blocks.block.vanilla.MushroomVanilla", remap = false)
public class MushroomVanillaMixin {

    /**
     * Redirect BlockState.with() inside MushroomVanilla.getStateForNeighborUpdate.
     * If the state (which may be AIR after the super call) doesn't have the property,
     * return the state as-is instead of throwing IAE.
     *
     * Target uses intermediary names because remap = false.
     * class_2688 = net.minecraft.state.State (base state class, defines with())
     * class_2748 = net.minecraft.state.property.Property
     */
    @Redirect(
            method = "method_9559",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_2688;method_11657(Lnet/minecraft/class_2748;Ljava/lang/Comparable;)Lnet/minecraft/class_2688;"
            ),
            remap = false
    )
    private <T extends Comparable<T>> BlockState safeWith(BlockState state, Property<T> property, T value) {
        if (!state.contains(property)) {
            return state;
        }
        return state.with(property, value);
    }
}
