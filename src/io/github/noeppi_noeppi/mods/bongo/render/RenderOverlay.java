/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.render;

import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.Keybinds;
import io.github.noeppi_noeppi.mods.bongo.config.ClientConfig;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.data.WinCondition;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class RenderOverlay implements GuiLayer {
   public static RenderOverlay INSTANCE = new RenderOverlay();
   public static final Identifier BINGO_TEXTURE = Identifier.fromNamespaceAndPath(BingoMod.getInstance().modid, "textures/overlay/bingo.png");
   public static final Identifier BINGO_SLOTS_TEXTURE = Identifier.fromNamespaceAndPath(BingoMod.getInstance().modid, "textures/overlay/bingo_slots.png");
   public static final Identifier BEACON_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/beacon.png");
   public static final Identifier BARRIER_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/item/barrier.png");
   public static final Identifier COMPLETED_TEXTURE = Identifier.fromNamespaceAndPath(BingoMod.getInstance().modid, "textures/overlay/completed_rects.png");
   public static final Identifier ICONS_TEXTURE = Identifier.fromNamespaceAndPath(BingoMod.getInstance().modid, "textures/overlay/icons.png");
   private static final int[][] RECTS_1 = new int[][]{{0, 0, 24, 24}};
   private static final int[][] RECTS_2 = new int[][]{{0, 0, 12, 24}, {12, 0, 24, 24}};
   private static final int[][] RECTS_4 = new int[][]{{0, 0, 12, 12}, {12, 0, 24, 12}, {0, 12, 12, 24}, {12, 12, 24, 24}};
   private static final int[][] RECTS_6 = new int[][]{{0, 0, 12, 8}, {12, 0, 24, 8}, {0, 8, 12, 16}, {12, 8, 24, 16}, {0, 16, 12, 24}, {12, 16, 24, 24}};
   private static final int[][] RECTS_8 = new int[][]{
      {0, 0, 12, 6}, {12, 0, 24, 6}, {0, 6, 12, 12}, {12, 6, 24, 12}, {0, 12, 12, 18}, {12, 12, 24, 18}, {0, 18, 12, 24}, {12, 18, 24, 24}
   };
   private static final int[][] RECTS_16 = new int[][]{
      {0, 0, 12, 3},
      {12, 0, 24, 3},
      {0, 3, 12, 6},
      {12, 3, 24, 6},
      {0, 6, 12, 9},
      {12, 6, 24, 9},
      {0, 9, 12, 12},
      {12, 9, 24, 12},
      {0, 12, 12, 15},
      {12, 12, 24, 15},
      {0, 15, 12, 18},
      {12, 15, 24, 18},
      {0, 18, 12, 21},
      {12, 18, 24, 21},
      {0, 21, 12, 24},
      {12, 21, 24, 24}
   };

   private RenderOverlay() {
   }

   public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         Bingo bingo = Bingo.get(mc.level);
         Team team = bingo.getTeam(mc.player);
         if (bingo.active() && (!bingo.running() || team != null)) {
            int width = graphics.guiWidth();
            int height = graphics.guiHeight();
            graphics.pose().pushMatrix();
            double padding = 5.0;
            double px = height / 3.5;
            double x = padding;
            double y = padding;
            boolean itemNames = false;
            if (Keybinds.BIG_OVERLAY.isDown()) {
               px = Math.min(width - 2.0 * padding, height - 6.0 * padding);
               x = (width - px) / 2.0;
               y = (height - px) / 2.0 - 2.0 * padding;
               itemNames = true;
            } else {
               px *= (float)((Double)ClientConfig.bongoMapScaleFactor.get()).doubleValue();
               if ((Boolean)ClientConfig.bongoMapOnTheRight.get()) {
                  x = width - padding - px;
               }
            }

            graphics.pose().translate((float)x, (float)y);
            graphics.pose().scale((float)(px / 138.0), (float)(px / 138.0));
            ItemRenderUtil.blit(graphics, BINGO_TEXTURE, 0, 0, 0, 0, 138, 138, 256, 256);
            graphics.pose().translate(2.0F, 2.0F);

            for (int ySlot = 0; ySlot < 5; ySlot++) {
               for (int xSlot = 0; xSlot < 5; xSlot++) {
                  int slot = xSlot + 5 * ySlot;
                  List<Integer> colorCodes = new ArrayList<>();

                  for (DyeColor dc : DyeColor.values()) {
                     if (!bingo.getTeam(dc).isEmpty() && bingo.getTeam(dc).completion().has(slot)) {
                        colorCodes.add(dc.getTextColor());
                     }
                  }

                  graphics.pose().pushMatrix();
                  graphics.pose().translate(xSlot * 27, ySlot * 27);
                  this.renderCompleted(graphics, colorCodes);
                  graphics.pose().popMatrix();
               }
            }

            graphics.pose().translate(-2.0F, -2.0F);

            for (int ySlot = 0; ySlot < 5; ySlot++) {
               for (int xSlot = 0; xSlot < 5; xSlot++) {
                  int slot = xSlot + 5 * ySlot;
                  graphics.pose().pushMatrix();
                  graphics.pose().translate(6 + 27 * xSlot, 6 + 27 * ySlot);
                  bingo.task(slot).renderSlot(mc, graphics);
                  graphics.pose().popMatrix();
               }
            }

            graphics.pose().translate(7.0F, 7.0F);

            for (int ySlot = 0; ySlot < 5; ySlot++) {
               for (int xSlot = 0; xSlot < 5; xSlot++) {
                  int slot = xSlot + 5 * ySlot;
                  Task task = bingo.task(slot);
                  graphics.pose().pushMatrix();
                  graphics.pose().translate(xSlot * 27, ySlot * 27);
                  task.renderSlotContent(mc, graphics, itemNames);
                  graphics.pose().popMatrix();
                  boolean hasOverlayIcon = team != null && (team.completed(slot) || team.locked(slot));
                  if (!hasOverlayIcon && task.inverted() && task.customTexture() == null) {
                     graphics.pose().pushMatrix();
                     graphics.pose().translate(xSlot * 27 - 4, ySlot * 27 - 4);
                     graphics.pose().scale(1.5F, 1.5F);
                     ItemRenderUtil.blit(graphics, BARRIER_TEXTURE, 0, 0, 0, 0, 16, 16, 16, 16);
                     graphics.pose().popMatrix();
                  }

                  if (itemNames) {
                     graphics.pose().pushMatrix();
                     graphics.pose().translate(xSlot * 27 + 8, ySlot * 27 + 4);
                     graphics.pose().scale(0.3F, 0.3F);
                     renderTextWithBackground(graphics, task.typeName());
                     graphics.pose().translate(0.0F, 26.666666F);
                     graphics.pose().scale(0.8F, 0.8F);
                     renderTextWithBackground(graphics, task.renderDisplayName(mc));
                     graphics.pose().popMatrix();
                  }

                  if (team != null) {
                     if (team.completed(slot)) {
                        ItemRenderUtil.blit(graphics, BEACON_TEXTURE, xSlot * 27, ySlot * 27, 90, 222, 16, 16, 256, 256);
                     } else if (team.locked(slot)) {
                        graphics.pose().pushMatrix();
                        graphics.pose().translate(xSlot * 27, ySlot * 27);
                        graphics.pose().scale(1.0666667F, 1.0666667F);
                        ItemRenderUtil.blit(graphics, BEACON_TEXTURE, 0, 0, 113, 222, 15, 15, 256, 256);
                        graphics.pose().popMatrix();
                     }
                  }
               }
            }

            if (!itemNames) {
               List<String> lines = new ArrayList<>();
               if (bingo.getSettings().game().winCondition() != WinCondition.DEFAULT) {
                  lines.add(I18n.get("bongo.wc." + bingo.getSettings().game().winCondition().id, new Object[0]));
               }

               if (bingo.getSettings().game().teleportsPerTeam() != 0 && !bingo.won()) {
                  boolean hasRunningTeam = team != null && bingo.running();
                  int tpLeft = hasRunningTeam ? team.teleportsLeft() : bingo.getSettings().game().teleportsPerTeam();
                  String tpLeftStr = tpLeft < 0 ? I18n.get("bongo.infinite", new Object[0]) : Integer.toString(tpLeft);
                  lines.add(I18n.get(hasRunningTeam ? "bongo.tp_left" : "bongo.tp_team", new Object[]{tpLeftStr}));
               }

               if (!bingo.running() && !bingo.won()) {
                  if (bingo.active() && bingo.getSettings().game().time().limit().orElse(-1) >= 0) {
                     int maxTime = bingo.getSettings().game().time().limit().orElse(0);
                     int sec = maxTime % 60;
                     int min = maxTime / 60 % 60;
                     int hour = maxTime / 3600;
                     lines.add(I18n.get("bongo.maxtime", new Object[0]) + Util.formatTime(hour, min, sec));
                  }
               } else if (bingo.won() && bingo.winningTeam() == null) {
                  lines.add(I18n.get("bongo.draw", new Object[0]));
               } else if (!bingo.won() && bingo.tasksForWin() > 0) {
                  lines.add(I18n.get("bongo.instantwin", new Object[0]) + bingo.tasksForWin());
               } else {
                  boolean isTimer = true;
                  long millis;
                  if (bingo.won()) {
                     millis = bingo.ranUntil() - bingo.runningSince();
                  } else if (bingo.getSettings().game().time().limited()) {
                     isTimer = false;
                     millis = Math.max(0L, bingo.runningUntil() - System.currentTimeMillis());
                  } else {
                     millis = System.currentTimeMillis() - bingo.runningSince();
                  }

                  int decimal = (int)(millis / 100L % 10L);
                  int sec = (int)(millis / 1000L % 60L);
                  int min = (int)(millis / 60000L % 60L);
                  int hour = (int)millis / 3600000;
                  lines.add(I18n.get(isTimer ? "bongo.timer" : "bongo.timeleft", new Object[0]) + Util.formatTime(hour, min, sec, decimal));
               }

               if (!lines.isEmpty()) {
                  graphics.pose().pushMatrix();
                  if ((Boolean)ClientConfig.bongoMapOnTheRight.get()) {
                     graphics.pose().translate(124.0F, 133.0F);
                  } else {
                     graphics.pose().translate(0.0F, 133.0F);
                  }

                  graphics.pose().scale(1.3F, 1.3F);

                  for (int i = 0; i < lines.size(); i++) {
                     int off = ClientConfig.bongoMapOnTheRight.get() ? -mc.font.width(lines.get(i)) : 0;
                     graphics.text(mc.font, lines.get(i), off, (9 + 1) * i, -1, false);
                  }

                  graphics.pose().popMatrix();
               }
            }

            graphics.pose().popMatrix();
         }
      }
   }

   public void renderCompleted(GuiGraphicsExtractor graphics, List<Integer> colorCodes) {
      if (!colorCodes.isEmpty()) {
         int[][] rects;
         if (colorCodes.size() <= 1) {
            rects = RECTS_1;
         } else if (colorCodes.size() <= 2) {
            rects = RECTS_2;
         } else if (colorCodes.size() <= 4) {
            rects = RECTS_4;
         } else if (colorCodes.size() <= 6) {
            rects = RECTS_6;
         } else if (colorCodes.size() <= 8) {
            rects = RECTS_8;
         } else {
            rects = RECTS_16;
         }

         graphics.pose().pushMatrix();
         graphics.pose().scale(1.0833334F, 1.0833334F);

         for (int rect = 0; rect < rects.length && rect < colorCodes.size(); rect++) {
            int color = -1610612736 | colorCodes.get(rect) & 16777215;
            graphics.fill(rects[rect][0], rects[rect][1], rects[rect][2], rects[rect][3], color);
         }

         graphics.pose().popMatrix();
      }
   }

   public static void renderTextWithBackground(GuiGraphicsExtractor graphics, Component text) {
      renderTextWithBackground(graphics, text.getVisualOrderText());
   }

   public static void renderTextWithBackground(GuiGraphicsExtractor graphics, FormattedCharSequence text) {
      if (Minecraft.getInstance().font.width(text) != 0) {
         float widthHalf = Minecraft.getInstance().font.width(text) / 2.0F;
         float heightHalf = 9.0F / 2.0F;
         graphics.pose().pushMatrix();
         graphics.pose().translate(-(widthHalf + 2.0F), -(heightHalf + 2.0F));
         graphics.fill(0, 0, (int)(2.0F * widthHalf) + 4, (int)(2.0F * heightHalf) + 4, -869059789);
         graphics.pose().translate(widthHalf + 2.0F, heightHalf + 2.0F);
         graphics.text(Minecraft.getInstance().font, text, (int)(-widthHalf), (int)(-heightHalf), -1, false);
         graphics.pose().popMatrix();
      }
   }
}
