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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ItemRenderUtil {
   public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int width, int height, int texWidth, int texHeight) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texWidth, texHeight);
   }

   public static void renderItem(GuiGraphicsExtractor graphics, ItemStack stack, boolean includeAmount) {
      graphics.fakeItem(stack, 0, 0);
      if (includeAmount) {
         graphics.itemDecorations(Minecraft.getInstance().font, stack, 0, 0);
      }
   }
}
