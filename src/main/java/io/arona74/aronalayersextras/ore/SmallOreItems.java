package io.arona74.aronalayersextras.ore;

import io.arona74.aronalayersextras.Compat;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Ore fragments dropped by VanillaLayer+ ore layers (see OreLayerDrops), one per layer. Eight
 * craft back into the ore's normal drop, so a full 8-layer stack is worth exactly one ore block.
 *
 * <p>Only ores whose normal drop is a single item need a fragment. Redstone, lapis, nether gold and
 * nether quartz already drop small vanilla items, so their layers drop a share of those directly.
 *
 * <p>Registered unconditionally: item registries are synced to clients, so whether the items exist
 * cannot depend on a config toggle or on VanillaLayer+ being installed.
 */
public final class SmallOreItems {
    private SmallOreItems() {}

    public static Item SMALL_COAL;
    public static Item SMALL_RAW_IRON;
    public static Item SMALL_RAW_COPPER;
    public static Item SMALL_RAW_GOLD;
    public static Item SMALL_DIAMOND;
    public static Item SMALL_EMERALD;

    public static void register() {
        SMALL_COAL = register("small_coal");
        SMALL_RAW_IRON = register("small_raw_iron");
        SMALL_RAW_COPPER = register("small_raw_copper");
        SMALL_RAW_GOLD = register("small_raw_gold");
        SMALL_DIAMOND = register("small_diamond");
        SMALL_EMERALD = register("small_emerald");
        Compat.addToIngredientsTab(List.of(
                SMALL_COAL, SMALL_RAW_IRON, SMALL_RAW_COPPER, SMALL_RAW_GOLD, SMALL_DIAMOND, SMALL_EMERALD));
    }

    private static Item register(String path) {
        return Compat.registerItem(path, new Item(Compat.itemProperties(path)));
    }
}
