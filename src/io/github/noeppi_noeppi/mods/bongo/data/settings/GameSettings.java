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

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.util.DynamicUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moddingx.libx.annotation.api.Codecs;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.datapack.DataLoader;
import org.moddingx.libx.util.lazy.CachedValue;

public record GameSettings(PlaySettings game, LevelSettings level, EquipmentSettings equipment, ServerSettings server) {
   public static final Codec<GameSettings> CODEC = Codecs.get(BingoMod.class, GameSettings.class);
   public static final Identifier DEFAULT_ID = BingoMod.getInstance().id("default");
   public static final GameSettings DEFAULT = new GameSettings(PlaySettings.DEFAULT, LevelSettings.DEFAULT, EquipmentSettings.DEFAULT, ServerSettings.DEFAULT);
   private static Map<Identifier, Dynamic<JsonElement>> GAME_SETTINGS = Map.of();
   private static final CachedValue<Set<Identifier>> SETTING_KEYS = new CachedValue(() -> {
      Set<Identifier> set = new HashSet<>(GAME_SETTINGS.keySet());
      set.add(DEFAULT_ID);
      return Set.copyOf(set);
   });

   public static Codec<GameSettings> mergeTo(GameSettings fallback) {
      return RecordCodecBuilder.create(
         instance -> instance.group(
               MoreCodecs.optionalFieldOf(DynamicUtil.createMergedCodec(PlaySettings.CODEC, fallback.game()), "game", fallback.game())
                  .forGetter(GameSettings::game),
               MoreCodecs.optionalFieldOf(DynamicUtil.createMergedCodec(LevelSettings.CODEC, fallback.level()), "level", fallback.level())
                  .forGetter(GameSettings::level),
               MoreCodecs.optionalFieldOf(DynamicUtil.createMergedCodec(EquipmentSettings.CODEC, fallback.equipment()), "equipment", fallback.equipment())
                  .forGetter(GameSettings::equipment),
               MoreCodecs.optionalFieldOf(DynamicUtil.createMergedCodec(ServerSettings.CODEC, fallback.server()), "server", fallback.server())
                  .forGetter(GameSettings::server)
            )
            .apply(instance, GameSettings::new)
      );
   }

   public static Set<Identifier> gameSettings() {
      return (Set<Identifier>)SETTING_KEYS.get();
   }

   public static GameSettings load(List<Identifier> settings) {
      if (settings.isEmpty()) {
         return DEFAULT;
      } else {
         Identifier last = settings.get(settings.size() - 1);
         if (DEFAULT_ID.equals(last)) {
            return DEFAULT;
         } else if (!GAME_SETTINGS.containsKey(last)) {
            throw new NoSuchElementException("Settings not found: " + last);
         } else {
            return (GameSettings)((Pair)mergeTo(load(settings.subList(0, settings.size() - 1)))
                  .decode(GAME_SETTINGS.get(last))
                  .getOrThrow(err -> new IllegalStateException(err)))
               .getFirst();
         }
      }
   }

   public static void loadGameSettings(ResourceManager rm) throws IOException {
      SETTING_KEYS.invalidate();
      GAME_SETTINGS = DataLoader.loadJson(rm, "bingo_settings", (id, json) -> new Dynamic(JsonOps.INSTANCE, json));
   }
}
