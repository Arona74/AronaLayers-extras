package io.arona74.aronalayersextras.mixin.compat;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents crashes caused by mods (notably Conquest Reforged's MushroomVanilla)
 * that call BlockState.with(property, value) on a state (such as AIR) that
 * doesn't have that property, throwing IllegalArgumentException.
 *
 * We inject into StateHolder.with() — the base class method that actually throws —
 * and return the state unchanged instead of crashing when the property is absent.
 *
 * class_2688 = net.minecraft.world.level.block.state.StateHolder (Yarn 1.20.1+build.10)
 */
@Mixin(StateHolder.class)
public abstract class StateWithMixin {

    @Shadow
    public abstract boolean hasProperty(Property<?> property);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private <T extends Comparable<T>> void guardInvalidWith(
            Property<T> property, T value, CallbackInfoReturnable cir) {
        if (!hasProperty(property)) {
            cir.setReturnValue(this);
        }
    }
}
