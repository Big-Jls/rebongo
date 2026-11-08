/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.network;

import io.github.noeppi_noeppi.mods.bongo.Bingo;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class BingoNetwork extends NetworkX {
   public BingoNetwork(ModX mod) {
      super(mod);
      this.register(new BingoUpdateMessage.Handler());
      this.register(new AdvancementInfoUpdateMessage.Handler());
   }

   protected String getVersion() {
      return "4";
   }

   public void syncAdvancement(AdvancementHolder advancement) {
      advancement.value()
         .display()
         .ifPresent(
            display -> PacketDistributor.sendToAllPlayers(
               new AdvancementInfoUpdateMessage(advancement.id(), display.getIcon().create(), display.getTitle()), new CustomPacketPayload[0]
            )
         );
   }

   public void syncAdvancementTo(AdvancementHolder advancement, ServerPlayer player) {
      advancement.value()
         .display()
         .ifPresent(
            display -> PacketDistributor.sendToPlayer(
               player, new AdvancementInfoUpdateMessage(advancement.id(), display.getIcon().create(), display.getTitle()), new CustomPacketPayload[0]
            )
         );
   }

   public void updateBingo(Level level) {
      if (!level.isClientSide()) {
         PacketDistributor.sendToAllPlayers(new BingoUpdateMessage(Bingo.get(level)), new CustomPacketPayload[0]);
      }
   }

   public void updateBingo(Player player) {
      if (!player.level().isClientSide()) {
         PacketDistributor.sendToPlayer((ServerPlayer)player, new BingoUpdateMessage(Bingo.get(player.level())), new CustomPacketPayload[0]);
      }
   }

   public void updateBingo(Level level, BingoMessageType messageType) {
      if (!level.isClientSide()) {
         PacketDistributor.sendToAllPlayers(new BingoUpdateMessage(Bingo.get(level), messageType), new CustomPacketPayload[0]);
      }
   }

   public void updateBingo(Player player, BingoMessageType messageType) {
      if (!player.level().isClientSide()) {
         PacketDistributor.sendToPlayer((ServerPlayer)player, new BingoUpdateMessage(Bingo.get(player.level()), messageType), new CustomPacketPayload[0]);
      }
   }
}
