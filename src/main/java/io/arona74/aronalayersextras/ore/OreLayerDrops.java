package io.arona74.aronalayersextras.ore;

import io.arona74.aronalayersextras.Compat;
import io.arona74.aronalayersextras.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Drops for VanillaLayer+ ore layers ({@code vanillalayerplus:*_ore_layer}), mined with the right
 * tool and without Silk Touch: a share of the ore's normal drop and experience, proportional to the
 * layer count, so that eight layers are worth one ore block.
 *
 * <ul>
 *   <li>Single-item ores (coal, iron, copper, gold, diamond, emerald) drop their fragment item
 *       (SmallOreItems): per layer, as many fragments as the ore drops items. Eight fragments craft
 *       into one item, so copper's 2-5 raw copper per ore becomes 2-5 fragments per layer.</li>
 *   <li>Ores that already drop several small items (redstone, lapis, nether gold, nether quartz)
 *       drop those directly: one eighth of the ore's roll per layer, randomly rounded.</li>
 *   <li>Fortune applies as on the ore itself: the ore-drops multiplier for most, redstone's
 *       uniform bonus for redstone.</li>
 *   <li>Experience is one eighth of the ore's roll per layer, randomly rounded.</li>
 * </ul>
 *
 * Silk Touch keeps VanillaLayer+'s own drop (the layers themselves) so they can still be collected
 * for building, and anything that is not an ore layer is left to vanilla.
 */
public final class OreLayerDrops {
    private OreLayerDrops() {}

    private enum Bonus { ORE_DROPS, UNIFORM }

    /**
     * @param perLayer true: {@code min..max} items per layer (fragments). false: the ore's
     *                 {@code min..max} per block, shared out over its eight layers.
     */
    private record Ore(Supplier<Item> item, int min, int max, boolean perLayer, Bonus bonus, int xpMin, int xpMax) {}

    // Vanilla 1.20-26.x ore drop and experience ranges.
    private static final Map<String, Ore> ORES = Map.of(
            "coal", new Ore(() -> SmallOreItems.SMALL_COAL, 1, 1, true, Bonus.ORE_DROPS, 0, 2),
            "iron", new Ore(() -> SmallOreItems.SMALL_RAW_IRON, 1, 1, true, Bonus.ORE_DROPS, 0, 0),
            "copper", new Ore(() -> SmallOreItems.SMALL_RAW_COPPER, 2, 5, true, Bonus.ORE_DROPS, 0, 0),
            "gold", new Ore(() -> SmallOreItems.SMALL_RAW_GOLD, 1, 1, true, Bonus.ORE_DROPS, 0, 0),
            "diamond", new Ore(() -> SmallOreItems.SMALL_DIAMOND, 1, 1, true, Bonus.ORE_DROPS, 3, 7),
            "emerald", new Ore(() -> SmallOreItems.SMALL_EMERALD, 1, 1, true, Bonus.ORE_DROPS, 3, 7),
            "redstone", new Ore(() -> Items.REDSTONE, 4, 5, false, Bonus.UNIFORM, 1, 5),
            "lapis", new Ore(() -> Items.LAPIS_LAZULI, 4, 9, false, Bonus.ORE_DROPS, 2, 5),
            "nether_gold", new Ore(() -> Items.GOLD_NUGGET, 2, 6, false, Bonus.ORE_DROPS, 0, 1),
            "nether_quartz", new Ore(() -> Items.QUARTZ, 1, 1, false, Bonus.ORE_DROPS, 2, 5));

    private static final int LAYERS_PER_BLOCK = 8;

    private static final Map<Block, Optional<Ore>> BY_BLOCK = new ConcurrentHashMap<>();

    /**
     * Drops this ore layer's fragments and experience in place of its normal drop.
     *
     * @return true if handled (the caller must skip vanilla's drop), false to leave it to vanilla
     */
    public static boolean drop(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool) {
        if (!ModConfig.getInstance().enableSmallOreDrops) return false;
        Ore ore = BY_BLOCK.computeIfAbsent(state.getBlock(), OreLayerDrops::oreOf).orElse(null);
        if (ore == null || !state.hasProperty(BlockStateProperties.LAYERS)) return false;
        if (Compat.silkTouchLevel(level, tool) > 0) return false;

        int layers = state.getValue(BlockStateProperties.LAYERS);
        RandomSource random = level.getRandom();

        int rolled = 0;
        for (int i = 0; i < layers; i++) rolled += roll(random, ore.min(), ore.max());
        int count = ore.perLayer() ? rolled : roundRandomly((double) rolled / LAYERS_PER_BLOCK, random);

        int fortune = Compat.fortuneLevel(level, tool);
        if (fortune > 0) {
            if (ore.bonus() == Bonus.ORE_DROPS) {
                count *= Math.max(0, random.nextInt(fortune + 2) - 1) + 1;
            } else {
                int bonus = 0;
                for (int i = 0; i < layers; i++) bonus += random.nextInt(fortune + 1);
                count += roundRandomly((double) bonus / LAYERS_PER_BLOCK, random);
            }
        }

        Item item = ore.item().get();
        int stackSize = new ItemStack(item).getMaxStackSize();
        while (count > 0) {
            int n = Math.min(count, stackSize);
            Block.popResource(level, pos, new ItemStack(item, n));
            count -= n;
        }

        int xpRolled = 0;
        for (int i = 0; i < layers; i++) xpRolled += roll(random, ore.xpMin(), ore.xpMax());
        int xp = roundRandomly((double) xpRolled / LAYERS_PER_BLOCK, random);
        if (xp > 0 && Compat.doBlockDrops(level)) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), xp);
        }
        return true;
    }

    /** {@code vanillalayerplus:[deepslate_]<ore>_ore_layer} -> its ore entry. */
    private static Optional<Ore> oreOf(Block block) {
        String id = Compat.blockId(block);
        String prefix = "vanillalayerplus:";
        String suffix = "_ore_layer";
        if (!id.startsWith(prefix) || !id.endsWith(suffix)) return Optional.empty();
        String name = id.substring(prefix.length(), id.length() - suffix.length());
        if (name.startsWith("deepslate_")) name = name.substring("deepslate_".length());
        return Optional.ofNullable(ORES.get(name));
    }

    private static int roll(RandomSource random, int min, int max) {
        return max <= min ? min : min + random.nextInt(max - min + 1);
    }

    /** Integer part, plus one with probability equal to the fraction: right on average. */
    private static int roundRandomly(double value, RandomSource random) {
        int whole = (int) Math.floor(value);
        return whole + (random.nextDouble() < value - whole ? 1 : 0);
    }
}
