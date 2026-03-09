package io.arona74.crlayersextras;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig config = ModConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("CRLayers Extras Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Enable Grass Spreading"), config.enableGrassSpreading)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Grass blocks spread to CR loamy dirt slabs"))
                    .setSaveConsumer(val -> config.enableGrassSpreading = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Enable Mycelium Spreading"), config.enableMyceliumSpreading)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Mycelium spreads to CR loamy dirt slabs"))
                    .setSaveConsumer(val -> config.enableMyceliumSpreading = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Sheep Eat Grass Layers"), config.enableSheepEatingGrassLayers)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Sheep can eat CR grass block layers"))
                    .setSaveConsumer(val -> config.enableSheepEatingGrassLayers = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Prevent Grass Block Decay"), config.preventGrassDecay)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Grass blocks never decay to dirt in darkness"))
                    .setSaveConsumer(val -> config.preventGrassDecay = val)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("Layers Fall with Sand/Gravel"), config.enableLayersFallWithSand)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("CR layer blocks above sand or gravel fall when the sand/gravel falls"))
                    .setSaveConsumer(val -> config.enableLayersFallWithSand = val)
                    .build());

            builder.setSavingRunnable(config::save);
            return builder.build();
        };
    }
}
