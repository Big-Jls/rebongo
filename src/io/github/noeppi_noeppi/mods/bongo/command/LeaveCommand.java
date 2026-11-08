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
import io.github.noeppi_noeppi.mods.bongo.event.BingoChangeTeamEvent;
import io.github.noeppi_noeppi.mods.bongo.util.Messages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

public class LeaveCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Bingo bingo = Bingo.get(player.level());
      if (!bingo.active()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.noactive")).create();
      }

      if (!bingo.running() && !bingo.won()) {
         Team team = bingo.getTeam(player);
         if (team == null) {
            throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.leavonojoin")).create();
         }

         BingoChangeTeamEvent event = new BingoChangeTeamEvent(player, bingo, team, null, Component.translatable("bongo.cmd.team.denied.leave"));
         if (((BingoChangeTeamEvent)NeoForge.EVENT_BUS.post(event)).isCanceled()) {
            throw new SimpleCommandExceptionType(event.getFailureMessage()).create();
         }

         team.removePlayer(player);
         Messages.onLeave(player.level(), player, team);
         return 0;
      } else {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.running")).create();
      }
   }
}
