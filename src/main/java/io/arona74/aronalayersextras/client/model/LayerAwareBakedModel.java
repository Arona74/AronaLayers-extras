package io.arona74.aronalayersextras.client.model;

import io.arona74.aronalayersextras.ModConfig;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.function.Supplier;

public class LayerAwareBakedModel implements BakedModel {

    private final BakedModel wrapped;

    public LayerAwareBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    private float computeOffset(BlockRenderView blockView, BlockPos pos) {
        if (!ModConfig.getInstance().enableBlockOffset) return 0f;

        BlockState below = blockView.getBlockState(pos.down());
        float offset = yOffsetFor(below, blockView, pos.down());
        if (offset != 0f) return offset;

        // Upper half of a 2-block-tall plant: check two blocks down
        if (below.contains(Properties.DOUBLE_BLOCK_HALF)
                && below.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
            BlockState belowBelow = blockView.getBlockState(pos.down().down());
            offset = yOffsetFor(belowBelow, blockView, pos.down().down());
            if (offset != 0f) return offset;
        }

        return 0f;
    }

    private float yOffsetFor(BlockState state, BlockRenderView blockView, BlockPos pos) {
        var shape = state.getOutlineShape(blockView, pos);
        if (shape.isEmpty()) return 0f;
        double topY = shape.getMax(Direction.Axis.Y);
        if (!Double.isFinite(topY) || topY <= 0.0 || topY >= 1.0) return 0f;
        return -(float) (1.0 - topY);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
                               Supplier<Random> randomSupplier, RenderContext context) {
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
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        wrapped.emitItemQuads(stack, randomSupplier, context);
    }

    // Delegate all vanilla BakedModel methods to the wrapped model

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return wrapped.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return wrapped.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return wrapped.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return wrapped.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return wrapped.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return wrapped.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return wrapped.getOverrides();
    }
}
