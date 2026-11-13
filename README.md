# ReBongo — Bongo 的非官方 NeoForge 26.2 移植

> **原项目（上游）**
> - Bongo — <https://github.com/noeppi-noeppi/Bongo>
> - BingoLobby — <https://github.com/noeppi-noeppi/BingoLobby>
> - LibX — <https://github.com/ModdingX/LibX>
>
> **非官方项目**：这是社区维护的移植版，不是官方发布，与原作者 **noeppi_noeppi** 没有隶属关系，也未获其背书或赞助。原项目为 **Bongo**（mcmod 分类「宾果游戏」）。
>
> **许可**：Apache License 2.0。部分素材/代码来自 Botania，遵循 [Botania License](https://botaniamod.net/license.php)，详见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。
>
> **源码**：本仓库即完整源码。按 Botania License 的 Copyleft 条款，本项目保持开源、允许再分发与修改，并且免费分发。

## 这是什么

Bongo 是一个多人宾果小游戏：玩家组队，在 Bingo Card 上完成任务（收集物品、完成进度、击杀实体、前往群系等），率先完成一行、一列或对角线的 5 个任务即获胜。

本仓库把 Forge / Minecraft 1.20.1 时代的 Bongo 代码移植到 **Minecraft 26.2 / NeoForge**，并保留上游的 mod id 与命名空间：

- `modId = bongo`
- 资源命名空间 `assets/bongo`、`data/bongo`
- 命令根节点仍是 `/bingo`（与上游一致）

## 环境要求

| 依赖 | 版本 |
|---|---|
| Minecraft | 26.2 |
| NeoForge | 26.2.0.93 或更高 |
| Java | 25 |
| LibX | **26.2-8.0.0**（官方版，必需，客户端与服务端都要装） |

LibX 请使用官方发布版（CurseForge 文件 ID 9409485）。本仓库不附带、也不发布 LibX。

## 安装

1. 安装 NeoForge 26.2.0.93+。
2. 把 `ReBongo-26.2-1.0.0-port.11.jar` 与 `LibX-26.2-8.0.0.jar` 放入 `mods`。
   > 从 `26.2-1.0.0-port.6` 起构建产物改名为 `ReBongo-<version>.jar`。升级时请先删除旧的 `Bingo-*.jar`，避免同一个 `modId = bongo` 出现两个 jar。
3. 客户端与服务端都需要安装。
4. 进入游戏后由 OP 执行 `/bingo create <tasks_id> <settings_id>` 创建对局。

## 命令

| 命令 | 说明 |
|---|---|
| `/bingo create <tasks_id> <settings_id>` | 创建对局 |
| `/bingo join <team>` | 加入队伍（16 种颜色） |
| `/bingo leave` | 离开队伍 |
| `/bingo start` | 开始对局（会清空背包与进度） |
| `/bingo stop` | 停止对局 |
| `/bingo spread <teams>` | 自动均分队伍 |
| `/bingo teams` | 列出队伍与成员 |
| `/bingo backpack`（`/bp`） | 打开队伍共享背包 |
| `/bingo teamchat`（`/tc`） | 切换队伍聊天 |
| `/bingo teleport <player>`（`/bingo tp`） | 传送到队友 |
| `/bingo dump [everything]` | 导出内置任务/设置定义 |
| `/bingo emergency` | 领取紧急物资（按设置） |

默认按键 **Y** 展开整张 Bingo Card；地图大小与位置可在客户端配置中调整。

## 与上游的差异

- 完整移植到 Minecraft 26.2 / NeoForge 26.2.0.93，API 重写见 [CHANGES.md](CHANGES.md)。
- 新增 `TraderSpawnPatch`：短局中让流浪商人有机会刷新。
- 上游的可选联动（Curios、MineMention、SkyblockBuilder、Waila、JEI 书签插件）以及 `CrownRenderer` 未包含在本移植源码中。
- **存档不兼容**：mod id 与 SavedData 标识已回到 `bongo`，此前使用其它 id 的测试存档中的 Bingo 状态不会被读取，请开新世界或重新 `/bingo create`。

## 构建

需要 JDK 25 与一份 NeoForge 26.2.0.93 客户端/服务端安装。示例：

```powershell
.\build.ps1 -Version 26.2-1.0.0-port.11 `
  -MinecraftDir "D:\minecraft\.minecraft" `
  -NeoForgeVersion "26.2-NeoForge_26.2.0.93" `
  -LibXJar "D:\minecraft\.minecraft\mods\LibX-26.2-8.0.0.jar"
```

脚本会编译 `src/`、把 `res/` 与 `LICENSE` / `NOTICE` / `THIRD-PARTY-NOTICES.md` 打进 `dist/ReBongo-<version>.jar`。

## 许可与致谢

- 原作品 **Bongo**：作者 **noeppi_noeppi**，Apache License 2.0 — https://github.com/noeppi-noeppi/Bongo
- Botania 素材/代码：**Vazkii 与 Botania 作者**，Botania License；本项目已注明修改，保持开源与免费分发
- 任务/设置内容：**CastCrafter**（随上游项目提供，位于 `data/castbingo`）
- 依赖 **LibX**：**ModdingX（MelanX、noeppi_noeppi）**，Apache License 2.0

本移植版维护：**Big_Jls**；移植协助：**DeepSeek-v4.1-Flash-1017**。

本项目不使用上游的图标/作者头像，也不暗示任何官方关系（Apache License 2.0 第 6 条不授予商标权）。
