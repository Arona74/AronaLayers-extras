package io.arona74.aronalayersextras;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

/**
 * Holds every reference to Cloth Config. Loading this class links the Cloth
 * Config API, so it must only be touched once {@link ModMenuIntegration} has
 * confirmed Cloth Config is actually installed.
 */
final class ClothConfigScreenFactory {

    private ClothConfigScreenFactory() {
    }

    static ConfigScreenFactory<?> create() {
        return parent -> {
            ModConfig config = ModConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Arona Layers Extras Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Enable Grass Spreading"), config.enableGrassSpreading)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Grass blocks spread to CR loamy dirt slabs"))
                    .setSaveConsumer(val -> config.enableGrassSpreading = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Enable Mycelium Spreading"), config.enableMyceliumSpreading)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Mycelium spreads to CR loamy dirt slabs"))
                    .setSaveConsumer(val -> config.enableMyceliumSpreading = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Sheep Eat Grass Layers"), config.enableSheepEatingGrassLayers)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Sheep can eat CR grass block layers"))
                    .setSaveConsumer(val -> config.enableSheepEatingGrassLayers = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Prevent Grass Block Decay"), config.preventGrassDecay)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Grass blocks never decay to dirt in darkness"))
                    .setSaveConsumer(val -> config.preventGrassDecay = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Layers Fall with Sand/Gravel"), config.enableLayersFallWithSand)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("CR layer blocks above sand or gravel fall when the sand/gravel falls"))
                    .setSaveConsumer(val -> config.enableLayersFallWithSand = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Wet Sand Near Water"), config.enableWetSands)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("CR sand blocks touching water turn into their wet variant, and dry back out when the water is gone. The block pairs live under 'WetSandMappings' in the JSON config."))
                    .setSaveConsumer(val -> config.enableWetSands = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Small Ores from Ore Layers"), config.enableSmallOreDrops)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("VanillaLayer+ ore layers mined without Silk Touch drop small ores (small raw iron, small diamond...) and experience in proportion to their layers, instead of the layers themselves. Eight small ores craft into the normal ore drop."))
                    .setSaveConsumer(val -> config.enableSmallOreDrops = val)
                    .build());


            builder.setSavingRunnable(config::save);
            return builder.build();
        };
    }
}
