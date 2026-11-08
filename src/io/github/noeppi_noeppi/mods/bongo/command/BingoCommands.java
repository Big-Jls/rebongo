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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.noeppi_noeppi.mods.bongo.command.arg.GameSettingsArgument;
import io.github.noeppi_noeppi.mods.bongo.command.arg.GameTasksArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.moddingx.libx.command.EnumArgumentIgnoreCase;

public class BingoCommands {
   public static void register(RegisterCommandsEvent var0) {
      var0.getDispatcher().register((LiteralArgumentBuilder)Commands.literal("bp").executes(new BackPackCommand()));
      var0.getDispatcher().register((LiteralArgumentBuilder)Commands.literal("tc").executes(new TeamChatCommand()));
      var0.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                      "bingo"
                                                   )
                                                   .then(Commands.literal("backpack").executes(new BackPackCommand())))
                                                .then(
                                                   Commands.literal("join")
                                                      .then(
                                                         Commands.argument("team", EnumArgumentIgnoreCase.enumArgument(DyeColor.class))
                                                            .executes(new JoinCommand())
                                                      )
                                                ))
                                             .then(Commands.literal("leave").executes(new LeaveCommand())))
                                          .then(
                                             ((LiteralArgumentBuilder)Commands.literal("create")
                                                   .requires(var0x -> var0x.permissions().hasPermission(new HasCommandLevel(PermissionLevel.GAMEMASTERS))))
                                                .then(
                                                   ((RequiredArgumentBuilder)Commands.argument("tasks", GameTasksArgument.gameTasks())
                                                         .executes(new CreateCommand()))
                                                      .then(Commands.argument("settings", GameSettingsArgument.gameSettings()).executes(new CreateCommand()))
                                                )
                                          ))
                                       .then(Commands.literal("start").executes(new StartCommand())))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("stop")
                                             .requires(var0x -> var0x.permissions().hasPermission(new HasCommandLevel(PermissionLevel.GAMEMASTERS))))
                                          .executes(new StopCommand())
                                    ))
                                 .then(
                                    Commands.literal("spread")
                                       .then(Commands.argument("amount", IntegerArgumentType.integer(1, 16)).executes(new SpreadCommand()))
                                 ))
                              .then(Commands.literal("teams").executes(new TeamsCommand())))
                           .then(Commands.literal("teamchat").executes(new TeamChatCommand())))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("dump")
                                    .requires(var0x -> var0x.permissions().hasPermission(new HasCommandLevel(PermissionLevel.GAMEMASTERS))))
                                 .executes(new DumpCommand()))
                              .then(Commands.argument("everything", BoolArgumentType.bool()).executes(new DumpCommand()))
                        ))
                     .then(Commands.literal("teleport").then(Commands.argument("target", EntityArgument.player()).executes(new TeleportCommand()))))
                  .then(Commands.literal("tp").then(Commands.argument("target", EntityArgument.player()).executes(new TeleportCommand()))))
               .then(Commands.literal("emergency").executes(new EmergencyCommand()))
         );
   }
}
