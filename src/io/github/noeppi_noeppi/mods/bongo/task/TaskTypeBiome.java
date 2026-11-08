/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.task;

import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TaskTypeBiome implements TaskType<Identifier> {
   public static final TaskTypeBiome INSTANCE = new TaskTypeBiome();
   private static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/icon/biome/forest.png");

   private TaskTypeBiome() {
   }

   @Override
   public String id() {
      return "bongo.biome";
   }

   @Override
   public Class<Identifier> taskClass() {
      return Identifier.class;
   }

   @Override
   public MapCodec<Identifier> codec() {
      return Identifier.CODEC.fieldOf("value");
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.biome.name");
   }

   public Component contentName(Identifier element, @Nullable MinecraftServer server) {
      return Component.translatable(Util.makeDescriptionId("biome", element));
   }

   @Override
   public Comparator<Identifier> order() {
      return io.github.noeppi_noeppi.mods.bongo.util.Util.COMPARE_RESOURCE;
   }

   public void validate(Identifier element, MinecraftServer server) {
      if (server.registryAccess().lookupOrThrow(Registries.BIOME).get(element) == null) {
         throw new IllegalStateException("Biome not registered: " + element);
      }
   }

   @Override
   public Stream<Identifier> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? server.registryAccess().lookupOrThrow(Registries.BIOME).keySet().stream()
         : io.github.noeppi_noeppi.mods.bongo.util.Util.biome(player.level(), player.blockPosition()).stream();
   }

   public boolean shouldComplete(ServerPlayer player, Identifier element, Identifier compare) {
      return Objects.equals(element, compare);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 18, 0, 18, 18, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, Identifier element, boolean bigBongo) {
      Identifier biomeTexture = element != null
         ? Identifier.fromNamespaceAndPath(element.getNamespace(), "textures/icon/biome/" + element.getPath() + ".png")
         : FALLBACK_TEXTURE;
      if (mc.getResourceManager().getResource(biomeTexture).isEmpty()) {
         biomeTexture = FALLBACK_TEXTURE;
      }

      ItemRenderUtil.blit(graphics, biomeTexture, 0, 0, 0, 0, 16, 16, 16, 16);
   }
}
