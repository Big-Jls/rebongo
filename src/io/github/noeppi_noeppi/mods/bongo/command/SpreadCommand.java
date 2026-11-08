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
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.event.BingoChangeManyTeamsEvent;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.util.game.ServerMessages;

public class SpreadCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      Level level = ((CommandSourceStack)context.getSource()).getLevel();
      Bingo bingo = Bingo.get(level);
      int teams = Mth.clamp((Integer)context.getArgument("amount", Integer.class), 1, 16);
      List<Player> players = new ArrayList<>(level.getServer().getPlayerList().getPlayers());
      if (!bingo.active()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.noactive")).create();
      }

      if (!bingo.running() && !bingo.won()) {
         if (teams > players.size()) {
            throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.spread.less", new Object[]{teams})).create();
         }

         Set<Team> teamSet = Util.PREFERRED_COLOR_ORDER.subList(0, teams).stream().map(bingo::getTeam).collect(Collectors.toSet());
         BingoChangeManyTeamsEvent event = new BingoChangeManyTeamsEvent(bingo, teamSet, Component.translatable("bongo.cmd.team.denied.many"));
         if (((BingoChangeManyTeamsEvent)NeoForge.EVENT_BUS.post(event)).isCanceled()) {
            throw new SimpleCommandExceptionType(event.getFailureMessage()).create();
         }

         int perTeam = players.size() / teams;
         int teamsWithOneMore = players.size() % teams;
         Random random = new Random();

         for (int i = 0; i < teams; i++) {
            Team team = bingo.getTeam(Util.PREFERRED_COLOR_ORDER.get(i));
            team.clearPlayers();
            int playersThisTeam = i < teamsWithOneMore ? perTeam + 1 : perTeam;
            List<Player> added = new ArrayList<>();

            for (int j = 0; j < playersThisTeam; j++) {
               Player player = players.remove(random.nextInt(players.size()));
               team.addPlayer(player);
               added.add(player);
            }

            MutableComponent tc = Component.translatable("bongo.cmd.spread.added");
            tc.append(team.getName()).append(Component.literal(":"));

            for (Player player : added) {
               tc.append(Component.literal(" ")).append(player.getDisplayName());
            }

            ServerMessages.broadcast(level, tc);
         }

         return 0;
      } else {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.running")).create();
      }
   }
}
