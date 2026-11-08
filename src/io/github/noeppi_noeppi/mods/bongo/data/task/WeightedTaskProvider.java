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
import java.util.stream.Stream;
import org.moddingx.libx.codec.MoreCodecs;

public sealed interface WeightedTaskProvider permits WeightedTask, WeightedGroup {
   Codec<WeightedTaskProvider> CODEC = MoreCodecs.lazy(
      () -> Codec.either(WeightedGroup.CODEC, WeightedTask.CODEC).xmap(either -> Util.join(either.swap()), provider -> provider.decompose().swap())
   );

   int totalWeight();

   Stream<Pair<Either<Task, GameTaskGroup>, Integer>> elements();

   Either<WeightedTask, WeightedGroup> decompose();
}
