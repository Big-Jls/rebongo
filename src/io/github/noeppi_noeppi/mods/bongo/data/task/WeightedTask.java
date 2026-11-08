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
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import java.util.stream.Stream;
import org.moddingx.libx.codec.MoreCodecs;

public record WeightedTask(Task task, int weight) implements WeightedTaskProvider {
   public static final Codec<WeightedTask> CODEC = MoreCodecs.extend(
      Task.CODEC, Codec.INT.fieldOf("weight").orElse(1), task -> Pair.of(task.task(), task.weight()), WeightedTask::new
   );

   @Override
   public int totalWeight() {
      return this.weight();
   }

   @Override
   public Stream<Pair<Either<Task, GameTaskGroup>, Integer>> elements() {
      return Stream.of(Pair.of(Either.left(this.task()), this.weight()));
   }

   @Override
   public Either<WeightedTask, WeightedGroup> decompose() {
      return Either.left(this);
   }
}
