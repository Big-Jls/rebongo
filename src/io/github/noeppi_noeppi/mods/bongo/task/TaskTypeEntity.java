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

import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.render.RenderOverlay;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import io.github.noeppi_noeppi.mods.bongo.util.RenderEntityCache;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class TaskTypeEntity extends RegistryTaskType<EntityType<?>> {
   public static final TaskTypeEntity INSTANCE = new TaskTypeEntity();
   private static final Set<EntityType<?>> FALLBACK_LOGGED = new HashSet<>();

   private TaskTypeEntity() {
      super((Class<EntityType<?>>)(Class<?>)EntityType.class, BuiltInRegistries.ENTITY_TYPE);
   }

   @Override
   public String id() {
      return "bongo.entity";
   }

   @Override
   public Component name() {
      return Component.translatable("bongo.task.entity.name");
   }

   public Component contentName(EntityType<?> element, @Nullable MinecraftServer server) {
      return element.getDescription();
   }

   @Override
   public Stream<EntityType<?>> listElements(MinecraftServer server, @Nullable ServerPlayer player) {
      return player == null
         ? BuiltInRegistries.ENTITY_TYPE.stream().filter(type -> type.canSummon() && type.getCategory() != MobCategory.MISC)
         : player.level()
            .getEntities(
               player,
               new AABB(player.getX() - 10.0, player.getY() - 5.0, player.getZ() - 10.0, player.getX() + 10.0, player.getY() + 5.0, player.getZ() + 10.0)
            )
            .stream()
            .<EntityType<?>>map(Entity::getType)
            .collect(Collectors.toSet())
            .stream();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      graphics.pose().translate(-2.0F, -2.0F);
      graphics.pose().scale(0.84615386F);
      ItemRenderUtil.blit(graphics, RenderOverlay.BINGO_SLOTS_TEXTURE, 0, 0, 0, 44, 26, 26, 256, 256);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, EntityType<?> element, boolean bigBongo) {
      Entity entity = RenderEntityCache.getRenderEntity(mc, (EntityType<Entity>)element);
      if (entity == null) {
         renderFallback(graphics, element, bigBongo, null);
      } else {
         try {
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            EntityRenderer<Entity, EntityRenderState> renderer = (EntityRenderer<Entity, EntityRenderState>)(EntityRenderer<?, ?>)dispatcher.getRenderer(entity);
            if (renderer == null) {
               renderFallback(graphics, element, bigBongo, null);
               return;
            }

            EntityRenderState state = renderer.createRenderState(entity, 1.0F);
            state.shadowPieces.clear();
            state.outlineColor = 0;
            AABB bb = entity.getBoundingBox();
            float width = (float)Math.max(0.1, bb.getXsize());
            float height = (float)Math.max(0.1, bb.getYsize());
            float depth = (float)Math.max(0.1, bb.getZsize());
            Matrix3x2fStack pose = graphics.pose();
            Vector2f pos0 = pose.transformPosition(new Vector2f(0.0F, 0.0F));
            Vector2f pos1 = pose.transformPosition(new Vector2f(16.0F, 16.0F));
            float poseScale = (float)Math.max(0.01, Math.hypot(pose.m00(), pose.m10()));
            int x0 = Math.round(pos0.x());
            int y0 = Math.round(pos0.y());
            int x1 = Math.round(pos1.x());
            int y1 = Math.round(pos1.y());
            float scale = Math.min(Math.min(8.0F * poseScale / width, 16.0F * poseScale / height), 8.0F * poseScale / depth);
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
            rotation.mul(new Quaternionf().rotateY((float)Math.toRadians(45.0)));
            float renderHeight = state.boundingBoxHeight > 0.0F ? state.boundingBoxHeight : height;
            Vector3f translation = new Vector3f(0.0F, renderHeight / 2.0F, 0.0F);
            graphics.entity(state, scale, translation, rotation, new Quaternionf(), x0, y0, x1, y1);
         } catch (Exception e) {
            renderFallback(graphics, element, bigBongo, e);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderFallback(GuiGraphicsExtractor graphics, EntityType<?> element, boolean bigBongo, @Nullable Throwable error) {
      if (FALLBACK_LOGGED.add(element)) {
         if (error != null) {
            BingoMod.logger.warn("Could not render bingo icon for entity type {}, using item fallback", EntityType.getKey(element), error);
         } else {
            BingoMod.logger.warn("Could not create an entity for bingo icon {}, using item fallback", EntityType.getKey(element));
         }
      }

      ItemStack stack = SpawnEggItem.byId(element).<ItemStack>map(egg -> new ItemStack(egg.value())).orElseGet(() -> new ItemStack(Items.SPAWNER));
      ItemRenderUtil.renderItem(graphics, stack, !bigBongo);
   }
}
