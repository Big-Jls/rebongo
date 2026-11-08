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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class PotionTextureRenderCache {
   private static final Map<Identifier, Identifier> CACHE = new HashMap<>();

   public static Identifier getRenderTexture(MobEffect effect) {
      Identifier id = Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(effect));
      if (CACHE.containsKey(id)) {
         return CACHE.get(id);
      }

      Identifier texture = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png");
      CACHE.put(id, texture);
      return texture;
   }
}
