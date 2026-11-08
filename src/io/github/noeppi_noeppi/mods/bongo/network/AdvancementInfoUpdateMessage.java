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

import io.github.noeppi_noeppi.mods.bongo.util.ClientAdvancementInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.moddingx.libx.network.PacketHandler;

public record AdvancementInfoUpdateMessage(Identifier id, ItemStack display, Component translation) implements CustomPacketPayload {
   public static final Type<AdvancementInfoUpdateMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("bongo", "advancement_info"));
   public static final StreamCodec<RegistryFriendlyByteBuf, AdvancementInfoUpdateMessage> CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC,
      AdvancementInfoUpdateMessage::id,
      ItemStack.STREAM_CODEC,
      AdvancementInfoUpdateMessage::display,
      ComponentSerialization.STREAM_CODEC,
      AdvancementInfoUpdateMessage::translation,
      AdvancementInfoUpdateMessage::new
   );

   public Type<AdvancementInfoUpdateMessage> type() {
      return TYPE;
   }

   public static class Handler extends PacketHandler<AdvancementInfoUpdateMessage> {
      public Handler() {
         super(PacketFlow.CLIENTBOUND, AdvancementInfoUpdateMessage.CODEC, AdvancementInfoUpdateMessage.TYPE);
      }

      public void handle(AdvancementInfoUpdateMessage msg, IPayloadContext ctx) {
         ClientAdvancementInfo.updateAdvancementInfo(msg.id, msg.display, msg.translation);
      }
   }
}
