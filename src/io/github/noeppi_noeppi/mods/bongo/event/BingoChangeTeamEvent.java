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
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class BingoChangeTeamEvent extends Event implements ICancellableEvent {
   private final ServerPlayer player;
   private final Bingo bingo;
   @Nullable
   private final Team oldTeam;
   @Nullable
   private final Team newTeam;
   private Component failureMessage;

   public BingoChangeTeamEvent(ServerPlayer player, Bingo bingo, @Nullable Team oldTeam, @Nullable Team newTeam, Component failureMessage) {
      this.player = player;
      this.bingo = bingo;
      this.oldTeam = oldTeam;
      this.newTeam = newTeam;
      this.failureMessage = failureMessage;
   }

   public ServerPlayer getPlayer() {
      return this.player;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   @Nullable
   public Team getOldTeam() {
      return this.oldTeam;
   }

   @Nullable
   public Team getNewTeam() {
      return this.newTeam;
   }

   public Component getFailureMessage() {
      return this.failureMessage;
   }

   public void setFailureMessage(Component failureMessage) {
      this.failureMessage = failureMessage;
   }
}
