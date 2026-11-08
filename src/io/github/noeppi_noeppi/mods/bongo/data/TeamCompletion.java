/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data;

import io.github.noeppi_noeppi.mods.bongo.Bingo;

public class TeamCompletion {
   private final int winningTasks;
   private final int winningAmount;

   public TeamCompletion(Bingo bingo, Team team) {
      int winningTasks = 0;
      int winningAmount = 0;

      for (int i = 0; i < 25; i++) {
         if (bingo.task(i).isWinningTask(team.completed(i), team.locked(i))) {
            winningTasks |= 1 << i;
            winningAmount++;
         }
      }

      this.winningTasks = winningTasks;
      this.winningAmount = winningAmount;
   }

   public int count() {
      return this.winningAmount;
   }

   public boolean has(int task) {
      return (this.winningTasks & 1 << task % 25) != 0;
   }
}
