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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Map.Entry;

public class DynamicUtil {
   public static <T> DataResult<Dynamic<T>> mergeMaps(Dynamic<T> dynamic, Dynamic<T> additional) {
      DataResult<Dynamic<T>> result = DataResult.success(dynamic);
      boolean dynamicIsMap = dynamic.getMapValues().result().isPresent();
      if (dynamicIsMap) {
         DataResult<Map<Dynamic<T>, Dynamic<T>>> additionalMapValues = additional.getMapValues();
         if (additionalMapValues.result().isPresent()) {
            Map<Dynamic<T>, Dynamic<T>> map = (Map<Dynamic<T>, Dynamic<T>>)(Map)additionalMapValues.result().get();
            for (Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
               result = result.flatMap(dyn -> dyn.merge(entry.getKey(), entry.getValue()).get());
            }
         }
      }

      return result;
   }

   public static <T> Codec<T> createMergedCodec(Codec<T> codec, T defaultValue) {
      return Codec.of(codec, createMergedDecoder(codec, defaultValue));
   }

   public static <T> Decoder<T> createMergedDecoder(final Codec<T> codec, final T defaultValue) {
      return new Decoder<T>() {
         public <A> DataResult<Pair<T, A>> decode(DynamicOps<A> ops, A input) {
            DataResult<Dynamic<A>> dflRes = codec.encodeStart(ops, defaultValue).map(res -> new Dynamic(ops, res));
            if (dflRes.result().isEmpty()) {
               return DataResult.error(() -> "Failed to encode default value: " + dflRes.error().map(err -> err.message()).orElse("null") + ": " + defaultValue);
            }

            Dynamic<A> dynamic = (Dynamic<A>)dflRes.result().get();
            DataResult<Dynamic<A>> merged = DynamicUtil.mergeMaps((Dynamic<T>)dynamic, new Dynamic(ops, input));
            return merged.flatMap(r -> codec.decode(r));
         }
      };
   }
}
