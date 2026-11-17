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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
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
import net.minecraft.world.item.component.ItemLore;
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
    * 比较物品是否满足任务要求。required 是任务定义的物品，found 是玩家实际持有（或用于判定）的物品。
    *
    * 26.2 把大量物品状态放进了数据组件：耐久（DAMAGE / REPAIR_COST）、附魔（ENCHANTMENTS）、
    * 药水内容（POTION_CONTENTS）、烟花数据（FIREWORKS / FIREWORK_EXPLOSION）、旗帜与盾牌图案
    * （BANNER_PATTERNS / BASE_COLOR）、蜂巢蜜蜂（BEES）、潜影盒与收纳袋内容（CONTAINER /
    * BUNDLE_CONTENTS）、盔甲纹饰（TRIM）、山羊角乐器（INSTRUMENT）、地图编号（MAP_ID）、
    * 弩装填（CHARGED_PROJECTILES）、书本内容（WRITTEN_BOOK_CONTENT / WRITABLE_BOOK_CONTENT /
    * STORED_ENCHANTMENTS / RECIPES）、生物桶变体（BUCKET_ENTITY_DATA / SALMON_SIZE /
    * TROPICAL_FISH_* / AXOLOTL_VARIANT / SULFUR_CUBE_CONTENT）、方块实体数据（BLOCK_ENTITY_DATA /
    * POT_DECORATIONS）等。任务定义通常只写物品 id（没有显式组件），逐组件比较会让这些物品
    * 永远无法完成任务。
    *
    * 规则：
    * 1. 任务没有显式组件时，只比较物品类型，上述组件全部忽略；
    * 2. 任务显式指定组件时，只比较任务指定的组件，未指定的组件仍忽略（支持“指定药水/指定附魔”等任务）；
    * 3. 带命名、Lore、custom_data 或无法破坏标记的物品视为特殊/技能道具，不参与普通任务，
    *    避免海克斯发放的命名道具（钓鱼佬鱼竿、海神三叉戟等）直接点亮任务。
    */
   public static boolean sameForTask(ItemStack required, ItemStack found) {
      if (!ItemStack.isSameItem(required, found)) {
         return false;
      }

      DataComponentPatch spec = required.getComponentsPatch();
      if (spec.isEmpty()) {
         return !isSpecialItem(found);
      }

      for (Map.Entry<DataComponentType<?>, Optional<?>> entry : spec.entrySet()) {
         if (!componentMatches(found, entry.getKey(), entry.getValue())) {
            return false;
         }
      }

      return true;
   }

   private static boolean componentMatches(ItemStack stack, DataComponentType<?> type, Optional<?> expected) {
      return Objects.equals(stack.get((DataComponentType)type), expected.orElse(null));
   }

   // 特殊/技能道具：命名、非空 Lore、海克斯发放标记（custom_data）或无法破坏。
   // 注意：26.2 所有物品的原型都带一个空的 LORE，必须检查 lines 是否为空，不能只用 has。
   private static boolean isSpecialItem(ItemStack stack) {
      if (stack.has(DataComponents.CUSTOM_NAME)
         || stack.has(DataComponents.CUSTOM_DATA)
         || stack.has(DataComponents.UNBREAKABLE)) {
         return true;
      }
      ItemLore lore = stack.get(DataComponents.LORE);
      return lore != null && !lore.lines().isEmpty();
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
