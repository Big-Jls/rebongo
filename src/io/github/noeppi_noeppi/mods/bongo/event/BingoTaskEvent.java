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
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class BingoTaskEvent extends Event {
   private final Bingo bingo;
   private final ServerLevel level;
   private final ServerPlayer player;
   private final Task task;

   public BingoTaskEvent(Bingo bingo, ServerLevel level, ServerPlayer player, Task task) {
      this.bingo = bingo;
      this.level = level;
      this.player = player;
      this.task = task;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public ServerPlayer getPlayer() {
      return this.player;
   }

   public Task getTask() {
      return this.task;
   }
}
