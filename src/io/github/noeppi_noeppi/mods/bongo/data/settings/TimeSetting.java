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

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.OptionalInt;
import java.util.stream.Stream;

public sealed interface TimeSetting permits TimeSetting.Time, TimeSetting.Unlimited {
   boolean limited();

   OptionalInt limit();

   static MapCodec<TimeSetting> fieldOf(final String name) {
      return new MapCodec<TimeSetting>() {
         public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of((T)ops.createString(name));
         }

         public <T> DataResult<TimeSetting> decode(DynamicOps<T> ops, MapLike<T> input) {
            T elem = (T)input.get(name);
            if (elem == null) {
               return DataResult.error(() -> "Missing key: " + name);
            }

            DataResult<Number> num = ops.getNumberValue(elem);
            return num.result().isPresent() ? num.map(n -> new TimeSetting.Time(n.intValue())) : ops.getStringValue(elem).flatMap(str -> {
               if ("unlimited".equalsIgnoreCase(str)) {
                  return DataResult.success(new TimeSetting.Unlimited());
               }

               try {
                  return DataResult.success(new TimeSetting.Time(Integer.parseInt(str)));
               } catch (NumberFormatException e) {
                  return DataResult.error(() -> "Invalid time value: " + str);
               }
            });
         }

         public <T> RecordBuilder<T> encode(TimeSetting input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (input instanceof TimeSetting.Time time) {
               prefix.add(name, ops.createInt(time.time()));
            } else {
               prefix.add(name, ops.createString("unlimited"));
            }

            return prefix;
         }
      };
   }

   record Time(int time) implements TimeSetting {
      @Override
      public boolean limited() {
         return true;
      }

      @Override
      public OptionalInt limit() {
         return OptionalInt.of(this.time());
      }
   }

   record Unlimited() implements TimeSetting {
      @Override
      public boolean limited() {
         return false;
      }

      @Override
      public OptionalInt limit() {
         return OptionalInt.empty();
      }
   }
}
