# Arona Layers Extras

A Fabric mod for Minecraft 1.20.1 that makes layer blocks from **Conquest Reforged** and **VanillaLayer+** behave like their vanilla counterparts.

## Features

### 1. Grass Spreading
- `conquest:grass_block_layer` spreads to `conquest:loamy_dirt_slab`
- `vanillalayerplus:grass_layer` spreads to `vanillalayerplus:dirt_layer`
- `minecraft:grass_block` also spreads to both of the above
- Requires proper light levels (light level 9+ at source, 4+ at target)

### 2. Mycelium Spreading
- `conquest:mycelium_layer` spreads to `conquest:loamy_dirt_slab`
- `vanillalayerplus:mycelium_layer` spreads to `vanillalayerplus:dirt_layer`
- Mimics vanilla mycelium — can spread at any light level

### 3. Sheep Grass Eating
- Sheep can eat `conquest:grass_block_layer` (converts to `conquest:loamy_dirt_slab`)
- Sheep can eat `vanillalayerplus:grass_layer` (converts to `vanillalayerplus:dirt_layer`)
- Sheep regrow wool after eating, just like in vanilla

### 4. Prevent Grass Block Decay
- Prevents `minecraft:grass_block` from turning into dirt when covered (light level 0)
- Useful for building with grass blocks underground or under structures

### 5. Layer Blocks Fall with Sand/Gravel
- When sand or gravel falls, CR and VanillaLayer+ layer/slab blocks directly above also fall
- Cascades upward through multiple stacked layer blocks
- Applies to `minecraft:sand`, `minecraft:red_sand`, and `minecraft:gravel`
- Also triggers when a player breaks sand/gravel directly (layers above fall immediately)

### 6. Plant Visual Offset on Layer Blocks
- Plants and flowers placed on partial-height layer blocks are shifted downward visually to sit flush with the layer surface
- Works for tall plants too (both lower and upper half are offset correctly)
- Applies to vanilla plants by default (flowers, tall grass, saplings, mushrooms, etc.)
- Extra plants from other mods (e.g. `conquest:seagrass`) can be added via the config

## Configuration

All features can be toggled on or off individually. The config file is located at `config/aronalayersextras.json` and is created automatically on first launch.

### Config options

| Key | Default | Description |
|-----|---------|-------------|
| `enableGrassSpreading` | `true` | Grass spreads to CR/VLP dirt layer equivalents |
| `enableMyceliumSpreading` | `true` | Mycelium spreads to CR/VLP dirt layer equivalents |
| `enableSheepEatingGrassLayers` | `true` | Sheep can eat CR/VLP grass layer blocks |
| `preventGrassDecay` | `true` | Grass blocks never decay to dirt in darkness |
| `enableLayersFallWithSand` | `true` | CR/VLP layer blocks fall when sand/gravel falls or is broken |
| `enableBlockOffset` | `true` | Plants are visually shifted down on partial-height layer blocks |
| `VanillaBlockOffset` | `true` | Vanilla plants receive the visual offset. Set to `false` to restrict offset to `AdditionalOffsetBlocks` only. **Requires F3+T to take effect.** |
| `AdditionalOffsetBlocks` | `["conquest:seagrass", "conquest:tall_seagrass", "minecraft:pink_petals"]` | Extra plant block IDs from other mods that also receive the visual offset. **Requires F3+T to take effect.** |

### Mod Menu Support
If you have [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, you can change most settings in-game through the Mod Menu config screen. Changes take effect immediately without restarting (except `VanillaBlockOffset` and `AdditionalOffsetBlocks`, which require F3+T).

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your `.minecraft/mods` folder
3. Make sure you have Fabric Loader and Fabric API installed
4. Works alongside Conquest Reforged and/or VanillaLayer+ (features activate only for installed mods)

### Optional Dependencies
- [Mod Menu](https://modrinth.com/mod/modmenu) - for in-game config screen
- [Cloth Config](https://modrinth.com/mod/cloth-config) - required for the config screen

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/`

## Dependencies

- Minecraft 1.20.1
- Fabric Loader 0.15.0+
- Fabric API
- Java 17+

## License

MIT License - See LICENSE file for details

## Credits

Created by Arona74
