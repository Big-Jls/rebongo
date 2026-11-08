/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

public class ClientAdvancementInfo {
   private static final Map<Identifier, Pair<ItemStack, Component>> CACHE = new HashMap<>();

   public static ItemStack getDisplay(Identifier id) {
      return CACHE.containsKey(id) ? (ItemStack)CACHE.get(id).getLeft() : new ItemStack(Items.BARRIER);
   }

   public static Component getTranslation(Identifier id) {
      return (Component)(CACHE.containsKey(id) ? (Component)CACHE.get(id).getRight() : Component.translatable("bongo.task.advancement.invalid"));
   }

   public static void updateAdvancementInfo(Identifier id, ItemStack display, Component translation) {
      CACHE.put(id, Pair.of(display, translation));
   }
}
