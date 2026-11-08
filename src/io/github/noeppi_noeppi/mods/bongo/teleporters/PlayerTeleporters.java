/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.teleporters;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlayerTeleporters {
   public static final Codec<PlayerTeleporter> CODEC = Codec.STRING.xmap(PlayerTeleporters::getTeleporter, PlayerTeleporter::id);
   private static final Map<String, PlayerTeleporter> teleporters = new HashMap<>();

   public static PlayerTeleporter getTeleporter(String id) {
      return id == null ? PlayerTeleporterDefault.INSTANCE : teleporters.get(id.toLowerCase());
   }

   public static void registerTeleporter(PlayerTeleporter type) {
      String id = type.id().toLowerCase();
      if (teleporters.containsKey(id)) {
         throw new IllegalStateException("Player Teleporter with id '" + id + "' is already registered.");
      }

      teleporters.put(id, type);
   }

   public static Collection<PlayerTeleporter> getTeleporters() {
      return teleporters.values();
   }
}
