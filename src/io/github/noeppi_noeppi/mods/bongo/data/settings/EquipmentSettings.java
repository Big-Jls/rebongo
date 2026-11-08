/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.data.settings;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.annotation.api.Codecs;

public record EquipmentSettings(
   List<ItemStack> inventory,
   ItemStack head,
   ItemStack chest,
   ItemStack legs,
   ItemStack feet,
   ItemStack offhand,
   List<ItemStack> backpack,
   List<ItemStack> emergency
) {
   public static final Codec<EquipmentSettings> CODEC = Codecs.get(BingoMod.class, EquipmentSettings.class);
   public static final EquipmentSettings DEFAULT = new EquipmentSettings(
      List.of(), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, List.of(), List.of()
   );

   public void equip(Team team, boolean suppressBingoSync) {
      team.clearBackPack(true);

      for (int slot = 0; slot < Math.min(this.backpack().size(), 27); slot++) {
         team.getBackPack().setStackInSlot(slot, this.backpack().get(slot).copy());
      }

      team.markDirty(suppressBingoSync);
   }

   public void equip(ServerPlayer player) {
      player.getInventory().clearContent();

      for (int slot = 0; slot < Math.min(this.inventory().size(), player.getInventory().getNonEquipmentItems().size()); slot++) {
         player.getInventory().getNonEquipmentItems().set(slot, this.inventory().get(slot).copy());
      }

      player.setItemSlot(EquipmentSlot.OFFHAND, this.offhand().copy());
      player.setItemSlot(EquipmentSlot.FEET, this.feet().copy());
      player.setItemSlot(EquipmentSlot.LEGS, this.legs().copy());
      player.setItemSlot(EquipmentSlot.CHEST, this.chest().copy());
      player.setItemSlot(EquipmentSlot.HEAD, this.head().copy());
   }

   public boolean hasEmergencyItems() {
      return !this.emergency().isEmpty();
   }

   public void giveEmergencyItems(ServerPlayer player) {
      for (ItemStack stack : this.emergency()) {
         if (!player.getInventory().add(stack.copy())) {
            player.drop(stack.copy(), false);
         }
      }
   }
}
