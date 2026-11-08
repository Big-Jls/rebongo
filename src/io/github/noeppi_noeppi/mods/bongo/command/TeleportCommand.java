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
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TeleportCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Bingo bingo = Bingo.get(player.level());
      Team team = bingo.getTeam(player);
      if (!bingo.running()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.tp.noactive")).create();
      }

      if (team == null) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.tp.noteam")).create();
      }

      EntitySelector sel = (EntitySelector)context.getArgument("target", EntitySelector.class);
      ServerPlayer target = sel.findSinglePlayer((CommandSourceStack)context.getSource());
      if (target.getUUID().equals(player.getUUID())) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.tp.self")).create();
      }

      if (!team.hasPlayer(target)) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.tp.wrongteam")).create();
      }

      if (team.consumeTeleport()) {
         if (player.level() != target.level()) {
            player.teleportTo(target.level(), target.getX(), target.getY(), target.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);
         } else {
            player.teleportTo(target.getX(), target.getY(), target.getZ());
         }

         Util.broadcastTeam(
            player.level(), team, Component.translatable("bongo.cmd.tp.success", new Object[]{player.getDisplayName(), target.getDisplayName()})
         );
         return 0;
      } else {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.tp.noleft")).create();
      }
   }
}
