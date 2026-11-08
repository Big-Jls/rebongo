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

import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class Keybinds {
   public static final KeyMapping BIG_OVERLAY = new KeyMapping("bongo.big_overlay", 89, Category.MISC);

   public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
      event.register(BIG_OVERLAY);
   }
}
