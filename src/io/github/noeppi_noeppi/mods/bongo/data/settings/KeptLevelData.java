/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data.settings;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.Unit;
import org.moddingx.libx.codec.MoreCodecs;

public enum KeptLevelData {
   game_mode,
   equipment,
   advancements,
   experience,
   statistics,
   time,
   weather,
   wandering_trader_time;

   public static final Set<KeptLevelData> ALL = Set.of(values());
   public static final Codec<Set<KeptLevelData>> CODEC = Codec.either(
         MoreCodecs.fixed("all"), MoreCodecs.enumCodec(KeptLevelData.class).listOf().xmap(Set::copyOf, List::copyOf)
      )
      .xmap(either -> (Set)either.map(unit -> ALL, Function.identity()), set -> ALL.equals(set) ? Either.left(Unit.INSTANCE) : Either.right(set));
}
