/*
 * Generated/decompiled artifact derived from Bingo (Copyright (c) noeppi_noeppi,
 * Apache License 2.0). MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls
 * (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * No new copyright claim is made for the generated structure.
 * See LICENSE and NOTICE.
 */
package io.github.noeppi_noeppi.mods.bongo;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.bongo.data.WinCondition;
import io.github.noeppi_noeppi.mods.bongo.data.settings.EquipmentSettings;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.data.settings.KeptLevelData;
import io.github.noeppi_noeppi.mods.bongo.data.settings.LevelSettings;
import io.github.noeppi_noeppi.mods.bongo.data.settings.PlaySettings;
import io.github.noeppi_noeppi.mods.bongo.data.settings.ServerSettings;
import io.github.noeppi_noeppi.mods.bongo.data.settings.TimeSetting;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporter;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporters;
import io.github.noeppi_noeppi.mods.bongo.util.StackTagReader;
import io.github.noeppi_noeppi.mods.bongo.util.TagWithCount;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.annotation.impl.LazyMapBuilder;
import org.moddingx.libx.annotation.impl.ProcessorInterface;
import org.moddingx.libx.mod.ModX;

public class BingoMod$ {
   private static ModX mod = null;
   public static final Map<Class<?>, Codec<?>> codecs = buildCodecs();

   private static final Map<Class<?>, Codec<?>> buildCodecs() {
      LazyMapBuilder builder = ProcessorInterface.lazyMapBuilder();
      builder.put(
         EquipmentSettings.class,
         () -> RecordCodecBuilder.<EquipmentSettings>create(
            instance -> instance.group(
                  StackTagReader.SAFE_ITEM_STACK.listOf().fieldOf("inventory").forGetter(EquipmentSettings::inventory),
                  StackTagReader.SAFE_ITEM_STACK.fieldOf("head").forGetter(EquipmentSettings::head),
                  StackTagReader.SAFE_ITEM_STACK.fieldOf("chest").forGetter(EquipmentSettings::chest),
                  StackTagReader.SAFE_ITEM_STACK.fieldOf("legs").forGetter(EquipmentSettings::legs),
                  StackTagReader.SAFE_ITEM_STACK.fieldOf("feet").forGetter(EquipmentSettings::feet),
                  StackTagReader.SAFE_ITEM_STACK.fieldOf("offhand").forGetter(EquipmentSettings::offhand),
                  StackTagReader.SAFE_ITEM_STACK.listOf().fieldOf("backpack").forGetter(EquipmentSettings::backpack),
                  StackTagReader.SAFE_ITEM_STACK.listOf().fieldOf("emergency").forGetter(EquipmentSettings::emergency)
               )
               .apply(
                  instance,
                  instance.stable(
                     (Function8<List<ItemStack>, ItemStack, ItemStack, ItemStack, ItemStack, ItemStack, List<ItemStack>, List<ItemStack>, EquipmentSettings>)(ctorArg0, ctorArg1, ctorArg2, ctorArg3, ctorArg4, ctorArg5, ctorArg6, ctorArg7) -> new EquipmentSettings(
                        ctorArg0, ctorArg1, ctorArg2, ctorArg3, ctorArg4, ctorArg5, ctorArg6, ctorArg7
                     )
                  )
               )
         )
      );
      builder.put(
         GameSettings.class,
         () -> RecordCodecBuilder.<GameSettings>create(
            instance -> instance.group(
                  PlaySettings.CODEC.fieldOf("game").forGetter(GameSettings::game),
                  LevelSettings.CODEC.fieldOf("level").forGetter(GameSettings::level),
                  EquipmentSettings.CODEC.fieldOf("equipment").forGetter(GameSettings::equipment),
                  ServerSettings.CODEC.fieldOf("server").forGetter(GameSettings::server)
               )
               .apply(
                  instance, instance.stable((Function4<PlaySettings, LevelSettings, EquipmentSettings, ServerSettings, GameSettings>)(ctorArg0, ctorArg1, ctorArg2, ctorArg3) -> new GameSettings(ctorArg0, ctorArg1, ctorArg2, ctorArg3))
               )
         )
      );
      builder.put(
         LevelSettings.class,
         () -> RecordCodecBuilder.<LevelSettings>create(
            instance -> instance.group(
                  KeptLevelData.CODEC.fieldOf("keep").forGetter(LevelSettings::keep),
                  PlayerTeleporters.CODEC.fieldOf("teleporter").forGetter(LevelSettings::teleporter),
                  Codec.INT.fieldOf("teleport_radius").forGetter(LevelSettings::teleportRadius)
               )
               .apply(instance, instance.stable((Function3<Set<KeptLevelData>, PlayerTeleporter, Integer, LevelSettings>)(ctorArg0, ctorArg1, ctorArg2) -> new LevelSettings(ctorArg0, ctorArg1, ctorArg2)))
         )
      );
      builder.put(
         PlaySettings.class,
         () -> RecordCodecBuilder.<PlaySettings>create(
            instance -> instance.group(
                  WinCondition.CODEC.fieldOf("win_condition").forGetter(PlaySettings::winCondition),
                  Codec.BOOL.fieldOf("invulnerable").forGetter(PlaySettings::invulnerable),
                  Codec.BOOL.fieldOf("pvp").forGetter(PlaySettings::pvp),
                  Codec.BOOL.fieldOf("friendly_fire").forGetter(PlaySettings::friendlyFire),
                  TimeSetting.fieldOf("time").forGetter(PlaySettings::time),
                  Codec.BOOL.fieldOf("consume_items").forGetter(PlaySettings::consumeItems),
                  Codec.BOOL.fieldOf("lock_task_on_death").forGetter(PlaySettings::lockTaskOnDeath),
                  Codec.INT.fieldOf("teleports_per_team").forGetter(PlaySettings::teleportsPerTeam),
                  Codec.BOOL.fieldOf("leaderboard").forGetter(PlaySettings::leaderboard),
                  Codec.BOOL.fieldOf("lockout").forGetter(PlaySettings::lockout)
               )
               .apply(
                  instance,
                  instance.stable(
                     (Function10<WinCondition, Boolean, Boolean, Boolean, TimeSetting, Boolean, Boolean, Integer, Boolean, Boolean, PlaySettings>)(ctorArg0, ctorArg1, ctorArg2, ctorArg3, ctorArg4, ctorArg5, ctorArg6, ctorArg7, ctorArg8, ctorArg9) -> new PlaySettings(
                        ctorArg0, ctorArg1, ctorArg2, ctorArg3, ctorArg4, ctorArg5, ctorArg6, ctorArg7, ctorArg8, ctorArg9
                     )
                  )
               )
         )
      );
      builder.put(
         ServerSettings.class,
         () -> RecordCodecBuilder.<ServerSettings>create(
            instance -> instance.group(Codec.BOOL.fieldOf("prevent_joining_during_game").forGetter(ServerSettings::preventJoiningDuringGame))
               .apply(instance, instance.stable((Function<Boolean, ServerSettings>)ctorArg0 -> new ServerSettings(ctorArg0)))
         )
      );
      builder.put(
         TagWithCount.class,
         () -> RecordCodecBuilder.<TagWithCount>create(
            instance -> instance.group(
                  Identifier.CODEC.fieldOf("id").forGetter(TagWithCount::getId), Codec.INT.fieldOf("count").forGetter(TagWithCount::getCount)
               )
               .apply(instance, instance.stable((BiFunction<Identifier, Integer, TagWithCount>)(ctorArg0, ctorArg1) -> new TagWithCount(ctorArg0, ctorArg1)))
         )
      );
      return builder.build();
   }

   public static void init(ModX mod) {
      BingoMod$.mod = mod;
   }
}
