/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.util;

import io.github.noeppi_noeppi.mods.bongo.data.Team;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.moddingx.libx.util.game.ServerMessages;

public class Messages {
   public static void onJoin(Level level, Player player, Team team) {
      player.sendSystemMessage(Component.translatable("bongo.cmd.team.joined").append(team.getName()));
      ServerMessages.broadcastTo(
         level,
         p -> p != player,
         ((MutableComponent)player.getDisplayName()).append(Component.translatable("bongo.cmd.team.joinedother").append(team.getName()))
      );
   }

   public static void onLeave(Level level, Player player, Team team) {
      player.sendSystemMessage(Component.translatable("bongo.cmd.team.left").append(team.getName()));
      ServerMessages.broadcastTo(
         level, p -> p != player, ((MutableComponent)player.getDisplayName()).append(Component.translatable("bongo.cmd.team.leftother").append(team.getName()))
      );
   }
}
