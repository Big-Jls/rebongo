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
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.List;
import java.util.stream.Stream;
import org.moddingx.libx.codec.MoreCodecs;

public record WeightedGroup(GameTaskGroup group, List<Integer> weights) implements WeightedTaskProvider {
   private static final Codec<List<Integer>> WEIGHTS_CODEC = Codec.either(Codec.INT, Codec.INT.listOf())
      .xmap(either -> Util.join(either.mapLeft(List::of)), Either::right);
   public static final Codec<WeightedGroup> CODEC = MoreCodecs.extend(
      GameTaskGroup.CODEC, WEIGHTS_CODEC.fieldOf("weight").orElse(List.of(1)), group -> Pair.of(group.group(), group.weights()), WeightedGroup::new
   );

   @Override
   public int totalWeight() {
      return this.weights.stream().reduce(0, Integer::sum);
   }

   @Override
   public Stream<Pair<Either<Task, GameTaskGroup>, Integer>> elements() {
      return this.weights.stream().map(weight -> Pair.of(Either.right(this.group()), weight));
   }

   @Override
   public Either<WeightedTask, WeightedGroup> decompose() {
      return Either.right(this);
   }
}
