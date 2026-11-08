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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.mods.bongo.command.BingoCommands;
import io.github.noeppi_noeppi.mods.bongo.command.arg.GameSettingsArgument;
import io.github.noeppi_noeppi.mods.bongo.command.arg.GameTasksArgument;
import io.github.noeppi_noeppi.mods.bongo.config.ClientConfig;
import io.github.noeppi_noeppi.mods.bongo.effect.DefaultEffects;
import io.github.noeppi_noeppi.mods.bongo.network.BingoNetwork;
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeAdvancement;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeBiome;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeEffect;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeEmpty;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeEntity;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeItem;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeStat;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypeTag;
import io.github.noeppi_noeppi.mods.bongo.task.TaskTypes;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporterDefault;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporterNothing;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporterStandard;
import io.github.noeppi_noeppi.mods.bongo.teleporters.PlayerTeleporters;
import javax.annotation.Nonnull;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.moddingx.libx.mod.ModX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("bongo")
public final class BingoMod extends ModX {
   public static final Logger logger = LoggerFactory.getLogger("bongo");
   private static BingoMod instance;
   private static BingoNetwork network;
   public static final Gson GSON = (Gson)Util.make(() -> {
      GsonBuilder gsonbuilder = new GsonBuilder();
      gsonbuilder.disableHtmlEscaping();
      gsonbuilder.setLenient();
      return gsonbuilder.create();
   });
   public static final Gson PRETTY_GSON = (Gson)Util.make(() -> {
      GsonBuilder gsonbuilder = new GsonBuilder();
      gsonbuilder.disableHtmlEscaping();
      gsonbuilder.setLenient();
      gsonbuilder.setPrettyPrinting();
      return gsonbuilder.create();
   });

   public BingoMod(ModContainer container) {
      instance = this;
      network = new BingoNetwork(this);
      container.registerConfig(Type.CLIENT, ClientConfig.CLIENT_CONFIG);
      IEventBus bus = container.getEventBus();
      bus.addListener(this::registerStuff);
      if (FMLEnvironment.getDist() == Dist.CLIENT) {
         bus.addListener(this::registerGuiLayers);
         bus.addListener(Keybinds::registerKeyBinds);
      }

      NeoForge.EVENT_BUS.register(new EventListener());
      NeoForge.EVENT_BUS.register(new DefaultEffects());
      NeoForge.EVENT_BUS.addListener(BingoCommands::register);
   }

   @Nonnull
   public static BingoMod getInstance() {
      return instance;
   }

   @Nonnull
   public static BingoNetwork getNetwork() {
      return network;
   }

   protected void setup(FMLCommonSetupEvent event) {
      TaskTypes.registerType(TaskTypeEmpty.INSTANCE);
      TaskTypes.registerType(TaskTypeItem.INSTANCE);
      TaskTypes.registerType(TaskTypeAdvancement.INSTANCE);
      TaskTypes.registerType(TaskTypeEntity.INSTANCE);
      TaskTypes.registerType(TaskTypeBiome.INSTANCE);
      TaskTypes.registerType(TaskTypeEffect.INSTANCE);
      TaskTypes.registerType(TaskTypeStat.INSTANCE);
      TaskTypes.registerType(TaskTypeTag.INSTANCE);
      PlayerTeleporters.registerTeleporter(PlayerTeleporterDefault.INSTANCE);
      PlayerTeleporters.registerTeleporter(PlayerTeleporterStandard.INSTANCE);
      PlayerTeleporters.registerTeleporter(PlayerTeleporterNothing.INSTANCE);
   }

   @OnlyIn(Dist.CLIENT)
   protected void clientSetup(FMLClientSetupEvent event) {
   }

   @OnlyIn(Dist.CLIENT)
   private void registerGuiLayers(RegisterGuiLayersEvent event) {
      event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, Identifier.fromNamespaceAndPath("bongo", "bongo"), RenderOverlay.INSTANCE);
   }

   private void registerStuff(RegisterEvent event) {
      event.register(Registries.COMMAND_ARGUMENT_TYPE, this.id("tasks"), () -> GameTasksArgument.Info.INSTANCE);
      event.register(Registries.COMMAND_ARGUMENT_TYPE, this.id("settings"), () -> GameSettingsArgument.Info.INSTANCE);
   }
}
