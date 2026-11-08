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

public class PlayerTeleporterNothing implements PlayerTeleporter {
   public static final PlayerTeleporterNothing INSTANCE = new PlayerTeleporterNothing();

   private PlayerTeleporterNothing() {
   }

   @Override
   public String id() {
      return "bongo.no_tp";
   }

   @Override
   public void teleportTeam(Bingo bingo, ServerLevel gameLevel, Team team, List<ServerPlayer> players, BlockPos center, int radius, Random random) {
      MutableBlockPos mpos = new MutableBlockPos(center.getX(), gameLevel.getMaxY(), center.getZ());

      while (mpos.getY() > 5 && gameLevel.getBlockState(mpos).isAir()) {
         mpos.move(Direction.DOWN);
      }

      BlockPos pos = mpos.immutable().above();
      players.forEach(player -> {
         if (player.level() != gameLevel) {
            player.teleportTo(gameLevel, center.getX() + 0.5, pos.getY(), center.getZ() + 0.5, Set.of(), player.getYHeadRot(), 0.0F, false);
         }

         BlockPos respawn = player.level() == gameLevel ? player.blockPosition() : pos;
         player.setRespawnPosition(new RespawnConfig(RespawnData.of(gameLevel.dimension(), respawn, 0.0F, 0.0F), true), false);
      });
   }
}
