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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TeamsCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      Player player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Level level = ((CommandSourceStack)context.getSource()).getLevel();
      Bingo bingo = Bingo.get(level);
      if (!bingo.active()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.team.noactive")).create();
      }

      for (Team team : bingo.getTeams()) {
         if (!team.getPlayers().isEmpty()) {
            MutableComponent tc = Component.translatable("bongo.cmd.spread.added");
            tc.append(team.getName()).append(Component.literal(":"));
            level.getServer().getPlayerList().getPlayers().forEach(teamPlayer -> {
               if (team.hasPlayer(teamPlayer)) {
                  tc.append(Component.literal(" ")).append(teamPlayer.getDisplayName());
               }
            });
            player.sendSystemMessage(tc);
         }
      }

      return 0;
   }
}
