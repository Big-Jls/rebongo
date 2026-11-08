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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.moddingx.libx.codec.MoreStreamCodecs;
import org.moddingx.libx.network.PacketHandler;

public record BingoUpdateMessage(CompoundTag data, BingoMessageType bongoMessageType) implements CustomPacketPayload {
   public static final Type<BingoUpdateMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("bongo", "update"));
   public static final StreamCodec<RegistryFriendlyByteBuf, BingoUpdateMessage> CODEC = StreamCodec.composite(
      MoreStreamCodecs.COMPOUND_TAG, BingoUpdateMessage::data, BingoMessageType.STREAM_CODEC, BingoUpdateMessage::bongoMessageType, BingoUpdateMessage::new
   );

   public BingoUpdateMessage(Bingo bingo) {
      this(bingo, BingoMessageType.GENERIC);
   }

   public BingoUpdateMessage(Bingo bingo, BingoMessageType bongoMessageType) {
      this(bingo.save(new CompoundTag()), bongoMessageType);
   }

   public Type<BingoUpdateMessage> type() {
      return TYPE;
   }

   public static class Handler extends PacketHandler<BingoUpdateMessage> {
      public Handler() {
         super(PacketFlow.CLIENTBOUND, BingoUpdateMessage.CODEC, BingoUpdateMessage.TYPE);
      }

      public void handle(BingoUpdateMessage msg, IPayloadContext ctx) {
         Bingo bingo = new Bingo();
         bingo.load(msg.data());
         Bingo.updateClient(bingo, msg.bongoMessageType());
      }
   }
}
