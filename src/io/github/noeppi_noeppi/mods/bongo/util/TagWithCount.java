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

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.moddingx.libx.annotation.api.Codecs;
import org.moddingx.libx.util.lazy.CachedValue;

public final class TagWithCount {
   private static final Registry<Item> ITEMS = BuiltInRegistries.ITEM;
   public static final Codec<TagWithCount> CODEC = Codecs.get(BingoMod.class, TagWithCount.class);
   private final Identifier id;
   private final TagKey<Item> key;
   private final CachedValue<Predicate<Item>> contains;
   private final CachedValue<List<Item>> itemList;
   private final int count;

   public TagWithCount(Identifier id, int count) {
      this.id = id;
      this.count = count;
      this.key = TagKey.create(Registries.ITEM, id);
      this.contains = new CachedValue(() -> {
         try {
            Named<Item> theTag = ITEMS.getTags().filter(tagValue -> tagValue.key().equals(this.key)).findFirst().orElse(null);
            Predicate<Item> pred;
            if (theTag == null) {
               pred = item -> false;
            } else {
               pred = item -> theTag.contains(ITEMS.wrapAsHolder(item));
            }
            return pred;
         } catch (Exception e) {
            e.printStackTrace();
            return (Predicate<Item>)(item -> false);
         }
      });
      this.itemList = new CachedValue(
         () -> {
            try {
               return ITEMS.getTags()
                  .filter(tagValue -> tagValue.key().equals(this.key))
                  .findFirst()
                  .map(tag -> tag.stream().map(holder -> (Item)holder.value()).toList())
                  .orElse(List.of());
            } catch (Exception e) {
               e.printStackTrace();
               return List.of();
            }
         }
      );
   }

   private TagWithCount(TagWithCount parent, int count) {
      this.id = parent.id;
      this.count = count;
      this.key = parent.key;
      this.contains = parent.contains.copy();
      this.itemList = parent.itemList.copy();
   }

   public Identifier getId() {
      return this.id;
   }

   public TagKey<Item> getKey() {
      return this.key;
   }

   public List<Item> getItems() {
      return (List<Item>)this.itemList.get();
   }

   public boolean contains(Item item) {
      return ((Predicate)this.contains.get()).test(item);
   }

   public int getCount() {
      return this.count;
   }

   public void invalidate() {
      this.contains.invalidate();
      this.itemList.invalidate();
   }

   public TagWithCount withCount(int count) {
      return new TagWithCount(this, count);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TagWithCount that = (TagWithCount)o;
         return this.count == that.count && this.id.equals(that.id);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

   @Override
   public String toString() {
      return "TagWithCount[ " + this.id + " x " + this.count + " ]";
   }
}
