/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: added a wandering-trader spawn helper for short Bingo rounds.
 * See LICENSE, NOTICE, and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo;

import io.github.noeppi_noeppi.mods.bongo.event.BingoStartEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WanderingTraderData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Makes wandering traders spawn during a Bingo round. The vanilla spawner only
 * decreases spawn_delay every 20 minutes and then has a 1/10 chance, so short
 * rounds would normally never see a trader. This resets the delay at game start
 * and once per minute while a game is running.
 */
@EventBusSubscriber(modid = "bongo")
public final class TraderSpawnPatch {
   @SubscribeEvent
   public static void gameStart(BingoStartEvent.Level event) {
      MinecraftServer server = event.getLevel().getServer();
      if (server != null) {
         ServerLevel level = server.overworld();
         level.getGameRules().set(GameRules.SPAWN_WANDERING_TRADERS, true, server);
         reset(level);
      }
   }

   @SubscribeEvent
   public static void serverTick(ServerTickEvent.Post event) {
      MinecraftServer server = event.getServer();
      if (server.getTickCount() % 1200 == 0) {
         ServerLevel level = server.overworld();
         Bingo bongo = Bingo.get(level);
         if (bongo.active() && bongo.running()) {
            reset(level);
         }
      }
   }

   private static void reset(ServerLevel level) {
      WanderingTraderData data = level.getDataStorage().computeIfAbsent(WanderingTraderData.TYPE);
      data.setSpawnDelay(0);
      data.setSpawnChance(100);
      data.setDirty();
   }

   private TraderSpawnPatch() {
   }
}