/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.data.GameDef;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.data.task.GameTasks;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import org.moddingx.libx.command.CommandUtil;
import org.moddingx.libx.util.game.ServerMessages;

public class CreateCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      Bingo bingo = Bingo.get(level);
      if (bingo.running()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.create.running")).create();
      }

      Identifier tasksId = (Identifier)context.getArgument("tasks", Identifier.class);
      List<Identifier> settingIds = (List<Identifier>)CommandUtil.getArgumentOrDefault(context, "settings", List.class, List.of());
      GameTasks tasks = GameTasks.gameTasks().get(tasksId);
      if (tasks == null) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.create.notfound")).create();
      }

      GameSettings settings;
      try {
         settings = GameSettings.load(settingIds);
      } catch (NoSuchElementException e) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.create.notfound")).create();
      } catch (Exception e) {
         e.printStackTrace();
         throw new SimpleCommandExceptionType(Component.literal("Error while merging settings: " + e.getMessage())).create();
      }

      GameDef gd = new GameDef(tasks, settings);
      Map<DyeColor, List<UUID>> previousTeams = new EnumMap<>(DyeColor.class);

      for (Team team : bingo.getTeams()) {
         if (!team.getPlayers().isEmpty()) {
            previousTeams.put(team.color, List.copyOf(team.getPlayers()));
         }
      }

      bingo.stop();
      bingo.reset();
      String err = gd.createBingo(bingo);
      if (err != null) {
         throw new SimpleCommandExceptionType(Component.translatable(err)).create();
      }

      bingo.activate();

      for (Entry<DyeColor, List<UUID>> entry : previousTeams.entrySet()) {
         Team team = bingo.getTeam(entry.getKey());

         for (UUID uid : entry.getValue()) {
            ServerPlayer player = ((CommandSourceStack)context.getSource()).getServer().getPlayerList().getPlayer(uid);
            if (player != null) {
               team.addPlayer(player);
            }
         }
      }

      ServerMessages.broadcast(
         level,
         Component.translatable("bongo.info")
            .append(((CommandSourceStack)context.getSource()).getDisplayName())
            .append(Component.translatable("bongo.cmd.create.done"))
      );
      return 0;
   }
}
