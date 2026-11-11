/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.mods.bongo.config.ClientConfig;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.data.WinCondition;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.event.BingoPickLevelEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoStartEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoStopEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoTaskEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoTeleportedEvent;
import io.github.noeppi_noeppi.mods.bongo.event.BingoWinEvent;
import io.github.noeppi_noeppi.mods.bongo.network.BingoMessageType;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import io.github.noeppi_noeppi.mods.bongo.task.TaskType;
import io.github.noeppi_noeppi.mods.bongo.util.Highlight;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.util.game.ServerMessages;

public class Bingo extends SavedData {
   public static final String ID = BingoMod.getInstance().modid;
   public static final Codec<Bingo> CODEC = CompoundTag.CODEC.xmap(tag -> {
      Bingo bingo = new Bingo();
      bingo.load(tag);
      return bingo;
   }, bingo -> bingo.save(new CompoundTag()));
   public static final SavedDataType<Bingo> DATA_TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(ID, ID), Bingo::new, CODEC, DataFixTypes.LEVEL);
   private static Bingo clientInstance;
   private static Minecraft mc = null;
   private ServerLevel level;
   private final Map<DyeColor, Team> teams;
   private final List<Task> items;
   private Predicate<ItemStack> tooltipPredicate;
   private final List<UUID> playersInTcMode = new ArrayList<>();
   private GameSettings settings = GameSettings.DEFAULT;
   private boolean active;
   private boolean running;
   private boolean teamWon;
   private long runningSince = 0L;
   private long ranUntil = 0L;
   private long runningUntil = 0L;
   private int taskAmountOutOfTime = -1;
   private DyeColor winningTeam;
   private boolean draw;

   public static Bingo get(Level level) {
      if (!level.isClientSide()) {
         ServerLevel serverLevel = (ServerLevel)level;
         Bingo bingo = (Bingo)serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(DATA_TYPE);
         bingo.level = serverLevel.getServer().overworld();
         return bingo;
      } else {
         return clientInstance == null ? new Bingo() : clientInstance;
      }
   }

   // 26.2 不再触发 TabListNameFormat 事件，改用原版计分板队伍为 Tab 列表上色
   public void syncScoreboardTeams() {
      if (this.level == null || this.level.getServer() == null) {
         return;
      }

      Scoreboard scoreboard = this.level.getScoreboard();
      for (ServerPlayer player : this.level.getServer().getPlayerList().getPlayers()) {
         Team team = this.getTeam(player);
         PlayerTeam current = scoreboard.getPlayersTeam(player.getScoreboardName());
         String wanted = team == null ? null : "bingo_" + team.color.getSerializedName();
         if (wanted == null) {
            if (current != null && current.getName().startsWith("bingo_")) {
               scoreboard.removePlayerFromTeam(player.getScoreboardName(), current);
            }
         } else if (current == null || !current.getName().equals(wanted)) {
            if (current != null && current.getName().startsWith("bingo_")) {
               scoreboard.removePlayerFromTeam(player.getScoreboardName(), current);
            }

            PlayerTeam target = scoreboard.getPlayerTeam(wanted);
            if (target == null) {
               target = scoreboard.addPlayerTeam(wanted);
               target.setDisplayName(team.getName());
            }

            target.setColor(Optional.of(Util.getTeamColor(team.color)));
            scoreboard.addPlayerToTeam(player.getScoreboardName(), target);
         }
      }

      for (PlayerTeam team : new ArrayList<>(scoreboard.getPlayerTeams())) {
         if (team.getName().startsWith("bingo_") && team.getPlayers().isEmpty()) {
            scoreboard.removePlayerTeam(team);
         }
      }
   }

   public static void updateClient(Bingo bingo, BingoMessageType bongoMessageType) {
      clientInstance = bingo;
      if (mc == null) {
         mc = Minecraft.getInstance();
      }

      if (bongoMessageType == BingoMessageType.START || bongoMessageType == BingoMessageType.STOP || bongoMessageType == BingoMessageType.FORCE) {
         if ((Boolean)ClientConfig.addItemTooltips.get()) {
            bingo.updateTooltipPredicate();
         }
      }
   }

   public Bingo() {
      this.level = null;
      Builder<DyeColor, Team> teamBuilder = ImmutableMap.builder();

      for (DyeColor dc : DyeColor.values()) {
         teamBuilder.put(dc, new Team(this, dc));
      }

      this.teams = teamBuilder.build();
      this.items = new ArrayList<>();

      for (int i = 0; i < 25; i++) {
         this.items.add(Task.EMPTY);
      }

      this.tooltipPredicate = null;
      this.active = false;
      this.running = false;
      this.teamWon = false;
      this.winningTeam = null;
      this.draw = false;
   }

   public Bingo(CompoundTag nbt) {
      this();
      this.load(nbt);
   }

   public Team getTeam(DyeColor color) {
      return this.teams.get(color);
   }

   @Nullable
   public Team getTeam(Player player) {
      return this.getTeam(player.getUUID());
   }

   @Nullable
   public Team getTeam(UUID uid) {
      for (Team team : this.teams.values()) {
         if (team.hasPlayer(uid)) {
            return team;
         }
      }

      return null;
   }

   public Set<Team> getTeams() {
      return Set.copyOf(this.teams.values());
   }

   public boolean active() {
      return this.active;
   }

   public void activate() {
      this.active = true;
      this.running = false;
      this.teamWon = false;
      this.draw = false;
      if (this.level != null) {
      }

      this.setDirty();
   }

   public boolean running() {
      return this.running;
   }

   public void start() {
      this.active = true;
      this.running = true;
      this.teamWon = false;
      this.draw = false;
      this.runningSince = System.currentTimeMillis();
      this.ranUntil = 0L;
      if (this.settings.game().time().limit().isPresent()) {
         this.runningUntil = System.currentTimeMillis() + 1000L * this.settings.game().time().limit().getAsInt();
      } else {
         this.runningUntil = 0L;
      }

      this.taskAmountOutOfTime = -1;
      Set<UUID> uids = new HashSet<>();

      for (Team team : this.teams.values()) {
         this.settings.equipment().equip(team, true);
         team.resetCompleted(true);
         team.resetLocked(true);
         team.teleportsLeft(this.getSettings().game().teleportsPerTeam());
         uids.addAll(team.getPlayers());
      }

      if (this.level != null) {
         BingoPickLevelEvent event = new BingoPickLevelEvent(this, this.level);
         NeoForge.EVENT_BUS.post(event);
         ServerLevel gameLevel = event.getLevel();
         NeoForge.EVENT_BUS.post(new BingoStartEvent.Level(this, gameLevel));
         this.level.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (uids.contains(player.getUUID())) {
               NeoForge.EVENT_BUS.post(new BingoStartEvent.Player(this, gameLevel, player));
            }
         });
         Random random = new Random();

         for (Team team : this.getTeams()) {
            List<ServerPlayer> players = this.level
               .getServer()
               .getPlayerList()
               .getPlayers()
               .stream()
               .filter(team::hasPlayer)
               .toList();
            if (!players.isEmpty()) {
               this.settings.level().teleporter().teleportTeam(this, gameLevel, team, players, BlockPos.ZERO, this.settings.level().teleportRadius(), random);
               NeoForge.EVENT_BUS.post(new BingoTeleportedEvent(this, gameLevel, team, this.settings.level().teleporter(), players));
            }
         }
      }

      this.markDirty(true);
      if (this.level != null) {
         BingoMod.getNetwork().updateBingo(this.level, BingoMessageType.START);
      }
   }

   public void stop() {
      Set<UUID> uids = new HashSet<>();

      for (Team team : this.teams.values()) {
         uids.addAll(team.getPlayers());
      }

      this.active = false;
      this.running = false;
      this.teamWon = false;
      this.winningTeam = null;
      this.draw = false;
      this.settings = GameSettings.DEFAULT;
      this.playersInTcMode.clear();
      this.markDirty(true);
      if (this.level != null) {
         NeoForge.EVENT_BUS.post(new BingoStopEvent.Level(this, this.level));

         for (UUID uid : uids) {
            ServerPlayer player = this.level.getServer().getPlayerList().getPlayer(uid);
            if (player != null) {
               NeoForge.EVENT_BUS.post(new BingoStopEvent.Player(this, player.level(), player));
               this.updateMentions(player);
            }
         }

         BingoMod.getNetwork().updateBingo(this.level, BingoMessageType.STOP);
      }
   }

   public boolean won() {
      return this.teamWon;
   }

   @Nullable
   public Team winningTeam() {
      return this.winningTeam == null ? null : this.teams.get(this.winningTeam);
   }

   public boolean draw() {
      return this.draw;
   }

   @Nonnull
   public CompoundTag save(@Nonnull CompoundTag compound) {
      compound.putBoolean("active", this.active);
      compound.putBoolean("running", this.running);
      compound.putBoolean("teamWon", this.teamWon);
      compound.putBoolean("draw", this.draw);
      compound.putLong("runningSince", this.runningSince);
      compound.putLong("ranUntil", this.ranUntil);
      compound.putLong("runningUntil", this.runningUntil);
      compound.putInt("taskAmountOutOfTime", this.taskAmountOutOfTime);
      if (this.winningTeam != null) {
         compound.putInt("winningTeam", this.winningTeam.ordinal());
      }

      safe(() -> nbtWrite(GameSettings.CODEC, this.settings)).ifPresent(elem -> compound.put("settings", elem));

      for (DyeColor dc : DyeColor.values()) {
         compound.put("team_" + dc.getSerializedName(), this.getTeam(dc).serializeNBT());
      }

      ListTag itemList = new ListTag();

      for (Task item : this.items) {
         itemList.add(safe(() -> nbtWrite(Task.CODEC, item)).orElse(StringTag.valueOf("invalid")));
      }

      compound.put("items", itemList);
      ListTag tcPlayers = new ListTag();

      for (UUID uid : this.playersInTcMode) {
         CompoundTag playerNbt = new CompoundTag();
         playerNbt.putString("player", uid.toString());
         tcPlayers.add(playerNbt);
      }

      compound.put("teamchat", tcPlayers);
      return compound;
   }

   public void load(@Nonnull CompoundTag nbt) {
      this.active = nbt.getBooleanOr("active", false);
      this.running = nbt.getBooleanOr("running", false);
      this.teamWon = nbt.getBooleanOr("teamWon", false);
      this.draw = nbt.getBooleanOr("draw", false);
      this.runningSince = nbt.getLongOr("runningSince", 0L);
      this.ranUntil = nbt.getLongOr("ranUntil", 0L);
      this.runningUntil = nbt.getLongOr("runningUntil", 0L);
      this.taskAmountOutOfTime = nbt.getIntOr("taskAmountOutOfTime", -1);
      if (nbt.contains("winningTeam")) {
         this.winningTeam = DyeColor.values()[nbt.getIntOr("winningTeam", 0)];
      } else {
         this.winningTeam = null;
      }

      this.settings = GameSettings.DEFAULT;
      if (nbt.contains("settings")) {
         this.settings = safe(() -> nbtRead(GameSettings.CODEC, nbt.get("settings"))).orElse(GameSettings.DEFAULT);
      }

      for (DyeColor dc : DyeColor.values()) {
         if (nbt.contains("team_" + dc.getSerializedName())) {
            this.getTeam(dc).deserializeNBT(nbt.getCompoundOrEmpty("team_" + dc.getSerializedName()));
         }
      }

      if (nbt.contains("items")) {
         ListTag itemList = nbt.getListOrEmpty("items");

         for (int i = 0; i < this.items.size(); i++) {
            if (i >= itemList.size()) {
               this.items.set(i, Task.EMPTY);
            } else if (itemList.get(i) instanceof StringTag && "invalid".equals(itemList.getStringOr(i, ""))) {
               this.items.set(i, Task.EMPTY);
            } else {
               int idx = i;
               this.items.set(i, safe(() -> nbtRead(Task.CODEC, itemList.get(idx))).orElse(Task.EMPTY));
            }
         }
      } else {
         this.clearItems();
      }

      this.playersInTcMode.clear();
      if (nbt.contains("teamchat")) {
         ListTag tcPlayers = nbt.getListOrEmpty("teamchat");

         for (int i = 0; i < tcPlayers.size(); i++) {
            CompoundTag playerNbt = tcPlayers.getCompoundOrEmpty(i);

            try {
               this.playersInTcMode.add(UUID.fromString(playerNbt.getStringOr("player", "")));
            } catch (IllegalArgumentException var6) {
            }
         }
      }
   }

   private static <T> Optional<T> safe(Callable<T> value) {
      try {
         return Optional.of(value.call());
      } catch (Exception e) {
         return Optional.empty();
      }
   }

   public void reset() {
      for (Team team : this.teams.values()) {
         team.reset(true);
      }

      this.active = false;
      this.running = false;
      this.runningSince = 0L;
      this.ranUntil = 0L;
      this.teamWon = false;
      this.winningTeam = null;
      this.draw = false;
      this.clearItems(true);
      this.playersInTcMode.clear();
      this.settings = GameSettings.DEFAULT;
      this.setDirty();
   }

   public GameSettings getSettings() {
      return this.settings == null ? GameSettings.DEFAULT : this.settings;
   }

   public void setSettings(GameSettings settings, boolean suppressBingoSync) {
      this.settings = settings == null ? GameSettings.DEFAULT : settings;
      this.markDirty(suppressBingoSync);
   }

   public void clearItems() {
      this.clearItems(false);
   }

   public void clearItems(boolean suppressBingoSync) {
      this.items.replaceAll(t -> Task.EMPTY);
      this.markDirty(suppressBingoSync);
   }

   public Task task(int slot) {
      Task task = this.items.get(slot);
      return task == null ? Task.EMPTY : task;
   }

   public List<Task> tasks() {
      return Collections.unmodifiableList(this.items);
   }

   public boolean canCompleteTasks(Player player) {
      return this.running() && this.getTeam(player) != null;
   }

   public <C> void checkCompleted(TaskType<C> type, Player player, C compare) {
      if (this.running && !player.level().isClientSide()) {
         Team team = this.getTeam(player);
         if (team != null && player instanceof ServerPlayer serverPlayer) {
            for (int i = 0; i < this.items.size(); i++) {
               if (!team.completed(i) && !team.locked(i) && this.task(i).getType() == type) {
                  Task.CompletionState state = this.items.get(i).shouldComplete(type, serverPlayer, compare);
                  if (state.shouldComplete) {
                     team.complete(i);
                     if (this.getSettings().game().consumeItems()) {
                        this.task(i).consume(type, serverPlayer, compare);
                     }

                     if (this.claimLockEnabled()) {
                        for (Team t : this.teams.values()) {
                           if (!t.completed(i)) {
                              t.lock(i);
                           }
                        }
                     }

                     NeoForge.EVENT_BUS.post(new BingoTaskEvent(this, serverPlayer.level(), serverPlayer, this.task(i)));
                  } else if (state.shouldLock) {
                     team.lock(i);
                     if (this.claimLockEnabled()) {
                        for (Team t : this.teams.values()) {
                           if (!t.locked(i)) {
                              t.complete(i);
                           }
                        }
                     }

                     NeoForge.EVENT_BUS.post(new BingoTaskEvent(this, serverPlayer.level(), serverPlayer, this.task(i)));
                  }
               }
            }
         }

         this.checkWin();
      }
   }

   private boolean claimLockEnabled() {
      return this.getSettings().game().lockout() || this.getSettings().game().winCondition() == WinCondition.LOCK;
   }

   public void checkWin() {
      if (this.running) {
         if (this.settings.game().winCondition() == WinCondition.LOCK) {
            this.checkWinLock();
         } else {
            if (this.settings.game().time().limited() && System.currentTimeMillis() > this.runningUntil) {
               if (this.taskAmountOutOfTime < 0) {
                  int max = 0;

                  for (Team team : this.teams.values()) {
                     int amount = team.completion().count();
                     if (amount > max) {
                        max = amount;
                     }
                  }

                  Team winning = null;

                  for (Team team : this.teams.values()) {
                     if (team.completion().count() >= max) {
                        if (winning != null) {
                           this.taskAmountOutOfTime = Math.min(25, max + 1);
                           this.setDirty();
                           return;
                        }

                        winning = team;
                     }
                  }

                  if (winning == null) {
                     this.taskAmountOutOfTime = Math.min(25, max + 1);
                     this.setDirty();
                  } else {
                     this.setWin(winning);
                  }

                  return;
               }

               for (Team team : this.teams.values()) {
                  if (team.completion().count() >= this.taskAmountOutOfTime) {
                     this.setWin(team);
                     return;
                  }
               }
            }

            for (Team team : this.teams.values()) {
               if (this.getSettings().game().winCondition().won(team.completion())) {
                  this.setWin(team);
                  return;
               }
            }
         }
      }
   }

   private void checkWinLock() {
      if (this.settings.game().time().limited() && System.currentTimeMillis() > this.runningUntil) {
         this.resolveLockTimeout();
      } else {
         for (Team team : this.teams.values()) {
            if (team.completion().count() >= 13) {
               this.setWin(team);
               return;
            }
         }

         for (Team team : this.teams.values()) {
            if (WinCondition.DEFAULT.won(team.completion())) {
               this.setWin(team);
               return;
            }
         }
      }
   }

   private void resolveLockTimeout() {
      int max = -1;
      Team winning = null;
      boolean tie = false;

      for (Team team : this.teams.values()) {
         int amount = team.completion().count();
         if (amount > max) {
            max = amount;
            winning = team;
            tie = false;
         } else if (amount == max) {
            tie = true;
         }
      }

      if (winning != null && !tie && max > 0) {
         this.setWin(winning);
      } else {
         this.setDraw();
      }
   }

   private void setWin(Team team) {
      this.running = false;
      this.teamWon = true;
      this.winningTeam = team.color;
      this.draw = false;
      if (this.level != null) {
         NeoForge.EVENT_BUS.post(new BingoWinEvent(this, this.level, team));
      }

      this.ranUntil = System.currentTimeMillis();
      this.playersInTcMode.clear();
      this.setDirty();
   }

   private void setDraw() {
      this.running = false;
      this.teamWon = true;
      this.winningTeam = null;
      this.draw = true;
      this.ranUntil = System.currentTimeMillis();
      this.playersInTcMode.clear();
      if (this.level != null) {
         ServerMessages.broadcast(this.level, Component.translatable("bongo.draw"));
      }

      this.setDirty();
   }

   public long runningSince() {
      return this.runningSince;
   }

   public long ranUntil() {
      return this.ranUntil;
   }

   public void setDirty() {
      super.setDirty();
      if (this.level != null) {
         BingoMod.getNetwork().updateBingo(this.level);
      }
   }

   public void markDirty(boolean suppressBingoSync) {
      super.setDirty();
      if (this.level != null && !suppressBingoSync) {
         BingoMod.getNetwork().updateBingo(this.level);
      }
   }

   private static <T> Tag nbtWrite(Codec<T> codec, T value) {
      return (Tag)codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow();
   }

   private static <T> T nbtRead(Codec<T> codec, Tag tag) {
      return (T)codec.parse(NbtOps.INSTANCE, tag).getOrThrow();
   }

   public boolean toggleTeamChat(Player player) {
      return this.toggleTeamChat(player.getUUID());
   }

   public boolean toggleTeamChat(UUID uid) {
      if (this.playersInTcMode.contains(uid)) {
         this.playersInTcMode.remove(uid);
         this.setDirty();
         return false;
      } else {
         this.playersInTcMode.add(uid);
         this.setDirty();
         return true;
      }
   }

   public boolean teamChat(Player player) {
      return this.teamChat(player.getUUID());
   }

   public boolean teamChat(UUID uid) {
      return this.playersInTcMode.contains(uid);
   }

   public void setTasks(List<Task> tasks) {
      for (int i = 0; i < 25; i++) {
         this.items.set(i, tasks.get(i));
         if (this.level != null) {
            tasks.get(i).sync(this.level.getServer(), null);
         }
      }

      this.updateTooltipPredicate();
      this.markDirty(true);
      if (this.level != null) {
         BingoMod.getNetwork().updateBingo(this.level, BingoMessageType.CREATE);
      }
   }

   private void updateTooltipPredicate() {
      if (this.level == null) {
         List<Predicate<ItemStack>> predicates = this.items
            .stream()
            .flatMap(Task::highlight)
            .flatMap(Highlight::asItem)
            .map(Highlight.Item::predicate)
            .toList();
         this.tooltipPredicate = stack -> {
            for (Predicate<ItemStack> predicate : predicates) {
               if (predicate.test(stack)) {
                  return true;
               }
            }

            return false;
         };
      }
   }

   public <T> Stream<T> getElementsOf(TaskType<T> type) {
      return this.items.stream().flatMap(task -> task.getElement(type).stream());
   }

   public boolean isTooltipStack(ItemStack stack) {
      if (this.tooltipPredicate == null) {
         this.updateTooltipPredicate();
      }

      return !stack.isEmpty() && this.tooltipPredicate.test(stack);
   }

   public void updateMentions(UUID player) {
      if (this.level != null) {
         ServerPlayer entity = this.level.getServer().getPlayerList().getPlayer(player);
         if (entity != null) {
            this.updateMentions(entity);
         }
      }
   }

   public void updateMentions(ServerPlayer player) {
   }

   public long runningUntil() {
      return this.runningUntil;
   }

   public int tasksForWin() {
      return this.taskAmountOutOfTime;
   }
}
