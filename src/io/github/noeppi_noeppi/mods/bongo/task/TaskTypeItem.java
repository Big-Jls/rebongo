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
import io.github.noeppi_noeppi.mods.bongo.util.StackTagReader;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.base.ItemBase;

public class TaskTypeItem implements TaskType<ItemStack> {
   public static final TaskTypeItem INSTANCE = new TaskTypeItem();

   private TaskTypeItem() {
   }

   @Override
   public String id() {
      return "bongo.item";
   }

   @Override
   public Class<ItemStack> taskClass() {
      return ItemStack.class;
   }

   @Override
   public MapCodec<ItemStack> codec() {
      return StackTagReader.DIRECT_STACK_CODEC.fieldOf("value");
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.item.name");
   }

   public Component contentName(ItemStack element, @Nullable MinecraftServer server) {
      Identifier id = BuiltInRegistries.ITEM.getKey(element.getItem());
      if (id != null && "minecraft".equals(id.getNamespace()) && id.getPath().startsWith("music_disc_")) {
         return Component.empty()
            .append(element.getHoverName())
            .append(Component.literal(" (" + id.getPath().substring("music_disc_".length()) + ")"));
      }

      return element.getHoverName();
   }

   @Override
   public Comparator<ItemStack> order() {
      return Comparator.<ItemStack, Identifier>comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()), Util.COMPARE_RESOURCE)
         .thenComparingInt(ItemStack::getCount);
   }

   @Override
   public Stream<ItemStack> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? BuiltInRegistries.ITEM
            .stream()
            .flatMap(
               item -> {
                  if (item instanceof ItemBase base) {
                     return Stream.of(new ItemStack(base));
                  } else {
                     return item instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockBase base
                        ? Stream.of(new ItemStack(base))
                        : Stream.of(new ItemStack(item));
                  }
               }
            )
            .filter(stack -> !stack.isEmpty())
         : player.getInventory().getNonEquipmentItems().stream().filter(stack -> !stack.isEmpty());
   }

   /**
    * 比较物品是否满足任务要求。生物桶（鲑鱼桶、鳕鱼桶、蝌蚪桶、热带鱼桶、美西螈桶等）在装到生物后会写入
    * 变体数据（BUCKET_ENTITY_DATA、SALMON_SIZE、TROPICAL_FISH_*、AXOLOTL_VARIANT 等），而任务物品没有这些数据，
    * 导致 isSameItemSameComponents 永远不相等、任务无法完成；这里在任一方携带变体数据时先剥离再比较。
    * 硫方怪桶还会额外写入 SULFUR_CUBE_CONTENT（吞噬的方块），同样需要忽略。
    */
   public static boolean sameForTask(ItemStack a, ItemStack b) {
      if (ItemStack.isSameItemSameComponents(a, b)) {
         return true;
      }

      if (!isBucketVariant(a) && !isBucketVariant(b)) {
         return false;
      }

      ItemStack copyA = a.copy();
      ItemStack copyB = b.copy();
      stripBucketVariant(copyA);
      stripBucketVariant(copyB);
      return ItemStack.isSameItemSameComponents(copyA, copyB);
   }

   // 生物桶的变体数据：实体数据 / 鲑鱼体型 / 热带鱼花纹与颜色 / 美西螈变种 / 硫方怪吞噬的方块
   private static boolean isBucketVariant(ItemStack stack) {
      return stack.has(DataComponents.BUCKET_ENTITY_DATA)
         || stack.has(DataComponents.SULFUR_CUBE_CONTENT)
         || stack.has(DataComponents.SALMON_SIZE)
         || stack.has(DataComponents.TROPICAL_FISH_PATTERN)
         || stack.has(DataComponents.TROPICAL_FISH_BASE_COLOR)
         || stack.has(DataComponents.TROPICAL_FISH_PATTERN_COLOR)
         || stack.has(DataComponents.AXOLOTL_VARIANT);
   }

   private static void stripBucketVariant(ItemStack stack) {
      stack.remove(DataComponents.BUCKET_ENTITY_DATA);
      stack.remove(DataComponents.SULFUR_CUBE_CONTENT);
      stack.remove(DataComponents.SALMON_SIZE);
      stack.remove(DataComponents.TROPICAL_FISH_PATTERN);
      stack.remove(DataComponents.TROPICAL_FISH_BASE_COLOR);
      stack.remove(DataComponents.TROPICAL_FISH_PATTERN_COLOR);
      stack.remove(DataComponents.AXOLOTL_VARIANT);
   }

   public boolean shouldComplete(ServerPlayer player, ItemStack element, ItemStack compare) {
      return sameForTask(element, compare) && element.getCount() <= compare.getCount();
   }

   public void consume(ServerPlayer player, ItemStack element, ItemStack found) {
      Util.removeItems(player, element.getCount(), stack -> sameForTask(element, stack));
   }

   public Stream<Highlight<?>> highlight(ItemStack element) {
      return Stream.of(new Highlight.Item(element));
   }

   @OnlyIn(Dist.CLIENT)
   public FormattedCharSequence renderDisplayName(Minecraft mc, ItemStack element) {
      FormattedCharSequence name = TaskType.super.renderDisplayName(mc, element);
      return element.getCount() != 1 ? FormattedCharSequence.composite(name, FormattedCharSequence.forward(" x " + element.getCount(), Style.EMPTY)) : name;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 0, 0, 18, 18, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, ItemStack element, boolean bigBongo) {
      ItemRenderUtil.renderItem(graphics, element, !bigBongo);
   }
}
