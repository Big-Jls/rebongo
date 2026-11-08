/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data.task;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.datapack.DataLoader;

public class GameTasks {
   public static final Codec<GameTasks> CODEC = WeightedTaskProvider.CODEC
      .listOf()
      .fieldOf("tasks")
      .codec()
      .xmap(GameTaskGroup::new, GameTaskGroup::tasks)
      .xmap(GameTasks::new, tasks -> tasks.rootGroup);
   private static Map<Identifier, GameTasks> GAME_TASKS = Map.of();
   private final GameTaskGroup rootGroup;

   public GameTasks(GameTaskGroup rootGroup) {
      this.rootGroup = rootGroup;
   }

   public Either<List<Task>, String> getBingoTasks() {
      return this.rootGroup.availableTasks() < 25 ? Either.right("bongo.cmd.create.less") : this.rootGroup.choseTasks(new Random(), 25);
   }

   public void validateTasks(MinecraftServer server) {
      this.rootGroup.validateTasks(server);
   }

   public static void validateAllTasks(MinecraftServer server) {
      GAME_TASKS.values().forEach(t -> t.validateTasks(server));
   }

   public static Map<Identifier, GameTasks> gameTasks() {
      return GAME_TASKS;
   }

   public static void loadGameTasks(ResourceManager rm) throws IOException {
      GAME_TASKS = DataLoader.loadJson(rm, "bingo_tasks", CODEC);
   }
}
