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
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class RenderEntityCache {
   private static final Map<EntityType<?>, Entity> CACHE = new HashMap<>();
   private static Level cachedLevel = null;

   @Nullable
   public static <T extends Entity> T getRenderEntity(Minecraft mc, EntityType<T> type) {
      Level level = mc.level;
      if (level == null) {
         return null;
      }

      if (cachedLevel != level) {
         CACHE.clear();
         cachedLevel = level;
      }

      if (CACHE.containsKey(type)) {
         return (T)CACHE.get(type);
      }

      T entity;
      try {
         // ignoreChecks: the difficulty / feature-flag check in EntityType#create would return null
         // for hostile mobs in peaceful worlds, which made their bingo icons fall back to a spawner.
         entity = type.create(level, new EntitySpawnRequest(EntitySpawnReason.EVENT, true));
      } catch (RuntimeException e) {
         entity = null;
      }

      if (entity == null) {
         return null;
      }

      if (entity instanceof Mob mob) {
         mob.setNoAi(true);
      }

      entity.setId(-100000 - CACHE.size());
      CACHE.put(type, entity);
      return entity;
   }
}
