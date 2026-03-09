# CRLayers Extras

A Fabric mod for Minecraft 1.20.1 that makes Conquest Reforged layer blocks behave like their vanilla counterparts.

## Features

This mod adds natural mechanics to Conquest Reforged layer blocks:

### 1. Grass Spreading
- `conquest:grass_block_layer` now spreads to `conquest:loamy_dirt_slab` blocks
- `minecraft:grass_block` also spreads to nearby `conquest:loamy_dirt_slab` blocks
- Mimics vanilla `minecraft:grass_block` spreading to `minecraft:dirt`
- Requires proper light levels (light level 9+ at source, 4+ at target)

### 2. Mycelium Spreading
- `conquest:mycelium_layer` now spreads to `conquest:loamy_dirt_slab` blocks
- Mimics vanilla `minecraft:mycelium` spreading to `minecraft:dirt`
- Can spread in any light level (just like vanilla mycelium)

### 3. Sheep Grass Eating
- Sheep can now eat `conquest:grass_block_layer` blocks
- Converts them to `conquest:loamy_dirt_slab` (similar to vanilla grass → dirt conversion)
- Sheep will regrow their wool after eating, just like in vanilla

### 4. Prevent Grass Block Decay
- Prevents `minecraft:grass_block` from turning into dirt when covered by a block (light level 0)
- Useful for building with grass blocks underground or under structures

### 5. Layer Blocks Fall with Sand/Gravel
- When a sand or gravel block falls, any CR layer/slab blocks directly above it also fall
- Cascades upward through multiple stacked layer blocks
- Applies to `minecraft:sand`, `minecraft:red_sand`, and `minecraft:gravel`
- Also triggers when a player breaks sand/gravel directly (layers above fall immediately)

### 6. Plant Visual Offset on Layer Blocks
- Plants and flowers placed on top of partial-height CR layer blocks are shifted downward visually to sit flush with the layer surface
- Works for tall plants too (both the lower and upper half are offset correctly)
- Applies to vanilla plants by default (flowers, tall grass, saplings, mushrooms, etc.)
- Extra plants from other mods (e.g. Conquest Reforged's `conquest:seagrass`) can be added via the config

## Configuration

All features can be toggled on or off individually. The config file is located at `config/crlayers-extras.json` and is created automatically on first launch.

### Config options

| Key | Default | Description |
|-----|---------|-------------|
| `enableGrassSpreading` | `true` | Grass spreads to CR loamy dirt slabs |
| `enableMyceliumSpreading` | `true` | Mycelium spreads to CR loamy dirt slabs |
| `enableSheepEatingGrassLayers` | `true` | Sheep can eat CR grass block layers |
| `preventGrassDecay` | `true` | Grass blocks never decay to dirt in darkness |
| `enableLayersFallWithSand` | `true` | CR layer blocks fall when sand/gravel falls or is broken |
| `enableBlockOffset` | `true` | Plants are visually shifted down on partial-height layer blocks |
| `VanillaBlockOffset` | `true` | Vanilla plants (flowers, grass, saplings, etc.) receive the visual offset. Set to `false` to restrict offset to blocks listed in `AdditionalOffsetBlocks` only. **Requires resource reload (F3+T) to take effect.** |
| `AdditionalOffsetBlocks` | `["conquest:seagrass", "conquest:tall_seagrass", "minecraft:pink_petals"]` | Extra plant block IDs from other mods that should also receive the visual offset. **Requires resource reload (F3+T) to take effect.** |

### Mod Menu Support
If you have [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, you can change most settings in-game through the Mod Menu config screen. Changes take effect immediately without restarting (except `VanillaBlockOffset` or `AdditionalOffsetBlocks`, which requires F3+T).

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your `.minecraft/mods` folder
3. Make sure you have Fabric Loader and Fabric API installed
4. Works best alongside Conquest Reforged mod

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
