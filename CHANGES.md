# Changelog

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
