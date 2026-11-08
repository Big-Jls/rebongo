/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.task;

import com.mojang.serialization.MapCodec;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TaskTypeEmpty implements TaskType<Unit> {
   public static final TaskTypeEmpty INSTANCE = new TaskTypeEmpty();

   private TaskTypeEmpty() {
   }

   @Override
   public String id() {
      return "bongo.empty";
   }

   @Override
   public Class<Unit> taskClass() {
      return Unit.class;
   }

   @Override
   public MapCodec<Unit> codec() {
      return MapCodec.unit(Unit.INSTANCE);
   }

   @Override
   public Component name() {
      return Component.literal("Empty");
   }

   public Component contentName(Unit element, @Nullable MinecraftServer server) {
      return Component.empty();
   }

   @Override
   public Comparator<Unit> order() {
      return Comparator.comparing(Enum::ordinal);
   }

   @Override
   public Stream<Unit> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return Stream.of(Unit.INSTANCE);
   }

   public boolean shouldComplete(ServerPlayer player, Unit element, Unit compare) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, Unit element, boolean bigBongo) {
   }
}
