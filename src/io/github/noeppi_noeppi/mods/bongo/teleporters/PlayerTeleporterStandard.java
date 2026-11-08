/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo.teleporters;

import io.github.noeppi_noeppi.mods.bongo.Bingo;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.world.level.storage.LevelData.RespawnData;

public class PlayerTeleporterStandard implements PlayerTeleporter {
   public static final PlayerTeleporterStandard INSTANCE = new PlayerTeleporterStandard();

   private PlayerTeleporterStandard() {
   }

   @Override
   public String id() {
      return "bongo.standard";
   }

   @Override
   public void teleportTeam(Bingo bingo, ServerLevel gameLevel, Team team, List<ServerPlayer> players, BlockPos center, int radius, Random random) {
      int i = 0;

      BlockPos pos;
      do {
         int x = center.getX() + (random.nextInt(2 * radius) - radius);
         int z = center.getZ() + (random.nextInt(2 * radius) - radius);
         MutableBlockPos mpos = new MutableBlockPos(x, gameLevel.getMaxY(), z);

         while (mpos.getY() > gameLevel.getMinY() + 5 && gameLevel.getBlockState(mpos).isAir()) {
            mpos.move(Direction.DOWN);
         }

         pos = mpos.immutable().above();
      } while (!Util.validSpawn(gameLevel, pos) && ++i < 10);

      BlockPos effectiveFinalPos = pos;
      players.forEach(
         player -> {
            player.teleportTo(
               gameLevel, effectiveFinalPos.getX() + 0.5, effectiveFinalPos.getY(), effectiveFinalPos.getZ() + 0.5, Set.of(), player.getYHeadRot(), 0.0F, false
            );
            player.setRespawnPosition(new RespawnConfig(RespawnData.of(gameLevel.dimension(), effectiveFinalPos, 0.0F, 0.0F), true), false);
         }
      );
   }
}
