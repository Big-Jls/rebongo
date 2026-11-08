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
import net.minecraft.server.level.ServerPlayer;

public class EmergencyCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Bingo bingo = Bingo.get(player.level());
      Team team = bingo.getTeam(player);
      if (team == null) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.emergency.noteam")).create();
      }

      if (!bingo.running()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.emergency.norun")).create();
      }

      if (!bingo.getSettings().equipment().hasEmergencyItems()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.emergency.disabled")).create();
      }

      if (team.redeemedEmergency()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.emergency.duplicate")).create();
      }

      if (!team.lockRandomTasks(3)) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.emergency.lesstasks")).create();
      }

      bingo.getSettings().equipment().giveEmergencyItems(player);
      team.redeemedEmergency(true);
      player.sendSystemMessage(Component.translatable("bongo.cmd.emergency.redeemed"));
      return 0;
   }
}
