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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.server.MinecraftServer;

public class GameTaskGroup {
   public static final String PSEUDO_TYPE = "bongo.group";
   public static final Codec<GameTaskGroup> CODEC = RecordCodecBuilder.<Pair<String, GameTaskGroup>>create(
         instance -> instance.group(
               Codec.STRING.fieldOf("type").forGetter(Pair::getFirst),
               WeightedTaskProvider.CODEC.listOf().fieldOf("tasks").forGetter(pair -> pair.getSecond().tasks())
            )
            .apply(instance, (type, tasks) -> Pair.of(type, new GameTaskGroup(tasks)))
      )
      .flatXmap(
         pair -> "bongo.group".equals(pair.getFirst()) ? DataResult.success(pair.getSecond()) : DataResult.error(() -> "Not a task group"),
         group -> DataResult.success(Pair.of("bongo.group", group))
      );
   private final List<WeightedTaskProvider> tasks;
   private final List<Pair<Either<Task, GameTaskGroup>, Integer>> builtTasks;
   private final int totalWeight;

   public GameTaskGroup(List<WeightedTaskProvider> tasks) {
      this.tasks = List.copyOf(tasks);
      this.builtTasks = this.tasks.stream().flatMap(WeightedTaskProvider::elements).toList();
      this.totalWeight = this.tasks.stream().map(WeightedTaskProvider::totalWeight).reduce(0, Integer::sum);
   }

   public int totalWeight() {
      return this.totalWeight;
   }

   public int availableTasks() {
      return this.builtTasks.size();
   }

   public List<WeightedTaskProvider> tasks() {
      return this.tasks;
   }

   public Either<List<Task>, String> choseTasks(Random random, int taskAmount) {
      if (this.builtTasks.size() < taskAmount) {
         return Either.right("bongo.cmd.create.less.subgroup");
      }

      int weightLeft = this.totalWeight;
      int collectedTasks = 0;
      List<Task> theTasks = new ArrayList<>();
      Map<GameTaskGroup, Integer> theGroups = new HashMap<>();
      List<Integer> theTasksIndices = new ArrayList<>();

      while (collectedTasks < taskAmount) {
         int rand = random.nextInt(weightLeft);
         int weightCounted = 0;

         for (int i = 0; i < this.builtTasks.size(); i++) {
            if (!theTasksIndices.contains(i)) {
               Pair<Either<Task, GameTaskGroup>, Integer> pair = this.builtTasks.get(i);
               weightCounted += pair.getSecond();
               if (weightCounted > rand) {
                  Either<Task, GameTaskGroup> taskDef = (Either<Task, GameTaskGroup>)pair.getFirst();
                  if (taskDef.left().isPresent()) {
                     theTasks.add((Task)taskDef.left().get());
                  } else if (taskDef.right().isPresent()) {
                     GameTaskGroup group = (GameTaskGroup)taskDef.right().get();
                     if (!theGroups.containsKey(group)) {
                        theGroups.put(group, 0);
                     }

                     theGroups.put(group, theGroups.get(group) + 1);
                  }

                  theTasksIndices.add(i);
                  collectedTasks++;
                  weightLeft -= pair.getSecond();
                  break;
               }
            }
         }
      }

      for (Entry<GameTaskGroup, Integer> entry : theGroups.entrySet()) {
         if (entry.getValue() > 0) {
            Either<List<Task>, String> result = entry.getKey().choseTasks(random, entry.getValue());
            if (result.right().isPresent() || result.left().isEmpty()) {
               return Either.right(result.right().isPresent() ? (String)result.right().get() : "Unknown Error");
            }

            theTasks.addAll((Collection<? extends Task>)result.left().get());
         }
      }

      Collections.shuffle(theTasks);
      return Either.left(theTasks);
   }

   public void validateTasks(MinecraftServer server) {
      for (WeightedTaskProvider entry : this.tasks) {
         Either<Task, GameTaskGroup> task = entry.decompose().mapLeft(WeightedTask::task).mapRight(WeightedGroup::group);
         task.left().ifPresent(t -> t.validate(server));
         task.right().ifPresent(t -> t.validateTasks(server));
      }
   }
}
