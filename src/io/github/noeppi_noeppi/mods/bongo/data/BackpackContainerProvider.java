/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BackpackContainerProvider implements MenuProvider {
   private final Component team;
   private final ItemStackHandler handler;
   private final Runnable dirty;

   public BackpackContainerProvider(Component team, ItemStackHandler handler, Runnable dirty) {
      this.team = team;
      this.handler = handler;
      this.dirty = dirty;
   }

   @Nonnull
   public Component getDisplayName() {
      return Component.translatable("bongo.backpack").append(this.team);
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
      return ChestMenu.sixRows(containerId, inventory, new BackpackContainerProvider.HandlerContainer(this.handler, this.dirty));
   }

   private static class HandlerContainer implements Container {
      private final ItemStackHandler handler;
      private final Runnable dirty;

      private HandlerContainer(ItemStackHandler handler, Runnable dirty) {
         this.handler = handler;
         this.dirty = dirty;
      }

      public int getContainerSize() {
         return this.handler.getSlots();
      }

      public boolean isEmpty() {
         for (int i = 0; i < this.handler.getSlots(); i++) {
            if (!this.handler.getStackInSlot(i).isEmpty()) {
               return false;
            }
         }

         return true;
      }

      @Nonnull
      public ItemStack getItem(int slot) {
         return this.handler.getStackInSlot(slot);
      }

      @Nonnull
      public ItemStack removeItem(int slot, int amount) {
         ItemStack stack = this.handler.extractItem(slot, amount, false);
         if (!stack.isEmpty()) {
            this.dirty.run();
         }

         return stack;
      }

      @Nonnull
      public ItemStack removeItemNoUpdate(int slot) {
         return this.handler.getStackInSlot(slot);
      }

      public void setItem(int slot, @Nonnull ItemStack stack) {
         this.handler.setStackInSlot(slot, stack);
         this.dirty.run();
      }

      public void setChanged() {
         this.dirty.run();
      }

      public boolean stillValid(@Nonnull Player player) {
         return true;
      }

      public void clearContent() {
         for (int i = 0; i < this.handler.getSlots(); i++) {
            this.handler.setStackInSlot(i, ItemStack.EMPTY);
         }

         this.dirty.run();
      }
   }
}
