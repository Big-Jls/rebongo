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

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JsonOps;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import io.github.noeppi_noeppi.mods.bongo.task.TaskType;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypes;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.OpenFile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.moddingx.libx.command.CommandUtil;

public class DumpCommand implements Command<CommandSourceStack> {
   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      try {
         MinecraftServer server = ((CommandSourceStack)context.getSource()).getServer();
         Path base = server.getServerDirectory().resolve("bingo-dump");
         if (!Files.exists(base)) {
            Files.createDirectories(base);
         }

         if (!Files.exists(base.resolve("bingo_tasks"))) {
            Files.createDirectories(base.resolve("bingo_tasks"));
         }

         if (!Files.exists(base.resolve("bingo_settings"))) {
            Files.createDirectories(base.resolve("bingo_settings"));
         }

         int types = 0;
         ServerPlayer playerForListing = CommandUtil.getArgumentOrDefault(context, "everything", Boolean.class, true)
            ? null
            : ((CommandSourceStack)context.getSource()).getPlayerOrException();

         for (TaskType<?> type : TaskTypes.getTypes()) {
            JsonArray data = listElements(type, server, playerForListing);
            JsonObject json = new JsonObject();
            json.add("tasks", data);
            Path path = base.resolve("bingo_tasks").resolve(type.id().replace(".", "-") + ".json");
            BufferedWriter w = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            w.write(BingoMod.PRETTY_GSON.toJson(json));
            w.close();
            types++;
         }

         JsonElement json = null;

         try {
            json = (JsonElement)GameSettings.CODEC.encodeStart(JsonOps.INSTANCE, GameSettings.DEFAULT).getOrThrow(err -> new IllegalStateException(err));
         } catch (Exception var12) {
         }

         if (json != null) {
            Path path = base.resolve("bingo_settings").resolve("default.json");
            BufferedWriter w = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            w.write(BingoMod.PRETTY_GSON.toJson(json));
            w.close();
         }

         Component dumped = Component.literal("Dumped data for " + types + " task types to " + base.toAbsolutePath().normalize())
               .withStyle(Style.EMPTY.withClickEvent(new OpenFile(base.toAbsolutePath().normalize())).withUnderlined(true));
         ((CommandSourceStack)context.getSource())
            .sendSuccess(
               () -> dumped,
               true
            );
         return 0;
      } catch (IOException e) {
         throw new SimpleCommandExceptionType(Component.literal("IOException: " + e.getMessage())).create();
      }
   }

   private static <T> JsonArray listElements(TaskType<T> type, MinecraftServer server, ServerPlayer player) {
      JsonArray array = new JsonArray();
      type.listElements(server, player).sorted(type.order()).map(elem -> new Task(type, (T)elem)).flatMap(task -> {
         try {
            return Stream.of(Task.CODEC.encodeStart(JsonOps.INSTANCE, task).getOrThrow(err -> new IllegalStateException(err)));
         } catch (Exception e) {
            e.printStackTrace();
            return Stream.empty();
         }
      }).forEach(array::add);
      return array;
   }
}
