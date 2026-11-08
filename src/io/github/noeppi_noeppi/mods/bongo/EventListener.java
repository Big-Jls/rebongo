/*
 * Original work: Bingo - Copyright (c) noeppi_noeppi.
 * Licensed under the Apache License, Version 2.0.
 *
 * MODIFIED for Minecraft 26.2 / NeoForge by Big_Jls (with DeepSeek-v4.1-Flash-1017) on 2026-11-08.
 * Changes: Forge 1.20.1 to NeoForge 26.2 port (registration, payloads, data loading,
 * rendering, commands, codecs); upstream ids and namespaces restored.
 * See LICENSE, NOTICE and THIRD-PARTY-NOTICES.md.
 */
package io.github.noeppi_noeppi.mods.bongo;

import io.github.noeppi_noeppi.mods.bongo.config.ClientConfig;
import io.github.noeppi_noeppi.mods.bongo.data.Team;
import io.github.noeppi_noeppi.mods.bongo.data.settings.GameSettings;
import io.github.noeppi_noeppi.mods.bongo.data.task.GameTasks;
import io.github.noeppi_noeppi.mods.bongo.network.BingoMessageType;
import io.github.noeppi_noeppi.mods.bongo.task.Task;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeAdvancement;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeBiome;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeEffect;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeEntity;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeItem;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeStat;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeTag;
import io.github.noeppi_noeppi.mods.bongo.util.StatAndValue;
import io.github.noeppi_noeppi.mods.bongo.util.TagWithCount;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentInitializers.PendingComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.NameFormat;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.TabListNameFormat;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

public class EventListener {
   @SubscribeEvent(priority = EventPriority.LOW)
   public void playerJoin(PlayerLoggedInEvent event) {
      BingoMod.getNetwork().updateBingo(event.getEntity());
      Level level = event.getEntity().level();
      if (!level.isClientSide() && level instanceof ServerLevel && event.getEntity() instanceof ServerPlayer) {
         Bingo bingo = Bingo.get(level);
         if (bingo.running() && bingo.getSettings().server().preventJoiningDuringGame()) {
            boolean playerFound = false;

            for (Team team : bingo.getTeams()) {
               if (team.hasPlayer(event.getEntity())) {
                  playerFound = true;
               }
            }

            if (!playerFound && !event.getEntity().permissions().hasPermission(new HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
               ((ServerPlayer)event.getEntity()).connection.disconnect(Component.translatable("bongo.disconnect"));
               return;
            }
         }

         for (Task task : bingo.tasks()) {
            if (task != null) {
               task.sync(level.getServer(), (ServerPlayer)event.getEntity());
            }
         }

         BingoMod.getNetwork().updateBingo(event.getEntity(), BingoMessageType.FORCE);
      }
   }

   @SubscribeEvent
   public void playerChangeDim(PlayerChangedDimensionEvent event) {
      BingoMod.getNetwork().updateBingo(event.getEntity());
   }

   @SubscribeEvent
   public void advancementGrant(AdvancementEarnEvent event) {
      Level level = event.getEntity().level();
      if (!level.isClientSide()) {
         Bingo.get(level).checkCompleted(TaskTypeAdvancement.INSTANCE, event.getEntity(), event.getAdvancement().id());
      }
   }

   @SubscribeEvent
   public void potionAdd(Added event) {
      if (event.getEntity() instanceof Player player) {
         Level level = player.level();
         if (!level.isClientSide()) {
            Bingo.get(level).checkCompleted(TaskTypeEffect.INSTANCE, player, (MobEffect)event.getEffectInstance().getEffect().value());
         }
      }
   }

   @SubscribeEvent
   public void serverTick(Post event) {
      if (event.getServer().getTickCount() % 20 == 0) {
         for (ServerLevel level : event.getServer().getAllLevels()) {
            Bingo bingo = Bingo.get(level);
            if (bingo.running()) {
               bingo.checkWin();
            }
         }
      }
   }

   @SubscribeEvent
   public void playerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.hasEffect(MobEffects.NIGHT_VISION)) {
         serverPlayer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, true, false, false));
      }

      if (!event.getEntity().level().isClientSide() && event.getEntity().tickCount % 20 == 0 && event.getEntity() instanceof ServerPlayer) {
         Bingo bingo = Bingo.get(event.getEntity().level());
         if (bingo.canCompleteTasks(event.getEntity())) {
            Map<ItemStack, Integer> stacks = new HashMap<>();
            bingo.getElementsOf(TaskTypeItem.INSTANCE).forEach(stackx -> stacks.put(stackx, 0));
            Map<TagWithCount, Integer> tags = new HashMap<>();
            bingo.getElementsOf(TaskTypeTag.INSTANCE).forEach(tag -> tags.put(tag, 0));

            for (ItemStack stack : event.getEntity().getInventory().getNonEquipmentItems()) {
               if (!stack.isEmpty()) {
                  for (Entry<ItemStack, Integer> entry : stacks.entrySet()) {
                     ItemStack test = entry.getKey();
                     if (ItemStack.isSameItemSameComponents(stack, test)) {
                        entry.setValue(entry.getValue() + stack.getCount());
                     }
                  }

                  for (Entry<TagWithCount, Integer> entry : tags.entrySet()) {
                     if (entry.getKey().contains(stack.getItem())) {
                        entry.setValue(entry.getValue() + stack.getCount());
                     }
                  }
               }
            }

            stacks.forEach((stackx, count) -> {
               ItemStack testx = stackx.copy();
               testx.setCount(count);
               bingo.checkCompleted(TaskTypeItem.INSTANCE, event.getEntity(), testx);
            });
            tags.forEach((tag, count) -> bingo.checkCompleted(TaskTypeTag.INSTANCE, event.getEntity(), tag.withCount(count)));

            try {
               Identifier biomeKey = event.getEntity()
                  .level()
                  .registryAccess()
                  .lookupOrThrow(Registries.BIOME)
                  .getKey((Biome)event.getEntity().level().getBiome(event.getEntity().blockPosition()).value());
               bingo.checkCompleted(TaskTypeBiome.INSTANCE, event.getEntity(), biomeKey);
            } catch (Exception e) {
               e.printStackTrace();
            }

            if (bingo.getSettings().game().invulnerable()) {
               event.getEntity().getFoodData().setFoodLevel(20);
               event.getEntity().setAirSupply(event.getEntity().getMaxAirSupply());
            }

            ServerStatsCounter mgr = ((ServerPlayer)event.getEntity()).level().getServer().getPlayerList().getPlayerStats(event.getEntity());
            bingo.getElementsOf(TaskTypeStat.INSTANCE)
               .map(value -> new StatAndValue(value.stat(), mgr.getValue(value.stat())))
               .forEach(value -> bingo.checkCompleted(TaskTypeStat.INSTANCE, event.getEntity(), value));
         }
      }
   }

   @SubscribeEvent
   public void serverStart(ServerStartedEvent event) {
      GameTasks.validateAllTasks(event.getServer());
   }

   @SubscribeEvent
   public void resourcesReload(AddServerReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath("bongo", "bongo_data"), new SimplePreparableReloadListener<Object>() {
         @Nonnull
         protected Object prepare(@Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
            return new Object();
         }

         protected void apply(@Nonnull Object object, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
            for (PendingComponents<?> pending : BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(event.getRegistryAccess())) {
               pending.apply();
            }

            try {
               GameTasks.loadGameTasks(resourceManager);
               GameSettings.loadGameSettings(resourceManager);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }
      });
   }

   @SubscribeEvent
   public void resourcesReloaded(OnDatapackSyncEvent event) {
      if (event.getPlayer() == null) {
         GameTasks.validateAllTasks(event.getPlayerList().getServer());
      }
   }

   @SubscribeEvent
   public void damage(LivingIncomingDamageEvent event) {
      this.handleCommonDamageEvent(event, event.getSource());
   }

   private void handleCommonDamageEvent(LivingIncomingDamageEvent event, DamageSource source) {
      if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Player player && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         Bingo bingo = Bingo.get(player.level());
         Team team = bingo.getTeam(player);
         if (bingo.running() && team != null) {
            if (source.getEntity() instanceof Player damageSourcePlayer) {
               if (!bingo.getSettings().game().pvp()) {
                  cancelDamage(event);
               } else if (team.hasPlayer(damageSourcePlayer) && !bingo.getSettings().game().friendlyFire()) {
                  cancelDamage(event);
               }
            } else if (bingo.getSettings().game().invulnerable()) {
               cancelDamage(event);
            }
         }
      }
   }

   private static void cancelDamage(LivingIncomingDamageEvent event) {
      event.setCanceled(true);
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public void addTooltip(ItemTooltipEvent event) {
      if ((Boolean)ClientConfig.addItemTooltips.get()) {
         ItemStack stack = event.getItemStack();
         if (stack.isEmpty() || event.getEntity() == null) {
            return;
         }

         Bingo bingo = Bingo.get(event.getEntity().level());
         if (bingo.active() && bingo.isTooltipStack(stack)) {
            event.getToolTip().add(Util.REQUIRED_ITEM);
         }
      }
   }

   @SubscribeEvent
   public void playerName(NameFormat event) {
      Player player = event.getEntity();
      Bingo bingo = Bingo.get(player.level());
      if (bingo.active()) {
         Team team = bingo.getTeam(player);
         if (team != null) {
            Component tc = event.getDisplayname();
            MutableComponent var6;
            if (tc instanceof MutableComponent) {
               var6 = ((MutableComponent)tc).withStyle(team.getFormatting());
            } else {
               var6 = Component.literal(event.getEntity().getScoreboardName()).withStyle(team.getFormatting());
            }

            event.setDisplayname(var6);
         }
      }
   }

   @SubscribeEvent
   public void tablistName(TabListNameFormat event) {
      Player player = event.getEntity();
      Bingo bingo = Bingo.get(player.level());
      if (bingo.active()) {
         Team team = bingo.getTeam(player);
         if (team != null) {
            Component tc = event.getDisplayName();
            MutableComponent var6;
            if (tc instanceof MutableComponent) {
               var6 = ((MutableComponent)tc).withStyle(team.getFormatting());
            } else {
               var6 = Component.literal(event.getEntity().getScoreboardName()).withStyle(team.getFormatting());
            }

            event.setDisplayName(var6);
         }
      }
   }

   @SubscribeEvent
   public void entityDie(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof Player player) {
         Bingo bingo = Bingo.get(player.level());
         bingo.checkCompleted(TaskTypeEntity.INSTANCE, player, event.getEntity().getType());
      }

      if (event.getEntity() instanceof Player player && !event.getEntity().level().isClientSide()) {
         Bingo bingo = Bingo.get(player.level());
         Util.handleTaskLocking(bingo, player);
      }
   }

   @SubscribeEvent
   public void serverChat(ServerChatEvent event) {
      if (!ModList.get().isLoaded("minemention") && event.getPlayer() != null) {
         Bingo bingo = Bingo.get(event.getPlayer().level());
         if (bingo.teamChat(event.getPlayer())) {
            Team team = bingo.getTeam(event.getPlayer());
            if (team != null) {
               event.setCanceled(true);
               MutableComponent tc = Component.literal("[");
               tc.append(team.getName());
               tc.append(Component.literal("] ").withStyle(ChatFormatting.RESET));
               tc.append(event.getMessage());
               Util.broadcastTeam(event.getPlayer().level(), team, tc);
            }
         }
      }
   }
}
