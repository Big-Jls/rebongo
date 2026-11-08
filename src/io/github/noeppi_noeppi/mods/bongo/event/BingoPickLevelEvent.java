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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class BingoPickLevelEvent extends Event {
   private final Bingo bingo;
   private final MinecraftServer server;
   private ServerLevel level;

   public BingoPickLevelEvent(Bingo bingo, ServerLevel level) {
      this.bingo = bingo;
      this.server = level.getServer();
      this.level = level;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public void setLevel(ServerLevel level) {
      this.level = level;
   }
}
