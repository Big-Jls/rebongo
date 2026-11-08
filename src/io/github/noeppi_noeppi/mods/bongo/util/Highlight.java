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

import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public abstract sealed class Highlight<T> permits Highlight.Item, Highlight.Advancement {
   protected final T element;

   public Highlight(T element) {
      this.element = element;
   }

   public T element() {
      return this.element;
   }

   public Stream<Highlight.Item> asItem() {
      return Stream.empty();
   }

   public Stream<Highlight.Advancement> asAdvancement() {
      return Stream.empty();
   }

   public static final class Advancement extends Highlight<Identifier> {
      public Advancement(Identifier element) {
         super(element);
      }

      @Override
      public Stream<Highlight.Advancement> asAdvancement() {
         return Stream.of(this);
      }
   }

   public static final class Item extends Highlight<ItemStack> {
      private final Predicate<ItemStack> predicate = stack -> stack.getItem() == this.element.getItem();

      public Item(ItemStack element) {
         super(element.copy());
      }

      public Predicate<ItemStack> predicate() {
         return this.predicate;
      }

      @Override
      public Stream<Highlight.Item> asItem() {
         return Stream.of(this);
      }
   }
}
