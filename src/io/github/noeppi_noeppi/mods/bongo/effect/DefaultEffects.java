/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.effect;

import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.data.settings.KeptLevelData;
import io.github.noeppi_noeppi.mods.bongo.event.BingoStartEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoTaskEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoWinEvent;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;

public class DefaultEffects {
   @SubscribeEvent
   public void gameStart(BingoStartEvent.Level event) {
   }

   @SubscribeEvent
   public void playerInit(BingoStartEvent.Player event) {
      Set<KeptLevelData> keep = event.getBingo().getSettings().level().keep();
      if (!keep.contains(KeptLevelData.equipment)) {
         event.getPlayer().getInventory().clearContent();
         event.getBingo().getSettings().equipment().equip(event.getPlayer());
      }

      if (!keep.contains(KeptLevelData.experience)) {
         event.getPlayer().setExperienceLevels(0);
         event.getPlayer().setExperiencePoints(0);
      }

      if (!keep.contains(KeptLevelData.game_mode)) {
         event.getPlayer().setGameMode(GameType.SURVIVAL);
      }

      if (!keep.contains(KeptLevelData.advancements)) {
         ServerPlayer player = event.getPlayer();
         for (AdvancementHolder holder : event.getLevel().getServer().getAdvancements().getAllAdvancements()) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            for (String criterion : progress.getCompletedCriteria()) {
               player.getAdvancements().revoke(holder, criterion);
            }
         }
      }

      if (!keep.contains(KeptLevelData.statistics)) {
      }
   }

   @SubscribeEvent
   public void playerTask(BingoTaskEvent event) {
      Team team = event.getBingo().getTeam(event.getPlayer());
      if (team != null) {
         MutableComponent tc = Component.translatable(
            event.getTask().inverted() ? "bongo.task.fail" : "bongo.task.complete",
            new Object[]{team.getName(), event.getTask().contentName(event.getLevel().getServer())}
         );
         event.getLevel()
            .getServer()
            .getPlayerList()
            .getPlayers()
            .forEach(
               player -> {
                  player.sendSystemMessage(tc);
                  if (team.hasPlayer(player)) {
                     player.connection
                        .send(
                           new ClientboundSoundPacket(
                              Util.sound(SoundEvents.END_PORTAL_SPAWN), SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 0.5F, 1.0F, 0L
                           )
                        );
                  }
               }
            );
      }
   }

   @SubscribeEvent
   public void teamWin(BingoWinEvent event) {
      MutableComponent tc = event.getTeam().getName().append(Component.translatable("bongo.win"));
      MutableComponent tcc = event.getTeam().getName().append(Component.translatable("bongo.winplayers"));
      event.getLevel()
         .getServer()
         .getPlayerList()
         .getPlayers()
         .forEach(
            player -> {
               if (event.getTeam().hasPlayer(player)) {
                  tcc.append(Component.literal(" "));
                  MutableComponent pname = player.getDisplayName().copy();
                  pname.setStyle(
                     Style.EMPTY
                        .applyFormat(ChatFormatting.RESET)
                        .applyFormat(ChatFormatting.UNDERLINE)
                        .withClickEvent(new RunCommand("/tp @p " + player.getX() + " " + player.getY() + " " + player.getZ()))
                  );
                  tcc.append(pname);
               }
            }
         );
      MutableComponent leaderboard;
      if (!event.getBingo().getSettings().game().leaderboard()) {
         leaderboard = null;
      } else {
         leaderboard = Component.empty();
         List<Team> teams = event.getBingo()
            .getTeams()
            .stream()
            .filter(teamx -> !teamx.getPlayers().isEmpty())
            .sorted(Comparator.<Team>comparingInt(teamx -> teamx.completion().count()).reversed())
            .toList();
         int place = 0;
         int toSkip = 0;
         int prevTeam = -1;

         for (Team team : teams) {
            int completed = team.completion().count();
            toSkip++;
            if (prevTeam != completed) {
               place += toSkip;
               toSkip = 0;
            }

            leaderboard.append(Component.literal(" "));
            leaderboard.append(
               Component.translatable(
                  completed == 1 ? "bongo.completed_tasks.one" : "bongo.completed_tasks.multiple",
                  new Object[]{Integer.toString(place), team.getName(), Integer.toString(completed)}
               )
            );
            leaderboard.append("\n");
            prevTeam = completed;
         }
      }

      event.getLevel()
         .getServer()
         .getPlayerList()
         .getPlayers()
         .forEach(
            player -> {
               player.sendSystemMessage(tcc);
               player.connection.send(new ClientboundSetTitleTextPacket(tc));
               player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 10));
               player.connection
                  .send(
                     new ClientboundSoundPacket(
                        Util.sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 1.2F, 1.0F, 0L
                     )
                  );
               if (leaderboard != null) {
                  player.sendSystemMessage(leaderboard);
               }
            }
         );
   }
}
