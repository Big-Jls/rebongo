/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public record StatAndValue(Stat<?> stat, int value) {
   public static final Codec<StatAndValue> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BuiltInRegistries.STAT_TYPE.byNameCodec().fieldOf("category").forGetter(stat -> stat.stat().getType()),
            Identifier.CODEC.fieldOf("stat").forGetter(StatAndValue::getValueId),
            Codec.INT.fieldOf("value").forGetter(StatAndValue::value)
         )
         .apply(instance, (type, id, value) -> new StatAndValue(resolve(type, id), value))
   );

   private static Stat<?> resolve(StatType<?> statType, Identifier id) {
      Object elem = statType.getRegistry().get(id);
      if (elem == null) {
         throw new IllegalStateException("Invalid stat value id for " + BuiltInRegistries.STAT_TYPE.getKey(statType) + ": " + id);
      } else {
         return (Stat<?>)((StatType)statType).get(elem);
      }
   }

   public Identifier getValueId() {
      return Objects.requireNonNull(((net.minecraft.core.Registry)this.stat.getType().getRegistry()).getKey(this.stat.getValue()), "Invalid stat object: No id");
   }
}
