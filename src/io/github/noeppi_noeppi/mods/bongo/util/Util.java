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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Util {
   public static final Component REQUIRED_ITEM = Component.translatable("bongo.tooltip.required").withStyle(ChatFormatting.GOLD);
   public static final Comparator<Identifier> COMPARE_RESOURCE = Comparator.nullsFirst(
      Comparator.comparing(Identifier::getNamespace).thenComparing(Identifier::getPath)
   );
   public static final List<DyeColor> PREFERRED_COLOR_ORDER = ImmutableList.of(
      DyeColor.YELLOW,
      DyeColor.LIME,
      DyeColor.LIGHT_BLUE,
      DyeColor.PINK,
      DyeColor.CYAN,
      DyeColor.ORANGE,
      DyeColor.RED,
      DyeColor.GREEN,
      DyeColor.BLUE,
      DyeColor.PURPLE,
      DyeColor.GRAY,
      DyeColor.MAGENTA,
      new DyeColor[]{DyeColor.BLACK, DyeColor.WHITE, DyeColor.BROWN, DyeColor.LIGHT_GRAY}
   );
   private static final Map<DyeColor, TextColor> COLOR_CACHE = new HashMap<>();

   public static String resourceStr(Identifier rl) {
      return "minecraft".equals(rl.getNamespace()) ? rl.getPath() : rl.toString();
   }

   public static Style getTextFormatting(@Nullable DyeColor color) {
      if (color == null) {
         return Style.EMPTY.applyFormat(ChatFormatting.RESET);
      }

      if (!COLOR_CACHE.containsKey(color)) {
         int colorValue = color.getTextColor();
         float[] hsb = Color.RGBtoHSB(colorValue >>> 16 & 0xFF, colorValue >>> 8 & 0xFF, colorValue & 0xFF, null);
         COLOR_CACHE.put(color, TextColor.fromRgb(16777215 & Color.HSBtoRGB(hsb[0], hsb[1] < 0.2 ? hsb[1] : 1.0F, hsb[2])));
      }

      return Style.EMPTY.withColor(COLOR_CACHE.get(color));
   }

   public static void broadcastTeam(Level level, Team team, Component message) {
      MinecraftServer server = level.getServer();
      if (server != null) {
         server.getPlayerList().getPlayers().forEach(player -> {
            if (team.hasPlayer(player)) {
               player.sendSystemMessage(message);
            }
         });
      }
   }

   public static boolean matchesNBT(@Nullable CompoundTag required, @Nullable CompoundTag actual) {
      if (required == null || required.isEmpty()) {
         return true;
      } else if (actual != null && !actual.isEmpty()) {
         CompoundTag copy = actual.copy();
         copy.merge(required);
         return copy.equals(actual);
      } else {
         return false;
      }
   }

   public static String formatTime(int hours, int minutes, int seconds) {
      return hours == 0 ? minutes + ":" + String.format("%02d", seconds) : hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
   }

   public static String formatTime(int hours, int minutes, int seconds, int decimal) {
      return hours == 0
         ? minutes + ":" + String.format("%02d", seconds) + "." + decimal
         : hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + "." + decimal;
   }

   public static boolean validSpawn(Level level, BlockPos pos) {
      return canRespawnIn(level.getBlockState(pos))
         && canRespawnIn(level.getBlockState(pos.above()))
         && level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
   }

   private static boolean canRespawnIn(BlockState state) {
      return state.getBlock().isPossibleToRespawnInThis(state);
   }

   public static Optional<Identifier> biome(ServerLevel level, BlockPos pos) {
      return join(
         level.getBiome(pos)
            .unwrap()
            .mapLeft(ResourceKey::identifier)
            .mapLeft(Optional::of)
            .mapRight(biome -> level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome))
            .mapRight(Optional::ofNullable)
      );
   }

   public static <T> T join(Either<? extends T, ? extends T> either) {
      return (T)(either.left().isPresent() ? either.left().get() : either.right().get());
   }

   public static void removeItems(Player player, int amount, Predicate<ItemStack> test) {
      int removeLeft = amount;

      for (int slot = 0; slot < player.getInventory().getContainerSize() && removeLeft > 0; slot++) {
         ItemStack playerSlot = player.getInventory().getItem(slot);
         if (test.test(playerSlot)) {
            int rem = Math.min(removeLeft, playerSlot.getCount());
            playerSlot.shrink(rem);
            player.getInventory().setItem(slot, playerSlot.isEmpty() ? ItemStack.EMPTY : playerSlot);
            removeLeft -= rem;
         }
      }
   }

   public static void handleTaskLocking(Bingo bingo, Player player) {
      if (bingo.running() && bingo.getSettings().game().lockTaskOnDeath()) {
         Team team = bingo.getTeam(player);
         if (team != null && team.lockRandomTask()) {
            MutableComponent tc = Component.translatable("bongo.task_locked.death", new Object[]{player.getDisplayName()});
            if (player instanceof ServerPlayer serverPlayer) {
               serverPlayer.level()
                  .getServer()
                  .getPlayerList()
                  .getPlayers()
                  .forEach(
                     thePlayer -> {
                        if (team.hasPlayer(thePlayer)) {
                           thePlayer.sendSystemMessage(tc);
                           thePlayer.connection
                              .send(
                                 new ClientboundSoundPacket(
                                    sound(SoundEvents.ANVIL_LAND), SoundSource.MASTER, thePlayer.getX(), thePlayer.getY(), thePlayer.getZ(), 1.0F, 1.0F, 0L
                                 )
                              );
                        }
                     }
                  );
            }
         }
      }
   }

   public static Holder<SoundEvent> sound(SoundEvent event) {
      return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event);
   }
}
