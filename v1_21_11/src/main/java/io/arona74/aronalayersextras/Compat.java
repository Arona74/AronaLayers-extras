package io.arona74.aronalayersextras;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Per-version shims.
 *
 * Block ids cross this boundary as canonical "namespace:path" strings rather
 * than as the id type itself. Java has no type aliases, and 1.21.11 renamed
 * ResourceLocation to Identifier, so shared code cannot name that type at all
 * without being duplicated per module.
 */
public final class Compat {
    private Compat() {}

    /** Canonical "namespace:path" id of a block. */
    public static String blockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    /**
     * Block registered under a canonical id, or AIR when nothing is.
     *
     * getValue is deliberate. On 1.21.11 Registry#get(Identifier) returns an
     * Optional, which would push a null or empty sentinel into shared code;
     * getValue on the defaulted block registry keeps the pre-1.21.11 behaviour
     * of answering AIR. Callers dereference the result immediately, and a null
     * sentinel here type-checks everywhere and only shows up at runtime.
     */
    public static Block blockFromId(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    }

    /**
     * One past the highest buildable Y.
     *
     * Deliberately not expressed as a rename of getMaxBuildHeight. On 1.21.11
     * that method became getMaxY, which is INCLUSIVE
     * (getMinY() + getHeight() - 1) where getMaxBuildHeight was EXCLUSIVE
     * (getMinBuildHeight() + getHeight()) -- verified in the bytecode of both
     * versions. Renaming it in place compiles and silently shifts every
     * top-down scan by one block, so the contract is fixed here instead.
     */
    public static int topYExclusive(LevelHeightAccessor world) {
        return world.getMaxY() + 1;
    }

    /** Lowest buildable Y. */
    public static int bottomY(LevelHeightAccessor world) {
        return world.getMinY();
    }

    public static int randomTickSpeed(Level world) {
        return world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
    }

    public static boolean doMobGriefing(Level world) {
        return world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }
}
