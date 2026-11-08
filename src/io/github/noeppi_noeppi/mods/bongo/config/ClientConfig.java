/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port; config spec initialised in a single
 * static block so field order cannot break class initialisation.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ClientConfig {
   private static final Builder CLIENT_BUILDER = new Builder();
   public static final ModConfigSpec CLIENT_CONFIG;

   public static ConfigValue<Double> bongoMapScaleFactor;
   public static ConfigValue<Boolean> bongoMapOnTheRight;
   public static ConfigValue<Boolean> addItemTooltips;
   public static ConfigValue<Boolean> modifyJeiBookamrks;

   static {
      init(CLIENT_BUILDER);
      CLIENT_CONFIG = CLIENT_BUILDER.build();
   }

   public static void init(Builder builder) {
      bongoMapScaleFactor = builder.comment("Controls the size of the bongo map.").define("bongo_map_scale_factor", 1.0);
      bongoMapOnTheRight = builder.comment("Whether the bongo map should be moved to the right side of the screen instead of the left.")
         .define("bongo_map_on_the_right", false);
      addItemTooltips = builder.comment(
            "Whether bongo should add tooltips to items in the bingo card. This may cause a short spike of lagg when a game is created."
         )
         .define("add_item_tooltips", true);
      modifyJeiBookamrks = builder.comment("Whether bongo should put all bingo items in the JEI bookmarks.").define("modify_jei_bookmarks", true);
   }
}
