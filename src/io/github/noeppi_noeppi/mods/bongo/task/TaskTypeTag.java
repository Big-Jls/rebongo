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
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.Highlight;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.TagWithCount;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.render.ClientTickHandler;
import org.moddingx.libx.util.Misc;
import org.moddingx.libx.util.game.ComponentUtil;

public class TaskTypeTag implements TaskType<TagWithCount> {
   public static final TaskTypeTag INSTANCE = new TaskTypeTag();

   private TaskTypeTag() {
   }

   @Override
   public String id() {
      return "bongo.tag";
   }

   @Override
   public Class<TagWithCount> taskClass() {
      return TagWithCount.class;
   }

   @Override
   public MapCodec<TagWithCount> codec() {
      return TagWithCount.CODEC.fieldOf("value");
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.item.name");
   }

   public Component contentName(TagWithCount element, @Nullable MinecraftServer server) {
      return Component.literal(Util.resourceStr(element.getId()));
   }

   @Override
   public Comparator<TagWithCount> order() {
      return Comparator.comparing(TagWithCount::getId, Util.COMPARE_RESOURCE).thenComparingInt(TagWithCount::getCount);
   }

   public void validate(TagWithCount element, MinecraftServer server) {
      if (!element.getId().equals(Misc.MISSINGNO)) {
         if (BuiltInRegistries.ITEM.getTags().noneMatch(tagValue -> tagValue.key().equals(element.getKey()))) {
            throw new IllegalStateException("Unknown tag: " + element.getId());
         }

         if (!BuiltInRegistries.ITEM.getTagOrEmpty(element.getKey()).iterator().hasNext()) {
            throw new IllegalStateException("Empty tag: " + element.getId());
         }
      }
   }

   @Override
   public Stream<TagWithCount> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? BuiltInRegistries.ITEM.getTags().map(tagValue -> new TagWithCount(tagValue.key().location(), 1))
         : player.getInventory()
            .getNonEquipmentItems()
            .stream()
            .filter(stack -> !stack.isEmpty())
            .flatMap(stack -> BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).tags())
            .distinct()
            .map(key -> new TagWithCount(key.location(), 1));
   }

   public boolean shouldComplete(ServerPlayer player, TagWithCount element, TagWithCount compare) {
      return element.getId().equals(compare.getId()) && element.getCount() <= compare.getCount();
   }

   public void consume(ServerPlayer player, TagWithCount element, TagWithCount found) {
      Util.removeItems(player, element.getCount(), stack -> element.contains(stack.getItem()));
   }

   public Stream<Highlight<?>> highlight(TagWithCount element) {
      return element.getItems().stream().<ItemStack>map(ItemStack::new).map(Highlight.Item::new);
   }

   public void invalidate(TagWithCount element) {
      element.invalidate();
   }

   @OnlyIn(Dist.CLIENT)
   public FormattedCharSequence renderDisplayName(Minecraft mc, TagWithCount element) {
      ItemStack stack = cycle(element);
      if (stack == null) {
         return Component.translatable("bongo.task.tag.empty").getVisualOrderText();
      }

      FormattedCharSequence name = ComponentUtil.subSequence(stack.getHoverName().getVisualOrderText(), 0, 16);
      return element.getCount() != 1 ? FormattedCharSequence.composite(name, FormattedCharSequence.forward(" x " + element.getCount(), Style.EMPTY)) : name;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 0, 0, 18, 18, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, TagWithCount element, boolean bigBongo) {
      ItemStack stack = cycle(element);
      ItemRenderUtil.renderItem(graphics, stack == null ? new ItemStack(Items.BARRIER) : stack, !bigBongo);
   }

   @Nullable
   private static ItemStack cycle(TagWithCount element) {
      List<Item> items = element.getItems();
      if (items.isEmpty()) {
         return null;
      }

      ItemStack stack = new ItemStack((ItemLike)items.get(ClientTickHandler.ticksInGame() / 20 % items.size()));
      stack.setCount(element.getCount());
      return stack;
   }
}
