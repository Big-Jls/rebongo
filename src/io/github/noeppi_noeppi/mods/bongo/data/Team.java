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

import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class Team {
   private final Bingo bingo;
   public final DyeColor color;
   private int completed;
   private int locked;
   private int teleportsLeft;
   private boolean redeemedEmergency;
   private final List<UUID> players;
   private final ItemStackHandler backpack;
   private TeamCompletion completion = null;

   public Team(Bingo bingo, DyeColor color) {
      this.bingo = bingo;
      this.color = color;
      this.completed = 0;
      this.locked = 0;
      this.teleportsLeft = 0;
      this.players = new ArrayList<>();
      this.backpack = new ItemStackHandler(54);
   }

   public MutableComponent getName() {
      return Component.translatable("bongo.team." + this.color.getSerializedName()).withStyle(Util.getTextFormatting(this.color));
   }

   public Style getFormatting() {
      return Util.getTextFormatting(this.color);
   }

   public boolean completed(int slot) {
      return (this.completed & 1 << slot % 25) > 0;
   }

   public void complete(int slot) {
      this.completed |= 1 << slot % 25;
      this.completion = null;
      this.bingo.setDirty();
   }

   public boolean locked(int slot) {
      return (this.locked & 1 << slot % 25) > 0;
   }

   public void lock(int slot) {
      this.locked |= 1 << slot % 25;
      this.completion = null;
      this.bingo.setDirty();
   }

   public TeamCompletion completion() {
      if (this.completion == null) {
         this.completion = new TeamCompletion(this.bingo, this);
      }

      return this.completion;
   }

   public boolean isEmpty() {
      return this.players.isEmpty();
   }

   public List<UUID> getPlayers() {
      return Collections.unmodifiableList(this.players);
   }

   public boolean hasPlayer(UUID uid) {
      return this.players.contains(uid);
   }

   public boolean hasPlayer(Player player) {
      return this.players.contains(player.getUUID());
   }

   private void addPlayer(UUID uid) {
      for (Team team : this.bingo.getTeams()) {
         team.removePlayer(uid);
      }

      this.players.add(uid);
      this.bingo.setDirty();
   }

   public void addPlayer(Player player) {
      this.addPlayer(player.getUUID());
      if (player instanceof ServerPlayer) {
         this.bingo.updateMentions((ServerPlayer)player);
      }

   }

   private void removePlayer(UUID uid) {
      this.players.remove(uid);
      this.bingo.setDirty();
   }

   public void removePlayer(Player player) {
      this.removePlayer(player.getUUID());
      if (player instanceof ServerPlayer) {
         this.bingo.updateMentions((ServerPlayer)player);
      }

   }

   public IItemHandlerModifiable getBackPack() {
      return this.backpack;
   }

   public void openBackPack(Player player) {
      if (!player.level().isClientSide()) {
         player.openMenu(new BackpackContainerProvider(this.getName(), this.backpack, this.bingo::setDirty));
      }
   }

   public int teleportsLeft() {
      return this.teleportsLeft;
   }

   public void teleportsLeft(int teleportsLeft) {
      this.teleportsLeft = teleportsLeft;
      this.bingo.setDirty();
   }

   public boolean redeemedEmergency() {
      return this.redeemedEmergency;
   }

   public void redeemedEmergency(boolean redeemedEmergency) {
      this.redeemedEmergency = redeemedEmergency;
      this.setDirty();
   }

   public boolean consumeTeleport() {
      if (this.teleportsLeft < 0) {
         return true;
      } else if (this.teleportsLeft > 0) {
         this.teleportsLeft--;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putInt("completed", this.completed);
      nbt.putInt("locked", this.locked);
      nbt.putInt("teleportsLeft", this.teleportsLeft);
      nbt.putBoolean("redeemedEmergency", this.redeemedEmergency);
      ListTag playerList = new ListTag();

      for (UUID uuid : this.players) {
         CompoundTag uuidTag = new CompoundTag();
         uuidTag.putString("player", uuid.toString());
         playerList.add(uuidTag);
      }

      nbt.put("players", playerList);
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.completed = nbt.getIntOr("completed", 0);
      this.locked = nbt.getIntOr("locked", 0);
      this.teleportsLeft = nbt.getIntOr("teleportsLeft", 0);
      this.redeemedEmergency = nbt.getBooleanOr("redeemedEmergency", false);
      if (nbt.contains("players")) {
         ListTag playerList = nbt.getListOrEmpty("players");
         this.players.clear();

         for (int i = 0; i < playerList.size(); i++) {
            try {
               this.players.add(UUID.fromString(playerList.getCompoundOrEmpty(i).getStringOr("player", "")));
            } catch (IllegalArgumentException var5) {
            }
         }
      }

      this.completion = null;
   }

   public void reset(boolean suppressBingoSync) {
      this.completed = 0;
      this.locked = 0;
      this.players.forEach(this.bingo::updateMentions);
      this.players.clear();
      this.teleportsLeft = 0;
      this.redeemedEmergency = false;
      this.clearBackPack(true);
      this.completion = null;
      this.bingo.markDirty(suppressBingoSync);
   }

   public void reset() {
      this.reset(false);
   }

   public void resetCompleted(boolean suppressBingoSync) {
      this.completed = 0;
      this.completion = null;
      this.bingo.markDirty(suppressBingoSync);
   }

   public void resetLocked(boolean suppressBingoSync) {
      this.locked = 0;
      this.completion = null;
      this.bingo.markDirty(suppressBingoSync);
   }

   public boolean lockRandomTask() {
      return this.lockRandomTasks(1);
   }

   public boolean lockRandomTasks(int amount) {
      ArrayList<Integer> lockableTasks = IntStream.range(0, 25)
         .boxed()
         .filter(ix -> !this.completed(ix) && !this.locked(ix))
         .collect(Collectors.toCollection(ArrayList::new));
      if (lockableTasks.size() < amount) {
         return false;
      }

      Random random = new Random();

      for (int i = 0; i < amount; i++) {
         int lockIdx = random.nextInt(lockableTasks.size());
         int taskId = lockableTasks.remove(lockIdx);
         this.lock(taskId);
      }

      return true;
   }

   public void clearBackPack(boolean suppressBingoSync) {
      for (int slot = 0; slot < this.backpack.getSlots(); slot++) {
         this.backpack.setStackInSlot(slot, ItemStack.EMPTY);
      }

      this.bingo.markDirty(suppressBingoSync);
   }

   public void clearBackPack() {
      this.clearBackPack(false);
   }

   public void clearPlayers() {
      this.players.clear();
      this.bingo.setDirty();
   }

   public void resetCompleted() {
      this.resetCompleted(false);
   }

   public void resetLocked() {
      this.resetLocked(false);
   }

   public void markDirty(boolean suppressBingoSync) {
      this.bingo.markDirty(suppressBingoSync);
   }

   public void setDirty() {
      this.bingo.setDirty();
   }
}
