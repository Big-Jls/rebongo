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

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.phys.shapes.BooleanOp;

public enum WinCondition {
   ONE("bongo.one", one()),
   ALL("bongo.all", all()),
   ROWS("bongo.rows", when(WinValues.VALUES_ROWS)),
   COLUMNS("bongo.columns", when(WinValues.VALUES_COLS)),
   DIAGONALS("bongo.diagonals", when(WinValues.VALUES_DIAGONAL)),
   ROWS_AND_COLUMNS("bongo.rows_and_columns", compose(BooleanOp.OR, ROWS, COLUMNS)),
   DEFAULT("bongo.default", compose(BooleanOp.OR, ROWS, COLUMNS, DIAGONALS)),
   ROW_AND_COLUMN("bongo.row_and_column", compose(BooleanOp.AND, ROWS, COLUMNS)),
   LOCK("bongo.lock", compose(BooleanOp.OR, ROWS, COLUMNS, DIAGONALS));

   public static final Codec<WinCondition> CODEC = Codec.STRING.xmap(WinCondition::getWinOrDefault, wc -> wc.id);
   public final String id;
   private final Function<TeamCompletion, Boolean> won;

   WinCondition(String id, Function<TeamCompletion, Boolean> won) {
      this.id = id;
      this.won = won;
   }

   public boolean won(TeamCompletion completion) {
      return this.won.apply(completion);
   }

   public static WinCondition getWin(String id) {
      for (WinCondition wc : values()) {
         if (wc.id.equalsIgnoreCase(id)) {
            return wc;
         }
      }

      return null;
   }

   public static WinCondition getWinOrDefault(String id) {
      for (WinCondition wc : values()) {
         if (wc.id.equalsIgnoreCase(id)) {
            return wc;
         }
      }

      return DEFAULT;
   }

   private static Function<TeamCompletion, Boolean> when(int[][] winValues) {
      return completion -> {
         label24:
         for (int[] win : winValues) {
            for (int slot : win) {
               if (!completion.has(slot)) {
                  continue label24;
               }
            }

            return true;
         }

         return false;
      };
   }

   private static Function<TeamCompletion, Boolean> compose(BooleanOp combine, WinCondition c1, WinCondition c2) {
      return completion -> combine.apply(c1.won(completion), c2.won(completion));
   }

   private static Function<TeamCompletion, Boolean> compose(BooleanOp combine, WinCondition c1, WinCondition c2, WinCondition c3) {
      return completion -> combine.apply(combine.apply(c1.won(completion), c2.won(completion)), c3.won(completion));
   }

   private static Function<TeamCompletion, Boolean> one() {
      return completion -> {
         for (int i = 0; i < 25; i++) {
            if (completion.has(i)) {
               return true;
            }
         }

         return false;
      };
   }

   private static Function<TeamCompletion, Boolean> all() {
      return completion -> {
         for (int i = 0; i < 25; i++) {
            if (!completion.has(i)) {
               return false;
            }
         }

         return true;
      };
   }
}
