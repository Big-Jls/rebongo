/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.command.arg;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.noeppi_noeppi.mods.bongo.data.task.GameTasks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class GameTasksArgument implements ArgumentType<Identifier> {
   @Nullable
   private final Set<Identifier> games;
   private final IdentifierArgument rla = new IdentifierArgument();

   public GameTasksArgument(@Nullable Set<Identifier> games) {
      this.games = games;
   }

   public static GameTasksArgument gameTasks() {
      return new GameTasksArgument(null);
   }

   public Identifier parse(StringReader reader) throws CommandSyntaxException {
      return this.rla.parse(reader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      StringReader reader = new StringReader(context.getInput());
      reader.setCursor(builder.getStart());
      String theString = reader.getRemaining().toLowerCase();

      for (Identifier rl : this.games()) {
         if (rl.toString().toLowerCase().startsWith(theString)) {
            builder.suggest(rl.toString());
         }
      }

      return CompletableFuture.completedFuture(builder.build());
   }

   public Collection<String> getExamples() {
      List<String> examples = new ArrayList<>();

      for (Identifier rl : this.games()) {
         examples.add(rl.toString());
      }

      return examples;
   }

   private Set<Identifier> games() {
      return this.games == null ? GameTasks.gameTasks().keySet() : this.games;
   }

   public static class Info implements ArgumentTypeInfo<GameTasksArgument, GameTasksArgument.Info.Template> {
      public static final GameTasksArgument.Info INSTANCE = new GameTasksArgument.Info();

      private Info() {
      }

      public void serializeToNetwork(@Nonnull GameTasksArgument.Info.Template template, @Nonnull FriendlyByteBuf buffer) {
         buffer.writeInt(template.argument.games().size());

         for (Identifier id : template.argument.games()) {
            buffer.writeIdentifier(id);
         }
      }

      @Nonnull
      public GameTasksArgument.Info.Template deserializeFromNetwork(@Nonnull FriendlyByteBuf buffer) {
         int amount = buffer.readInt();
         Set<Identifier> ids = new HashSet<>();

         for (int i = 0; i < amount; i++) {
            ids.add(buffer.readIdentifier());
         }

         return new GameTasksArgument.Info.Template(new GameTasksArgument(ids));
      }

      public void serializeToJson(@Nonnull GameTasksArgument.Info.Template template, @Nonnull JsonObject json) {
         for (Identifier id : template.argument.games()) {
            json.addProperty(id.toString(), id.toString());
         }
      }

      @Nonnull
      public GameTasksArgument.Info.Template unpack(@Nonnull GameTasksArgument argument) {
         return new GameTasksArgument.Info.Template(argument);
      }

      public class Template implements net.minecraft.commands.synchronization.ArgumentTypeInfo.Template<GameTasksArgument> {
         private final GameTasksArgument argument;

         private Template(GameTasksArgument argument) {
            this.argument = argument;
         }

         @Nonnull
         public ArgumentTypeInfo<GameTasksArgument, ?> type() {
            return Info.this;
         }

         @Nonnull
         public GameTasksArgument instantiate(@Nonnull CommandBuildContext ctx) {
            return this.argument;
         }
      }
   }
}
