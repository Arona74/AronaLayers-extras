# Arona Layers Extras

A Fabric mod that makes layer blocks from **Conquest Reforged** and **VanillaLayer+** behave like their vanilla counterparts — grass spreads onto them, sheep eat them, they fall with sand, and plants sit flush on top of them.

Supports **Minecraft 1.20.1, 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3** from a single shared codebase.

## Overview

Layer blocks are sub-block-height blocks used to smooth out terrain. Out of the box the game treats them as inert decoration: grass will not spread onto them, sheep ignore them, sand falls straight past them, and plants placed on top float in mid-air.

This mod closes those gaps. It needs a **layer block provider** to be useful:

| Provider | Notes |
|----------|-------|
| **Conquest Reforged** | `grass_block_layer`, `loamy_dirt_slab`, `mycelium_layer` and the rest of the CR layer set |
| **VanillaLayer+** | `grass_layer`, `dirt_layer`, `mycelium_layer` |

Both can be installed at once — each feature activates only for the blocks it finds. With neither installed the mod loads and does nothing harmful.

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
- Conquest's decorative ground rocks (`conquest:limestone_rocks`, `conquest:andesite_rocks` and the rest of the `*_rocks` family) fall too
- Cascades upward through multiple stacked layer blocks
- Applies to `minecraft:sand`, `minecraft:red_sand`, and `minecraft:gravel`
- Also triggers when a player breaks sand/gravel directly (layers above fall immediately)
- Anything else can be added with `AdditionalFallingBlocks`
- A block that lands on a partial-height block is placed on top of it rather than breaking, and is waterlogged if it settles in water

> **Why the landing needs help.** A falling block comes to rest wherever its collision box stops, and Minecraft rounds that down to a block position. Land on a full block and that position is the empty space above it; land on a layer, slab or rock and it is *inside* the block just hit, which is not replaceable — so vanilla discards the block and pops it as an item. It is the same reason vanilla sand breaks when it falls onto a slab. Blocks this mod launched are placed one block higher instead; vanilla sand and gravel keep their normal behaviour.

> Rocks are matched on the `_rocks` suffix, which is plural throughout Conquest and only ever used for the loose ground clutter — the solid blocks they are cut from are singular (`conquest:mudstone`, `conquest:dark_tuff`), so they stay put. New Conquest stone types are picked up without a mod update.

### 6. Wet Sand Near Water (Conquest Reforged)

- Sand touching water turns into the Conquest wet variant — `minecraft:sand` → `conquest:wet_sand`, `conquest:tan_sand_layer` → `conquest:wet_tan_sand_layer`, and so on
- "Touching water" means water on any of the six faces, a waterlogged neighbour, or the block being waterlogged itself
- Wet sand with no water left on any face dries back into its dry counterpart
- Layer properties (`layers`, `waterlogged`) carry across the swap
- The block pairs are config data (`WetSandMappings`), not hardcoded — add your own, or delete pairs you do not want

> **Two things worth knowing before you leave this on.** It converts in bulk: every shoreline, river bed and shallow sea floor near a player is rewritten as chunks load, and the drying direction applies to wet sand you placed deliberately — park `conquest:wet_sand` in a dry desert and it will become `minecraft:sand`, which then *falls*. If you want wet sand as plain decoration away from water, drop the `"minecraft:sand"` pair from `WetSandMappings` (or turn `enableWetSands` off) rather than fighting it.

> Only near-surface blocks are scanned — 32 blocks down from the top of each column. Sand at the bottom of a deep ocean, or sealed in a cave next to a waterlogged block, is left alone.

### 7. Small Ores from Ore Layers (VanillaLayer+)

- VanillaLayer+ ore layers mined without Silk Touch drop **small ores** and experience in proportion to their layers, instead of the layers themselves
- Eight small ores craft (ring of 8 in a crafting table) into the normal drop, so a full 8-layer stack is worth exactly one ore block

| Ore layer | Drop per layer | Crafts into |
|-----------|----------------|-------------|
| coal, iron, gold, diamond, emerald (stone and deepslate) | 1 Small Coal / Raw Iron / Raw Gold / Diamond / Emerald | 8 → 1 coal / raw iron / raw gold / diamond / emerald |
| copper (stone and deepslate) | 2–5 Small Raw Copper (copper ore drops 2–5) | 8 → 1 raw copper |
| redstone, lapis, nether gold, nether quartz | one eighth of the ore's vanilla drop (redstone, lapis, gold nuggets, quartz), randomly rounded | — |

- Fortune works as on the ore itself; experience is one eighth of the ore's per layer
- Silk Touch keeps dropping the layers, so they can still be collected for building
- The small ores reuse the vanilla item textures, drawn as a small pile of three pieces — no new textures
- Toggle with `enableSmallOreDrops`

> **Plant visual offset has moved.** Since 1.3.0, lowering plants onto layer blocks is part of [AronaLayers-gen](https://github.com/Arona74/AronaLayers-gen) 1.3.0+ (`plant_grounding`), which detects plants automatically and also lowers their selection and collision boxes. Arona Layers Extras 1.3.0 no longer has the offset, and gen 1.3.0 refuses to load beside an older Extras so plants are never lowered twice.

## Requirements

| Minecraft | Fabric Loader | Fabric API | Java |
|-----------|---------------|------------|------|
| 1.20.1 | 0.15.0+ | 0.92.2+ | 17 |
| 1.21.1 | 0.16.0+ | 0.104.0+ | 21 |
| 1.21.11 | 0.19.0+ | 0.141.5+ | 21 |
| 26.1.2 | 0.19.0+ | 0.154.2+ | 25 |
| 26.2 | 0.19.0+ | 0.156.0+ | 25 |
| 26.3 | 0.19.0+ | 0.162.0+ | 25 |

Plus a layer block provider:

- [Conquest Reforged](https://www.curseforge.com/minecraft/mc-mods/conquest-reforged)
- VanillaLayer+

> Availability differs per Minecraft version. Conquest Reforged and VanillaLayer+ do not exist on every supported version — check before planning a setup. Without a provider the layer features simply stay inactive; nothing breaks.

### Optional (recommended)

- [Mod Menu](https://modrinth.com/mod/modmenu) — in-game config screen
- [Cloth Config](https://modrinth.com/mod/cloth-config) — draws that config screen

> Neither is required. They are a pair: install **both** for the in-game config screen, or **neither** and edit `config/aronalayersextras.json` by hand. With Mod Menu but no Cloth Config, the mod loads normally and simply offers no config button — it logs one line saying so.

## Installation

1. Install Fabric Loader and Fabric API for your Minecraft version (see the table above)
2. Install Conquest Reforged and/or VanillaLayer+
3. Optionally install Mod Menu **and** Cloth Config together, for the in-game config screen
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
| `enableLayersFallWithSand` | `true` | CR/VLP layer, slab and rock blocks fall when sand/gravel falls or is broken |
| `AdditionalFallingBlocks` | `[]` | Extra block IDs that should also fall with the sand/gravel below them. JSON-only; picked up on the next launch |
| `enableWetSands` | `true` | CR sand blocks touching water turn wet, and dry back when the water goes |
| `WetSandMappings` | the six pairs below | Dry block → wet block pairs, read in both directions. Pairs whose blocks are not both registered are ignored |
| `enableSmallOreDrops` | `true` | VanillaLayer+ ore layers drop small ores and experience per layer instead of the layers (Silk Touch keeps the layers) |

`WetSandMappings` defaults to:

```json
"WetSandMappings": {
  "minecraft:sand": "conquest:wet_sand",
    "conquest:sand_layer": "conquest:wet_sand_layer",
    "conquest:sand_and_gravel_sand": "conquest:wet_sand_and_gravel_sand",
    "conquest:sand_and_gravel_sand_layer": "conquest:wet_sand_and_gravel_sand_layer",
    "conquest:sand_and_vegetation_sand": "conquest:wet_sand_and_vegetation_sand",
    "conquest:sand_and_vegetation_sand_layer": "conquest:wet_sand_and_vegetation_sand_layer",
    "conquest:river_sand": "conquest:wet_river_sand",
    "conquest:river_sand_layer": "conquest:wet_river_sand_layer",
    "conquest:gray_sand": "conquest:wet_gray_sand",
    "conquest:gray_sand_layer": "conquest:wet_gray_sand_layer",
    "conquest:light_gray_sand": "conquest:wet_light_gray_sand",
    "conquest:light_gray_sand_layer": "conquest:wet_light_gray_sand_layer",
    "conquest:lime_sand": "conquest:wet_lime_sand",
    "conquest:lime_sand_layer": "conquest:wet_lime_sand_layer",
    "conquest:pink_sand": "conquest:wet_pink_sand",
    "conquest:pink_sand_layer": "conquest:wet_pink_sand_layer",
    "conquest:purple_sand": "conquest:wet_purple_sand",
    "conquest:purple_sand_layer": "conquest:wet_purple_sand_layer",
    "conquest:brown_sand": "conquest:wet_brown_sand",
    "conquest:brown_sand_layer": "conquest:wet_brown_sand_layer",
    "conquest:tan_sand": "conquest:wet_tan_sand",
    "conquest:tan_sand_layer": "conquest:wet_tan_sand_layer"
}
```

Edits to it are picked up on the next launch.

### Mod Menu Support

Both mods are optional. With [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) installed, most settings can be changed in-game through the Mod Menu config screen; without them, every setting is still available by editing the JSON. Changes take effect immediately without restarting. `WetSandMappings` is JSON-only; the `enableWetSands` toggle beside it is not.

## Building from Source

```
./gradlew build
```

Each Minecraft version is a Gradle subproject sharing one source tree (`src/`), with version-specific code in the subproject's own `src/`. JARs land in each subproject's `build/libs/`:

```
v1_20_1/build/libs/    v1_21_1/build/libs/    v1_21_11/build/libs/
v26_1_2/build/libs/    v26_2/build/libs/      v26_3/build/libs/
```

Build a single version with `./gradlew :v1_21_1:build`.

> Changing `fabric_version` in a subproject also needs `.gradle/loom-cache/minecraftMaven/**/<mc-version>/` deleted. Loom bakes Fabric's injected interfaces into the cached Minecraft jar and does not rebuild it when only the API version changes, so the jar keeps interfaces from the old Fabric API. Nothing warns: you get `cannot access <SomeInterface>` on an unrelated line — `sheep.level()`, say — because javac cannot complete the class hierarchy.

> If the repository sits in a synced folder such as OneDrive, `build/` can get file-locked — Gradle then reports `Unable to delete directory` or `Failed to clean up stale outputs`, and in the worst case `BUILD SUCCESSFUL` while leaving the *previous* JAR in place. Build with `-PbuildDirRoot=<path>` (or set `ARONALAYERS_BUILD_DIR`) to move outputs elsewhere, or check the JAR timestamp before trusting a test.

`scripts/verify_mixins.py` checks that every mixin in a built JAR resolves against its Minecraft version:

```
python scripts/verify_mixins.py v1_20_1 v1_21_1 v1_21_11 v26_1_2 v26_2 v26_3
```

Worth running before releasing. An unresolvable mixin target still builds successfully and only fails when the game loads — and on the 26.x versions, which ship deobfuscated, nothing is remapped, so the build cannot warn about it at all.

`scripts/mapping-tools/` holds the one-off scripts used to port across Minecraft versions — deriving renames by joining mapping sets through intermediary rather than guessing them one compiler error at a time. See its [README](scripts/mapping-tools/README.md).

## A note on how this fork is developed

I use Claude AI as a development aid, mainly for the repetitive parts of multi-version maintenance, tracking down what a renamed Minecraft API became, debugging, etc.

That is where the help stops. I decide what goes in, I read the changes, and I test in-game before releasing. Fixes get re-tested after the fact, not assumed.

I am saying this because "AI-assisted" often means code nobody checked. That is not what this is. If you find a bug anyway, please open an issue — I would rather hear about it.

## License

MIT License - See LICENSE file for details

## Credits

Created by Arona74
