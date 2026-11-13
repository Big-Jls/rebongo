# Changelog

## 26.2-1.0.0-port.11 — 2026-11-14

### Fixed

- **Bucket item tasks**: tropical fish buckets store their variant in the
  separate `TROPICAL_FISH_PATTERN`, `TROPICAL_FISH_BASE_COLOR` and
  `TROPICAL_FISH_PATTERN_COLOR` components rather than `BUCKET_ENTITY_DATA`,
  so `tropical_fish_bucket` tasks still never matched. Item task matching now
  also strips `SALMON_SIZE` (salmon bucket) and `AXOLOTL_VARIANT` (axolotl
  bucket) alongside the previously handled bucket components.

## 26.2-1.0.0-port.10 — 2026-11-14

### Fixed

- **Sulfur cube bucket tasks**: a sulfur cube bucket that has absorbed a block
  also carries a `SULFUR_CUBE_CONTENT` component, so `sulfur_cube_bucket` tasks
  still never matched (the port.9 fix only ignored `BUCKET_ENTITY_DATA`). Item
  task matching now ignores both bucket variant components, covering every mob
  bucket (tadpole included) as well as sulfur cube buckets.

## 26.2-1.0.0-port.9 — 2026-11-13

### Fixed

- **Mob bucket item tasks**: salmon/cod/pufferfish/tropical fish/axolotl/tadpole
  buckets carry per-fish entity data, so a plain `salmon_bucket` task could
  never match the item in a player's inventory. Item task matching now ignores
  `BUCKET_ENTITY_DATA` when either side has it, both when counting inventory
  items and when consuming.
- **Tab list team colors**: the dye-to-TeamColor table mapped two pairs of
  distinct team colors to the same tab color (orange+brown -> gold,
  magenta+pink -> light purple), so different teams looked identical in the
  tab list. All 16 dye colors now map to 16 distinct TeamColors (pink -> dark
  red, brown -> dark blue).

## 26.2-1.0.0-port.8 — 2026-11-12

### Fixed

- **Tab list team colors**: MC 26.2 no longer fires `TabListNameFormat`, so tab
  names were never colored. Bingo now maintains a vanilla scoreboard team per
  Bingo team (`bingo_<color>`) and syncs membership every second; empty teams
  are removed after the game stops.
- **Music disc task names**: all disc items share the name "Music Disc", so
  task cards and messages were indistinguishable. Item task names now append
  the disc id, e.g. `Music Disc (13)` / `音乐唱片 (13)`.

## 26.2-1.0.0-port.7 — 2026-11-12

### Fixed

- Win condition names in the Bingo overlay no longer show raw translation keys
  such as `bongo.wc.bongo.row_and_column`. After the port renamed win condition
  ids from `bingo.*` to `bongo.*`, the overlay composed `bongo.wc.` + id while
  the language files still used `bongo.wc.bingo.*`. All five bundled languages
  now use `bongo.wc.bongo.*`.

## 26.2-1.0.0-port.6 — 2026-11-11

### Packaging

- Build artifact renamed from `Bingo-<version>.jar` to
  `ReBongo-<version>.jar`, matching the project and in-game display name.
- The mod id is still `bongo`. Remove any old `Bingo-*.jar` before installing
  this version so the same mod id is not loaded from two jars.

## 26.2-1.0.0-port.5 — 2026-11-10

### Fixed

- Entity task icons no longer turn into a spawner (or vanish) when the world
  difficulty is peaceful: render entities are now created with
  `EntitySpawnRequest(..., ignoreChecks = true)`, so the difficulty check in
  `EntityType#create` can no longer return `null` for hostile mobs.
- When an entity really cannot be created or rendered, the icon now falls back
  to the matching spawn egg instead of a generic spawner, and a warning with
  the entity type (and stack trace, if rendering threw) is written to the log
  once per type.

## 26.2-1.0.0-port.4 — 2026-11-10

### Fixed

- Registered `GameTasksArgument` and `GameSettingsArgument` with
  `ArgumentTypeInfos.registerByClass`. Without this the command tree could not
  be serialised for players, so joining a world/server failed with
  `IllegalArgumentException: Unrecognized argument type` and the player was
  disconnected with "Invalid player data".

## 26.2-1.0.0-port.3 — 2026-11-09

### Changed

- Team backpacks now have 54 slots instead of 27 (`/bingo bp` renders a
  six-row chest). Existing backpacks keep their items; the extra slots start
  empty.

## 26.2-1.0.0-port.2 — 2026-11-09

### Added

- Bingo cards now keep the task weight they were drawn with. `Task` exposes
  `weight()` / `withWeight(int)` and serialises it as the optional
  `card_weight` field (default `1`), so addons can identify the hardest task
  on a running card without re-reading the task set definition.

## 26.2-1.0.0-port.1 — 2026-11-08

First public source release of the unofficial NeoForge 26.2 port.

### Port

- Ported the Forge 1.20.1 code base to Minecraft 26.2 / NeoForge 26.2.0.93.
- Restored the upstream mod id `bongo`, resource namespaces `assets/bongo` and
  `data/bongo`, translation keys `bongo.*`, task/teleporter ids `bongo.*`, and
  network payload ids under the `bongo` namespace.
- Kept the upstream command tree: the root command is still `/bingo` (plus the
  `/bp` and `/tc` aliases).
- `libx` dependency raised to `[26.2-8.0.0,)`; verified against the official
  LibX 26.2-8.0.0 build.

### API / behaviour fixes required by 26.2

- `SavedDataType` now receives a `DataFixTypes` argument.
- Advancement reset rewritten to iterate `AdvancementHolder`s and revoke
  criteria through `PlayerAdvancements` (the old
  `AdvancementCommands.Action.REVOKE` helper is no longer public).
- Command argument types are registered only through `RegisterEvent`
  (`ArgumentTypeInfos.registerByClass` is gone).
- `PendingComponents` are built from `AddServerReloadListenersEvent`'s
  `RegistryAccess`.
- Statistic enumeration rewritten around `StatType` iteration.
- Player display-name refresh calls removed (the 26.2 API no longer exposes
  them); team names still update through the Bingo payloads.
- Codec builders in the generated `BongoMod$` class given explicit generic
  types, and `TagWithCount` / `DynamicUtil` / `Task` / `TaskTypeEntity`
  adjusted for stricter generics.
- `DumpCommand` now passes a `Supplier<Component>` to `sendSuccess`.

### Added

- `TraderSpawnPatch`: resets the wandering trader spawn delay at game start and
  once per minute while a round is running, so short rounds can see traders.

### Removed / not ported

- Optional integrations (Curios, MineMention, SkyblockBuilder, Waila, JEI
  bookmark plugin) and `CrownRenderer` are not part of this source tree.
- The Forge-era `accesstransformer.cfg` is dropped; the remaining reflection
  (`StatsCounter.stats`) uses `setAccessible(true)`.

### Compatibility

- Saved data is keyed on the `bongo` id again. Saves created while the mod used
  another id will not be read; start a new world or run `/bingo create` again.
