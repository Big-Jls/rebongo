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

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public class StackTagReader {
   public static final Codec<ItemStack> SAFE_ITEM_STACK = ItemStack.OPTIONAL_CODEC;
   public static final Codec<ItemStack> DIRECT_STACK_CODEC = ItemStack.CODEC;

   public static ItemStack fromNBT(CompoundTag nbt) {
      return ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElse(ItemStack.EMPTY);
   }

   public static ItemStack fromJson(JsonObject json) {
      return ItemStack.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(ItemStack.EMPTY);
   }
}
