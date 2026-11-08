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

import com.mojang.datafixers.util.Either;
import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.data.task.GameTasks;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import java.util.List;

public class GameDef {
   public final GameTasks tasks;
   public final GameSettings settings;

   public GameDef(GameTasks tasks, GameSettings settings) {
      this.tasks = tasks;
      this.settings = settings;
   }

   public String createBingo(Bingo bingo) {
      if (bingo.running()) {
         bingo.stop();
      }

      Either<List<Task>, String> taskList = this.tasks.getBingoTasks();
      if (!taskList.right().isPresent() && !taskList.left().isEmpty()) {
         bingo.setSettings(this.settings, true);
         bingo.setTasks((List<Task>)taskList.left().get());
         return null;
      } else {
         return taskList.right().isPresent() ? (String)taskList.right().get() : "Unknown Error";
      }
   }
}
