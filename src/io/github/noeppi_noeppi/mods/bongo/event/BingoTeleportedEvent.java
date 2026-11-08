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
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporter;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class BingoTeleportedEvent extends Event {
   private final Bingo bingo;
   private final ServerLevel level;
   private final Team team;
   private final PlayerTeleporter teleporter;
   private final List<ServerPlayer> players;

   public BingoTeleportedEvent(Bingo bingo, ServerLevel level, Team team, PlayerTeleporter teleporter, List<ServerPlayer> players) {
      this.bingo = bingo;
      this.level = level;
      this.team = team;
      this.teleporter = teleporter;
      this.players = players;
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

   public PlayerTeleporter getTeleporter() {
      return this.teleporter;
   }

   public List<ServerPlayer> getPlayers() {
      return this.players;
   }
}
