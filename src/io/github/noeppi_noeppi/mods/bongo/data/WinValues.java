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

public class WinValues {
   public static final int[][] VALUES_ROWS = new int[][]{
      {xy(0, 0), xy(1, 0), xy(2, 0), xy(3, 0), xy(4, 0)},
      {xy(0, 1), xy(1, 1), xy(2, 1), xy(3, 1), xy(4, 1)},
      {xy(0, 2), xy(1, 2), xy(2, 2), xy(3, 2), xy(4, 2)},
      {xy(0, 3), xy(1, 3), xy(2, 3), xy(3, 3), xy(4, 3)},
      {xy(0, 4), xy(1, 4), xy(2, 4), xy(3, 4), xy(4, 4)}
   };
   public static final int[][] VALUES_COLS = new int[][]{
      {xy(0, 0), xy(0, 1), xy(0, 2), xy(0, 3), xy(0, 4)},
      {xy(1, 0), xy(1, 1), xy(1, 2), xy(1, 3), xy(1, 4)},
      {xy(2, 0), xy(2, 1), xy(2, 2), xy(2, 3), xy(2, 4)},
      {xy(3, 0), xy(3, 1), xy(3, 2), xy(3, 3), xy(3, 4)},
      {xy(4, 0), xy(4, 1), xy(4, 2), xy(4, 3), xy(4, 4)}
   };
   public static final int[][] VALUES_DIAGONAL = new int[][]{
      {xy(0, 0), xy(1, 1), xy(2, 2), xy(3, 3), xy(4, 4)}, {xy(0, 4), xy(1, 3), xy(2, 2), xy(3, 1), xy(4, 0)}
   };

   private static int xy(int x, int y) {
      return x + 5 * y;
   }
}
