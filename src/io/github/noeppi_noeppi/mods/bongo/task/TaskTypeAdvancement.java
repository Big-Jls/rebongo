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
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.ClientAdvancementInfo;
import io.github.noeppi_noeppi.mods.bongo.util.Highlight;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

public class TaskTypeAdvancement implements TaskType<Identifier> {
   public static final TaskTypeAdvancement INSTANCE = new TaskTypeAdvancement();

   private TaskTypeAdvancement() {
   }

   @Override
   public String id() {
      return "bongo.advancement";
   }

   @Override
   public Class<Identifier> taskClass() {
      return Identifier.class;
   }

   @Override
   public MapCodec<Identifier> codec() {
      return Identifier.CODEC.fieldOf("value");
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.advancement.name");
   }

   public Component contentName(Identifier element, @Nullable MinecraftServer server) {
      if (server != null) {
         AdvancementHolder advancement = server.getAdvancements().get(element);
         return (Component)(advancement == null
            ? Component.translatable("bongo.task.advancement.invalid")
            : advancement.value().display().<Component>map(DisplayInfo::getTitle).orElse(Component.translatable("bongo.task.advancement.invalid")));
      } else {
         return (Component)(FMLEnvironment.getDist() == Dist.CLIENT
            ? ClientAdvancementInfo.getTranslation(element)
            : Component.translatable("bongo.task.advancement.invalid"));
      }
   }

   @Override
   public Comparator<Identifier> order() {
      return Util.COMPARE_RESOURCE;
   }

   public void validate(Identifier element, MinecraftServer server) {
      if (server.getAdvancements().get(element) == null) {
         throw new IllegalStateException("Advancement not found: " + element);
      }
   }

   public void sync(Identifier element, MinecraftServer server, @Nullable ServerPlayer target) {
      AdvancementHolder advancement = server.getAdvancements().get(element);
      if (advancement != null) {
         if (target == null) {
            BingoMod.getNetwork().syncAdvancement(advancement);
         } else {
            BingoMod.getNetwork().syncAdvancementTo(advancement, target);
         }
      }
   }

   @Override
   public Stream<Identifier> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? server.getAdvancements().getAllAdvancements().stream().filter(adv -> adv.value().display().isPresent()).map(AdvancementHolder::id)
         : server.getAdvancements()
            .getAllAdvancements()
            .stream()
            .filter(adv -> adv.value().display().isPresent())
            .filter(adv -> player.getAdvancements().getOrStartProgress(adv).isDone())
            .map(AdvancementHolder::id);
   }

   public boolean shouldComplete(ServerPlayer player, Identifier element, Identifier compare) {
      return Objects.equals(element, compare);
   }

   public Stream<Highlight<?>> highlight(Identifier element) {
      return Stream.of(new Highlight.Advancement(element));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      graphics.pose().translate(-1.0F, -1.0F);
      graphics.pose().scale(0.7692308F, 0.7692308F);
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 0, 18, 26, 26, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, Identifier element, boolean bigBongo) {
      ItemStack icon = ClientAdvancementInfo.getDisplay(element);
      ItemRenderUtil.renderItem(graphics, icon, false);
   }
}
