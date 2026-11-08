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
import io.github.noeppi_noeppi.mods.bongo.util.Highlight;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.util.game.ComponentUtil;

public interface TaskType<T> {
   String id();

   Class<T> taskClass();

   MapCodec<T> codec();

   Component name();

   Component contentName(T var1, @Nullable MinecraftServer var2);

   Comparator<T> order();

   default void validate(T element, MinecraftServer server) {
   }

   default void sync(T element, MinecraftServer server, @Nullable ServerPlayer target) {
   }

   Stream<T> listElements(MinecraftServer var1, @Nullable ServerPlayer var2);

   boolean shouldComplete(ServerPlayer var1, T var2, T var3);

   default void consume(ServerPlayer player, T element, T found) {
   }

   default Stream<Highlight<?>> highlight(T element) {
      return Stream.empty();
   }

   default void invalidate(T element) {
   }

   @OnlyIn(Dist.CLIENT)
   default FormattedCharSequence renderDisplayName(Minecraft mc, T element) {
      return ComponentUtil.subSequence(this.contentName(element, null).getVisualOrderText(), 0, 16);
   }

   @OnlyIn(Dist.CLIENT)
   void renderSlot(Minecraft var1, GuiGraphicsExtractor var2);

   @OnlyIn(Dist.CLIENT)
   void renderSlotContent(Minecraft var1, GuiGraphicsExtractor var2, T var3, boolean var4);
}
