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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public abstract class BingoStopEvent extends Event {
   private final Bingo bingo;
   private final ServerLevel level;

   private BingoStopEvent(Bingo bingo, ServerLevel level) {
      this.bingo = bingo;
      this.level = level;
   }

   public Bingo getBingo() {
      return this.bingo;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public static class Level extends BingoStopEvent {
      public Level(Bingo bingo, ServerLevel level) {
         super(bingo, level);
      }
   }

   public static class Player extends BingoStopEvent {
      private final ServerPlayer player;

      public Player(Bingo bingo, ServerLevel level, ServerPlayer player) {
         super(bingo, level);
         this.player = player;
      }

      public ServerPlayer getPlayer() {
         return this.player;
      }
   }
}
