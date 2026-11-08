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
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
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

public class GameSettingsArgument implements ArgumentType<List<Identifier>> {
   @Nullable
   private final Set<Identifier> games;
   private final IdentifierArgument rla = new IdentifierArgument();

   public GameSettingsArgument(@Nullable Set<Identifier> games) {
      this.games = games;
   }

   public static GameSettingsArgument gameSettings() {
      return new GameSettingsArgument(null);
   }

   public List<Identifier> parse(StringReader reader) throws CommandSyntaxException {
      List<Identifier> list = new ArrayList<>();

      while (true) {
         reader.skipWhitespace();
         if (!reader.canRead()) {
            return List.copyOf(list);
         }

         Identifier rl = this.rla.parse(reader);
         list.add(rl);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      StringReader reader = new StringReader(context.getInput());
      reader.setCursor(builder.getStart());
      String theString = reader.getRemaining().toLowerCase();
      String start;
      String current;
      if (theString.contains(" ")) {
         start = theString.substring(0, theString.lastIndexOf(32) + 1);
         current = theString.substring(theString.lastIndexOf(32) + 1);
      } else {
         start = "";
         current = theString;
      }

      for (Identifier rl : this.games()) {
         if (rl.toString().toLowerCase().startsWith(current)) {
            builder.suggest(start + rl);
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
      return this.games == null ? GameSettings.gameSettings() : this.games;
   }

   public static class Info implements ArgumentTypeInfo<GameSettingsArgument, GameSettingsArgument.Info.Template> {
      public static final GameSettingsArgument.Info INSTANCE = new GameSettingsArgument.Info();

      private Info() {
      }

      public void serializeToNetwork(@Nonnull GameSettingsArgument.Info.Template template, @Nonnull FriendlyByteBuf buffer) {
         buffer.writeInt(template.argument.games().size());

         for (Identifier rl : template.argument.games()) {
            buffer.writeIdentifier(rl);
         }
      }

      @Nonnull
      public GameSettingsArgument.Info.Template deserializeFromNetwork(@Nonnull FriendlyByteBuf buffer) {
         int amount = buffer.readInt();
         Set<Identifier> ids = new HashSet<>();

         for (int i = 0; i < amount; i++) {
            ids.add(buffer.readIdentifier());
         }

         return new GameSettingsArgument.Info.Template(new GameSettingsArgument(ids));
      }

      public void serializeToJson(@Nonnull GameSettingsArgument.Info.Template template, @Nonnull JsonObject json) {
         for (Identifier id : template.argument.games()) {
            json.addProperty(id.toString(), id.toString());
         }
      }

      @Nonnull
      public GameSettingsArgument.Info.Template unpack(@Nonnull GameSettingsArgument argument) {
         return new GameSettingsArgument.Info.Template(argument);
      }

      public class Template implements net.minecraft.commands.synchronization.ArgumentTypeInfo.Template<GameSettingsArgument> {
         private final GameSettingsArgument argument;

         private Template(GameSettingsArgument argument) {
            this.argument = argument;
         }

         @Nonnull
         public ArgumentTypeInfo<GameSettingsArgument, ?> type() {
            return Info.this;
         }

         @Nonnull
         public GameSettingsArgument instantiate(@Nonnull CommandBuildContext ctx) {
            return this.argument;
         }
      }
   }
}
