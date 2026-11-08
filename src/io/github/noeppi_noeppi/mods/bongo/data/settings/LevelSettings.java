/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data.settings;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporter;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporterDefault;
import java.util.Set;
import org.moddingx.libx.annotation.api.Codecs;

public record LevelSettings(Set<KeptLevelData> keep, PlayerTeleporter teleporter, int teleportRadius) {
   public static final Codec<LevelSettings> CODEC = Codecs.get(BingoMod.class, LevelSettings.class);
   public static final LevelSettings DEFAULT = new LevelSettings(Set.of(), PlayerTeleporterDefault.INSTANCE, 10000);
}
