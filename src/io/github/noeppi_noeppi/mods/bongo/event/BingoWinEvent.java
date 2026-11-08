/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.event;

import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class BingoWinEvent extends Event {
   private final Bingo bingo;
   private final ServerLevel level;
   private final Team team;

   public BingoWinEvent(Bingo bingo, ServerLevel level, Team team) {
      this.bingo = bingo;
      this.level = level;
      this.team = team;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public Team getTeam() {
      return this.team;
   }
}
