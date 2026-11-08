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

import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.StatAndValue;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.render.RenderHelper;

public class TaskTypeStat implements TaskType<StatAndValue> {
   public static final TaskTypeStat INSTANCE = new TaskTypeStat();
   public static final Identifier STAT_ICONS_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/stats_icons.png");

   private static Object2IntMap<Stat<?>> rawStats(StatsCounter counter) {
      try {
         Field field = StatsCounter.class.getDeclaredField("stats");
         field.setAccessible(true);
         return (Object2IntMap<Stat<?>>)field.get(counter);
      } catch (ReflectiveOperationException e) {
         return Object2IntMaps.emptyMap();
      }
   }

   private TaskTypeStat() {
   }

   @Override
   public String id() {
      return "bongo.stat";
   }

   @Override
   public Class<StatAndValue> taskClass() {
      return StatAndValue.class;
   }

   @Override
   public MapCodec<StatAndValue> codec() {
      return StatAndValue.CODEC.fieldOf("value");
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.stat.name");
   }

   public Component contentName(StatAndValue element, @Nullable MinecraftServer server) {
      Component cmp = Component.empty();
      Object value = element.stat().getValue();
      if (value instanceof ItemLike item) {
         cmp = new ItemStack(item).getHoverName();
      } else if (value instanceof EntityType<?> entity) {
         cmp = entity.getDescription();
      } else if (value instanceof Identifier rl) {
         return Component.literal(Util.resourceStr(rl));
      }

      return Component.translatable("stat_type." + BuiltInRegistries.STAT_TYPE.getKey(element.stat().getType()).toString().replace(':', '.'))
         .append(Component.literal(" "))
         .append(cmp);
   }

   @Override
   public Comparator<StatAndValue> order() {
      return Comparator.<StatAndValue, Identifier>comparing(stat -> BuiltInRegistries.STAT_TYPE.getKey(stat.stat().getType()), Util.COMPARE_RESOURCE)
         .thenComparing(StatAndValue::getValueId, Util.COMPARE_RESOURCE)
         .thenComparingInt(StatAndValue::value);
   }

   public void validate(StatAndValue element, MinecraftServer server) {
      element.getValueId();
   }

   @Override
   public Stream<StatAndValue> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      if (player != null) {
         ServerStatsCounter mgr = server.getPlayerList().getPlayerStats(player);
         return rawStats(mgr)
            .object2IntEntrySet()
            .stream()
            .filter(entry -> entry.getIntValue() > 0)
            .map(entry -> new StatAndValue((Stat<?>)entry.getKey(), entry.getIntValue()));
      } else {
         return BuiltInRegistries.STAT_TYPE
            .stream()
            .flatMap(type -> {
               Stream.Builder<StatAndValue> builder = Stream.builder();
               for (Object stat : (StatType)type) {
                  builder.add(new StatAndValue((Stat<?>)stat, 1));
               }
               return builder.build();
            });
      }
   }

   public boolean shouldComplete(ServerPlayer player, StatAndValue element, StatAndValue compare) {
      return element.stat().getType().equals(compare.stat().getType())
         && element.stat().getValue().equals(compare.stat().getValue())
         && compare.value() >= element.value();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 36, 0, 18, 18, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, StatAndValue element, boolean bigBongo) {
      Object value = element.stat().getValue();
      if (value instanceof ItemLike) {
         ItemStack renderStack = new ItemStack((ItemLike)value, element.value());
         ItemRenderUtil.renderItem(graphics, renderStack, false);
         int x = -1;
         if (element.stat().getType() == Stats.ITEM_CRAFTED) {
            x = 19;
         } else if (element.stat().getType() == Stats.ITEM_USED) {
            x = 37;
         } else if (element.stat().getType() == Stats.BLOCK_MINED) {
            x = 55;
         } else if (element.stat().getType() == Stats.ITEM_BROKEN) {
            x = 74;
         } else if (element.stat().getType() == Stats.ITEM_PICKED_UP) {
            x = 91;
         } else if (element.stat().getType() == Stats.ITEM_DROPPED) {
            x = 109;
         }

         if (x >= 0) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(9.0F, -1.0F);
            graphics.pose().scale(0.5F, 0.5F);
            ItemRenderUtil.blit(graphics, RenderOverlay.ICONS_TEXTURE, 0, 0, 32, 0, 16, 16, 256, 256);
            graphics.pose().translate(0.0F, 0.0F);
            ItemRenderUtil.blit(graphics, STAT_ICONS_TEXTURE, 0, 0, x, 19, 16, 16, 128, 128);
            graphics.pose().popMatrix();
         }
      } else if (value instanceof EntityType) {
         TaskTypeEntity.INSTANCE.renderSlotContent(mc, graphics, (EntityType<?>)value, bigBongo);
      } else {
         boolean foundCustomTex = false;
         if (Stats.CUSTOM.equals(element.stat().getType()) && element.stat().getValue() instanceof Identifier) {
            Identifier textureLocation = Identifier.fromNamespaceAndPath(
               ((Identifier)element.stat().getValue()).getNamespace(), "textures/icon/stat/" + ((Identifier)element.stat().getValue()).getPath() + ".png"
            );
            foundCustomTex = true;
            graphics.pose().pushMatrix();
            graphics.pose().scale(0.5F, 0.5F);
            ItemRenderUtil.blit(graphics, textureLocation, 0, 0, 0, 0, 32, 32, 32, 32);
            graphics.pose().popMatrix();
         }

         if (!foundCustomTex) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(0.5F, 0.5F);
            ItemRenderUtil.blit(graphics, RenderOverlay.ICONS_TEXTURE, 0, 0, 0, 0, 32, 32, 256, 256);
            graphics.pose().popMatrix();
         }
      }

      if (!bigBongo) {
         graphics.pose().pushMatrix();
         graphics.pose().translate(0.0F, 0.0F);
         graphics.pose().scale(0.6666667F, 0.6666667F);
         String text = element.stat().format(element.value());
         RenderHelper.resetColor();
         Font font = Minecraft.getInstance().font;
         graphics.text(font, text, 25 - font.width(text), 17, 16777215, true);
         graphics.pose().popMatrix();
      }
   }
}
