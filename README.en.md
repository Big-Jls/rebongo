# ReBongo — Unofficial NeoForge 26.2 Port of Bongo

> **Upstream projects**
> - Bongo — <https://github.com/noeppi-noeppi/Bongo>
> - BingoLobby — <https://github.com/noeppi-noeppi/BingoLobby>
> - LibX — <https://github.com/ModdingX/LibX>
>
> **Unofficial project**: this is a community-maintained port, not an official release. It is not affiliated with, endorsed by, or sponsored by the original author **noeppi_noeppi**.
>
> **License**: Apache License 2.0. Some assets/code originate from Botania and follow the [Botania License](https://botaniamod.net/license.php); see [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
>
> **Source**: this repository contains the complete source. Under the copyleft terms of the Botania License, this project stays open source, allows redistribution and modification, and is distributed free of charge.

## What is this?

Bongo is a multiplayer Bingo minigame: players form teams and complete tasks on a Bingo card (collect items, finish advancements, kill entities, visit biomes, ...). The first team to complete 5 tasks in a row, a column, or a diagonal wins.

This repository ports the Forge / Minecraft 1.20.1-era Bongo code base to **Minecraft 26.2 / NeoForge**, keeping the upstream mod id and namespaces:

- `modId = bongo`
- resource namespaces `assets/bongo`, `data/bongo`
- the `/bingo` command root (same as upstream)

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 26.2 |
| NeoForge | 26.2.0.93 or newer |
| Java | 25 |
| LibX | **26.2-8.0.0** (official release, required; install on both client and server) |

Use the official LibX release (CurseForge file ID 9409485). This repository does not bundle or redistribute LibX.

## Installation

1. Install NeoForge 26.2.0.93+.
2. Put `ReBongo-26.2-1.0.0-port.11.jar` and `LibX-26.2-8.0.0.jar` into `mods`.
   > Since `26.2-1.0.0-port.6` the artifact is named `ReBongo-<version>.jar`. Remove old `Bingo-*.jar` files when upgrading, otherwise the same `modId = bongo` will be loaded twice.
3. Install on both client and server.
4. In game, an operator runs `/bingo create <tasks_id> <settings_id>` to create a game.

## Commands

| Command | Description |
|---|---|
| `/bingo create <tasks_id> <settings_id>` | Create a game |
| `/bingo join <team>` | Join a team (16 colors) |
| `/bingo leave` | Leave the team |
| `/bingo start` | Start the game (clears inventories and progress) |
| `/bingo stop` | Stop the game |
| `/bingo spread <teams>` | Split players into teams automatically |
| `/bingo teams` | List teams and members |
| `/bingo backpack` (`/bp`) | Open the shared team backpack |
| `/bingo teamchat` (`/tc`) | Toggle team chat |
| `/bingo teleport <player>` (`/bingo tp`) | Teleport to a teammate |
| `/bingo dump [everything]` | Dump built-in task/settings definitions |
| `/bingo emergency` | Receive emergency supplies (depends on settings) |

The default key **Y** expands the whole Bingo card; its size and position can be adjusted in the client config.

## Differences from upstream

- Fully ported to Minecraft 26.2 / NeoForge 26.2.0.93; API changes are documented in [CHANGES.md](CHANGES.md).
- Added `TraderSpawnPatch` so wandering traders can spawn during short rounds.
- Upstream optional integrations (Curios, MineMention, SkyblockBuilder, Waila, JEI bookmark plugin) and `CrownRenderer` are not included in this port.
- **World saves are not compatible**: the mod id and SavedData identifiers are back to `bongo`, so Bingo state from test worlds that used another id will not be read. Start a new world or run `/bingo create` again.

## Building

Requires JDK 25 and a NeoForge 26.2.0.93 client/server installation. Example:

```powershell
.\build.ps1 -Version 26.2-1.0.0-port.11 `
  -MinecraftDir "D:\minecraft\.minecraft" `
  -NeoForgeVersion "26.2-NeoForge_26.2.0.93" `
  -LibXJar "D:\minecraft\.minecraft\mods\LibX-26.2-8.0.0.jar"
```

The script compiles `src/` and packages `res/` plus `LICENSE` / `NOTICE` / `THIRD-PARTY-NOTICES.md` into `dist/ReBongo-<version>.jar`.

## License and credits

- Original work **Bongo**: author **noeppi_noeppi**, Apache License 2.0 — https://github.com/noeppi-noeppi/Bongo
- Botania assets/code: **Vazkii and the Botania authors**, Botania License; modifications are documented, and this project stays open source and free to distribute.
- Task/settings content: **CastCrafter** (bundled with the upstream project under `data/castbingo`).
- Dependency **LibX**: **ModdingX (MelanX, noeppi_noeppi)**, Apache License 2.0.
- Dependency **NeoForge**: NeoForged, LGPL-2.1 (not bundled).
- Minecraft: Mojang, all rights reserved (not bundled).

Maintained by **Big_Jls**; porting assistance by **DeepSeek-v4.1-Flash-1017**.

This project does not use the upstream icon or author avatar and does not imply any official relationship (Apache License 2.0, section 6, does not grant trademark rights).
