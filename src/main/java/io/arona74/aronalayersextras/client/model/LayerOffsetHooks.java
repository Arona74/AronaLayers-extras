package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * How far to shift a plant down so it sits on the surface of a partial-height
 * layer block below it.
 *
 * This is shared across every supported version because none of the types it
 * touches changed. The model wrapper that calls it cannot be shared: 1.21.11
 * renamed BakedModel to BlockStateModel and dropped the item-model side of the
 * interface, so each module carries its own wrapper holding only the signature.
 */
public final class LayerOffsetHooks {
    private LayerOffsetHooks() {}

    public static float computeOffset(BlockAndTintGetter blockView, BlockPos pos) {
        if (!ModConfig.getInstance().enableBlockOffset) return 0f;

        BlockState below = blockView.getBlockState(pos.below());
        float offset = yOffsetFor(below, blockView, pos.below());
        if (offset != 0f) return offset;

        // Upper half of a 2-block-tall plant: check two blocks down
        if (below.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && below.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
            BlockState belowBelow = blockView.getBlockState(pos.below().below());
            offset = yOffsetFor(belowBelow, blockView, pos.below().below());
            if (offset != 0f) return offset;
        }

        return 0f;
    }

    private static float yOffsetFor(BlockState state, BlockAndTintGetter blockView, BlockPos pos) {
        // Prefer collision shape: it defines the actual surface entities stand on,
        // which is what matters for plant placement. Some modded layer blocks (e.g.
        // VanillaLayer+) leave the outline shape at the default full cube while
        // overriding the collision shape for their actual layer height.
        // Fall back to outline shape when collision shape is empty — vanilla snow at
        // layers=1 has no collision shape but does have a visible 2px outline shape.
        var shape = state.getCollisionShape(blockView, pos);
        if (shape.isEmpty()) {
            shape = state.getShape(blockView, pos);
        }
        if (shape.isEmpty()) return 0f;
        double topY = shape.max(Direction.Axis.Y);
        if (!Double.isFinite(topY) || topY <= 0.0 || topY >= 1.0) return 0f;
        return -(float) (1.0 - topY);
    }
}
