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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.util.game.ServerMessages;

public class StopCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      Bingo bingo = Bingo.get(((CommandSourceStack)context.getSource()).getLevel());
      if (!bingo.active()) {
         throw new SimpleCommandExceptionType(Component.translatable("bongo.cmd.stop.notrunning")).create();
      }

      bingo.stop();
      ServerMessages.broadcast(
         ((CommandSourceStack)context.getSource()).getLevel(),
         Component.translatable("bongo.info")
            .append(((CommandSourceStack)context.getSource()).getDisplayName())
            .append(Component.translatable("bongo.cmd.stop.done"))
      );
      return 0;
   }
}
