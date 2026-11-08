/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.task;

import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;

public abstract class RegistryTaskType<T> implements TaskType<T> {
   private final Class<T> cls;
   private final Registry<T> registry;

   protected RegistryTaskType(Class<T> cls, Registry<T> registry) {
      this.cls = cls;
      this.registry = registry;
   }

   @Override
   public Class<T> taskClass() {
      return this.cls;
   }

   @Override
   public MapCodec<T> codec() {
      return this.registry.byNameCodec().fieldOf("value");
   }

   @Override
   public Comparator<T> order() {
      return Comparator.comparing(this.registry::getKey, Util.COMPARE_RESOURCE);
   }

   @Override
   public boolean shouldComplete(ServerPlayer player, T element, T compare) {
      return Objects.equals(element, compare);
   }
}
