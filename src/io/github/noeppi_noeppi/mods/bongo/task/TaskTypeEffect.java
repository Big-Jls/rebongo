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

import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.PotionTextureRenderCache;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TaskTypeEffect extends RegistryTaskType<MobEffect> {
   public static final TaskTypeEffect INSTANCE = new TaskTypeEffect();

   private TaskTypeEffect() {
      super(MobEffect.class, BuiltInRegistries.MOB_EFFECT);
   }

   @Override
   public String id() {
      return "bongo.effect";
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.potion.name");
   }

   public Component contentName(MobEffect element, @Nullable MinecraftServer server) {
      return element.getDisplayName();
   }

   @Override
   public Stream<MobEffect> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? BuiltInRegistries.MOB_EFFECT.stream().filter(effect -> !effect.isInstantaneous())
         : player.getActiveEffects().stream().map(instance -> (MobEffect)instance.getEffect().value());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      graphics.pose().translate(-1.0F, -1.0F);
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 26, 18, 20, 20, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, MobEffect element, boolean bigBongo) {
      graphics.pose().translate(-1.0F, -1.0F);
      ItemRenderUtil.blit(graphics, PotionTextureRenderCache.getRenderTexture(element), 0, 0, 0, 0, 18, 18, 18, 18);
   }
}
