# Arona Layers Extras

A Fabric mod that makes layer blocks from **Conquest Reforged** and **VanillaLayer+** behave like their vanilla counterparts — grass spreads onto them, sheep eat them, they fall with sand, and plants sit flush on top of them.

Supports **Minecraft 1.20.1, 1.21.1, 1.21.11 and 26.2** from a single shared codebase.

## Overview

Layer blocks are sub-block-height blocks used to smooth out terrain. Out of the box the game treats them as inert decoration: grass will not spread onto them, sheep ignore them, sand falls straight past them, and plants placed on top float in mid-air.

This mod closes those gaps. It needs a **layer block provider** to be useful:

| Provider | Notes |
|----------|-------|
| **Conquest Reforged** | `grass_block_layer`, `loamy_dirt_slab`, `mycelium_layer` and the rest of the CR layer set |
| **VanillaLayer+** | `grass_layer`, `dirt_layer`, `mycelium_layer` |

Both can be installed at once — each feature activates only for the blocks it finds. With neither installed the mod loads and does nothing harmful; the plant visual offset still works on vanilla partial-height blocks such as snow layers.

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
- Purely visual — it does not change collision, placement rules or what the server thinks is there

> **1.21.11 and 26.2 pick plants by block type** (anything extending `VegetationBlock`), so plants added by newer Minecraft versions — `bush`, `firefly_bush`, `leaf_litter`, `wildflowers`, `short_dry_grass`, `cactus_flower` and so on — are handled automatically. 1.20.1 and 1.21.1 use a fixed list of the plants that exist on those versions. `AdditionalOffsetBlocks` works the same way on all four.

## Requirements

| Minecraft | Fabric Loader | Fabric API | Java |
|-----------|---------------|------------|------|
| 1.20.1 | 0.15.0+ | 0.92.2+ | 17 |
| 1.21.1 | 0.16.0+ | 0.104.0+ | 21 |
| 1.21.11 | 0.19.0+ | 0.141.5+ | 21 |
| 26.2 | 0.19.0+ | 0.156.0+ | 25 |

The plant visual offset is drawn through the Fabric Rendering API, so a provider for that API is required:

| Minecraft | Renderer requirement |
|-----------|----------------------|
| 1.20.1 | [Indium](https://modrinth.com/mod/indium) — Sodium is still 0.5.x here, which does not implement the Fabric Rendering API |
| 1.21.1, 1.21.11, 26.2 | [Sodium](https://modrinth.com/mod/sodium) 0.6.0+ — it implements the API natively |

> **Do not install Indium on 1.21.1 or newer.** It is unnecessary there and is not compatible with Sodium 0.6.0+.

Plus a layer block provider:

- [Conquest Reforged](https://www.curseforge.com/minecraft/mc-mods/conquest-reforged)
- VanillaLayer+

> Availability differs per Minecraft version. Conquest Reforged and VanillaLayer+ do not exist on every supported version — check before planning a setup. Without a provider the layer features simply stay inactive; nothing breaks.

### Optional

- [Mod Menu](https://modrinth.com/mod/modmenu) — in-game config screen
- [Cloth Config](https://modrinth.com/mod/cloth-config) — required by the config screen

## Installation

1. Install Fabric Loader and Fabric API for your Minecraft version (see the table above)
2. Install the renderer requirement for your version — Indium on 1.20.1, Sodium 0.6.0+ on 1.21.1 and newer
3. Install Conquest Reforged and/or VanillaLayer+
4. Place the mod JAR **for your Minecraft version** in your mods folder
5. Launch once to generate `config/aronalayersextras.json`

## Configuration

All features can be toggled individually. The config file is `config/aronalayersextras.json`, created automatically on first launch.

| Key | Default | Description |
|-----|---------|-------------|
| `enableGrassSpreading` | `true` | Grass spreads to CR/VLP dirt layer equivalents |
| `enableMyceliumSpreading` | `true` | Mycelium spreads to CR/VLP dirt layer equivalents |
| `enableSheepEatingGrassLayers` | `true` | Sheep can eat CR/VLP grass layer blocks |
| `preventGrassDecay` | `true` | Grass blocks never decay to dirt in darkness |
| `enableLayersFallWithSand` | `true` | CR/VLP layer blocks fall when sand/gravel falls or is broken |
| `enableBlockOffset` | `true` | Plants are visually shifted down on partial-height layer blocks |
| `VanillaBlockOffset` | `true` | Vanilla plants receive the visual offset. Set to `false` to restrict the offset to `AdditionalOffsetBlocks` only. **Requires F3+T to take effect.** |
| `AdditionalOffsetBlocks` | `["conquest:seagrass", "conquest:tall_seagrass", "minecraft:pink_petals"]` | Extra plant block IDs from other mods that also receive the visual offset. **Requires F3+T to take effect.** |

### Mod Menu Support

With [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, most settings can be changed in-game through the Mod Menu config screen. Changes take effect immediately without restarting — except `VanillaBlockOffset` and `AdditionalOffsetBlocks`, which are applied at model bake time and need a resource reload (F3+T).

## Building from Source

```
./gradlew build
```

Each Minecraft version is a Gradle subproject sharing one source tree (`src/`), with version-specific code in the subproject's own `src/`. JARs land in each subproject's `build/libs/`:

```
v1_20_1/build/libs/    v1_21_1/build/libs/    v1_21_11/build/libs/    v26_2/build/libs/
```

Build a single version with `./gradlew :v1_21_1:build`.

> If the repository sits in a synced folder such as OneDrive, `build/` can get file-locked — Gradle then reports `Unable to delete directory` or `Failed to clean up stale outputs`, and in the worst case `BUILD SUCCESSFUL` while leaving the *previous* JAR in place. Build with `-PbuildDirRoot=<path>` (or set `ARONALAYERS_BUILD_DIR`) to move outputs elsewhere, or check the JAR timestamp before trusting a test.

`scripts/verify_mixins.py` checks that every mixin in a built JAR resolves against its Minecraft version:

```
python scripts/verify_mixins.py v1_20_1 v1_21_1 v1_21_11 v26_2
```

Worth running before releasing. An unresolvable mixin target still builds successfully and only fails when the game loads — and on 26.2, which ships deobfuscated, nothing is remapped, so the build cannot warn about it at all.

`scripts/mapping-tools/` holds the one-off scripts used to port across Minecraft versions — deriving renames by joining mapping sets through intermediary rather than guessing them one compiler error at a time. See its [README](scripts/mapping-tools/README.md).

## A note on how this fork is developed

I use Claude AI as a development aid, mainly for the repetitive parts of multi-version maintenance, tracking down what a renamed Minecraft API became, debugging, etc.

That is where the help stops. I decide what goes in, I read the changes, and I test in-game before releasing. Fixes get re-tested after the fact, not assumed.

I am saying this because "AI-assisted" often means code nobody checked. That is not what this is. If you find a bug anyway, please open an issue — I would rather hear about it.

## License

MIT License - See LICENSE file for details

## Credits

Created by Arona74
