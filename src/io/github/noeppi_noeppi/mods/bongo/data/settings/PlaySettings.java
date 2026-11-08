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
import io.github.noeppi_noeppi.mods.bongo.data.WinCondition;
import org.moddingx.libx.annotation.api.Codecs;

public record PlaySettings(
   WinCondition winCondition,
   boolean invulnerable,
   boolean pvp,
   boolean friendlyFire,
   TimeSetting time,
   boolean consumeItems,
   boolean lockTaskOnDeath,
   int teleportsPerTeam,
   boolean leaderboard,
   boolean lockout
) {
   public static final Codec<PlaySettings> CODEC = Codecs.get(BingoMod.class, PlaySettings.class);
   public static final PlaySettings DEFAULT = new PlaySettings(
      WinCondition.DEFAULT, true, false, false, new TimeSetting.Unlimited(), false, false, 0, false, false
   );
}
