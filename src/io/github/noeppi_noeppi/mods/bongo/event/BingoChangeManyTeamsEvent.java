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
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class BingoChangeManyTeamsEvent extends Event implements ICancellableEvent {
   private final Bingo bingo;
   private final Set<Team> teamsToAssign;
   private Component failureMessage;

   public BingoChangeManyTeamsEvent(Bingo bingo, Set<Team> teamsToAssign, Component failureMessage) {
      this.bingo = bingo;
      this.teamsToAssign = teamsToAssign;
      this.failureMessage = failureMessage;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   public Set<Team> getTeamsToAssign() {
      return this.teamsToAssign;
   }

   public Component getFailureMessage() {
      return this.failureMessage;
   }

   public void setFailureMessage(Component failureMessage) {
      this.failureMessage = failureMessage;
   }
}
