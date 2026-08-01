package io.arona74.aronalayersextras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerLevel;
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

    // 1.21.11 rebuilt the gamerule system: the rules moved to
    // net.minecraft.world.level.gamerules, the constants were renamed
    // (RULE_RANDOMTICKING -> RANDOM_TICK_SPEED, RULE_MOBGRIEFING ->
    // MOB_GRIEFING), and the typed getInt/getBoolean accessors collapsed into
    // one generic get(GameRule<T>), reached via getLevelData().
    public static int randomTickSpeed(ServerLevel world) {
        return world.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
    }

    public static boolean doMobGriefing(Level world) {
        // Level no longer declares getGameRules on 1.21.11; only ServerLevel
        // does. The only caller is the sheep eating goal, which ticks
        // server-side, so a client-side Level answering false is unreachable
        // rather than a behaviour change.
        return world instanceof ServerLevel server
                && server.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    /**
     * Register a handler that runs at the end of each server level tick.
     *
     * Fabric API renamed ServerTickEvents.END_WORLD_TICK to END_LEVEL_TICK for
     * 26.2. The event is a Fabric name rather than a Minecraft one, but it is
     * still a name shared code cannot spell for every version.
     */
    public static void onEndLevelTick(java.util.function.Consumer<ServerLevel> handler) {
        ServerTickEvents.END_WORLD_TICK.register(handler::accept);
    }
}
