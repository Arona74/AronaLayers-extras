package io.arona74.aronalayersextras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

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
     * Never returns null. Callers dereference the result immediately, and a
     * null sentinel here type-checks everywhere and only shows up at runtime.
     */
    public static Block blockFromId(String id) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
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
        return world.getMaxBuildHeight();
    }

    /** Lowest buildable Y. */
    public static int bottomY(LevelHeightAccessor world) {
        return world.getMinBuildHeight();
    }

    public static int randomTickSpeed(ServerLevel world) {
        return world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
    }

    public static boolean doMobGriefing(Level world) {
        return world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
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

    /** Fortune level of a tool. 1.20.1 still has enchantments as plain objects (BLOCK_FORTUNE). */
    public static int fortuneLevel(ServerLevel level, net.minecraft.world.item.ItemStack tool) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE, tool);
    }

    public static int silkTouchLevel(ServerLevel level, net.minecraft.world.item.ItemStack tool) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, tool);
    }

    /** The doTileDrops game rule. */
    public static boolean doBlockDrops(ServerLevel level) {
        return level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
    }

    public static net.minecraft.world.item.Item.Properties itemProperties(String path) {
        return new net.minecraft.world.item.Item.Properties();
    }

    public static net.minecraft.world.item.Item registerItem(String path, net.minecraft.world.item.Item item) {
        return net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(AronaLayersExtras.MOD_ID, path), item);
    }

    public static void addToIngredientsTab(java.util.List<net.minecraft.world.item.Item> items) {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(
                net.minecraft.world.item.CreativeModeTabs.INGREDIENTS).register(entries -> items.forEach(entries::accept));
    }
}
