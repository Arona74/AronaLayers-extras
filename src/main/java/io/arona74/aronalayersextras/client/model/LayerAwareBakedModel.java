package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.List;
import java.util.function.Supplier;

public class LayerAwareBakedModel implements BakedModel {

    private final BakedModel wrapped;

    public LayerAwareBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    private float computeOffset(BlockAndTintGetter blockView, BlockPos pos) {
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

    private float yOffsetFor(BlockState state, BlockAndTintGetter blockView, BlockPos pos) {
        // Prefer collision shape: it defines the actual surface entities stand on,
        // which is what matters for plant placement. Some modded layer blocks (e.g.
        // VanillaLayer+) leave getOutlineShape at the default full cube while
        // overriding getCollisionShape for their actual layer height.
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

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, RenderContext context) {
        float yOffset = computeOffset(blockView, pos);
        if (yOffset != 0f) {
            context.pushTransform(quad -> {
                for (int v = 0; v < 4; v++) {
                    quad.pos(v, quad.x(v), quad.y(v) + yOffset, quad.z(v));
                }
                return true;
            });
        }
        wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        if (yOffset != 0f) context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        wrapped.emitItemQuads(stack, randomSupplier, context);
    }

    // Delegate all vanilla BakedModel methods to the wrapped model

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random) {
        return wrapped.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }
}
