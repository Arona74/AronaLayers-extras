package io.arona74.aronalayersextras.mixin;

import io.arona74.aronalayersextras.ore.OreLayerDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the drops of VanillaLayer+ ore layers broken by a player (see OreLayerDrops).
 *
 * <p>This overload is the one Block.playerDestroy calls, after the game has already checked the
 * tool may harvest the block, and it is the only one that knows the tool, which Fortune and Silk
 * Touch need. Explosions and other tool-less breaks use the other overloads and keep VanillaLayer+'s
 * own drop. Done in code rather than by overriding VanillaLayer+'s loot table files, because when
 * two mods ship the same data file, which one wins is not defined.
 */
@Mixin(Block.class)
public abstract class OreLayerDropMixin {

    @Inject(
        method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void aronalayersextras$smallOreDrops(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity,
                                                         Entity entity, ItemStack tool, CallbackInfo ci) {
        if (level instanceof ServerLevel server && OreLayerDrops.drop(state, server, pos, tool)) {
            ci.cancel();
        }
    }
}
