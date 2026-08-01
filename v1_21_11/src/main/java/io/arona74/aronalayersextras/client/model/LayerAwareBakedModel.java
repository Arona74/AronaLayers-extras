package io.arona74.aronalayersextras.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/**
 * Shifts a plant's quads down so it sits on the surface of a partial-height
 * layer block below it.
 *
 * 1.21.11 replaced BakedModel with BlockStateModel and dropped the item side of
 * the interface entirely, so this wrapper cannot be shared with the older
 * modules. The offset maths itself is version-independent and lives in
 * LayerOffsetHooks.
 */
public class LayerAwareBakedModel implements BlockStateModel {

    private final BlockStateModel wrapped;

    public LayerAwareBakedModel(BlockStateModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos,
                          BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        float yOffset = LayerOffsetHooks.computeOffset(blockView, pos);
        if (yOffset != 0f) {
            emitter.pushTransform(quad -> {
                for (int v = 0; v < 4; v++) {
                    quad.pos(v, quad.x(v), quad.y(v) + yOffset, quad.z(v));
                }
                return true;
            });
        }
        wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
        if (yOffset != 0f) emitter.popTransform();
    }

    // createGeometryKey is deliberately NOT delegated to the wrapped model.
    //
    // The key lets the renderer reuse baked geometry between positions that
    // share it, but this offset depends on the block *below*, not on the state
    // being rendered. Forwarding the wrapped model's key would let a plant
    // standing on a layer block reuse the geometry of an identical plant
    // standing on full ground, and vice versa. The interface default returns
    // null, meaning "not cacheable", which is the correct answer here.

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        wrapped.collectParts(random, parts);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return wrapped.particleIcon();
    }

    @Override
    public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
        return wrapped.particleSprite(blockView, pos, state);
    }
}
