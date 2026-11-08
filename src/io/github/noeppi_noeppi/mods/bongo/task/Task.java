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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.mods.bongo.BingoMod;
import io.github.noeppi_noeppi.mods.bongo.util.Highlight;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.codec.CodecHelper;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.render.RenderHelper;

public class Task {
   public static Task EMPTY = new Task(Task.TaskContent.EMPTY, Task.TaskExtension.EMPTY);
   public static final Codec<Task> CODEC = MoreCodecs.extend(
      Task.TaskContent.CODEC, Task.TaskExtension.CODEC, task -> Pair.of(task.content, task.ext), Task::new
   );
   public static final Set<JsonElement> RESERVED_KEYS = Stream.concat(
         Stream.of(new JsonPrimitive("type"), new JsonPrimitive("weight")), Task.TaskExtension.CODEC.keys(JsonOps.INSTANCE)
      )
      .collect(Collectors.toUnmodifiableSet());
   private final Task.TaskContent content;
   private final Task.TaskExtension ext;

   public <T> Task(TaskType<T> type, T element) {
      this(new Task.TaskContent(type, element), Task.TaskExtension.EMPTY);
   }

   public <T> Task(TaskType<T> type, T element, boolean inverted, @Nullable Identifier customTexture) {
      this(new Task.TaskContent(type, element), new Task.TaskExtension(inverted, Optional.ofNullable(customTexture)));
   }

   private Task(Task.TaskContent content, Task.TaskExtension ext) {
      this.content = content;
      this.ext = ext;
   }

   public TaskType<?> getType() {
      return this.content.type;
   }

   public Component typeName() {
      return (Component)(this.inverted() ? Component.translatable("bongo.avoid", new Object[]{this.content.type.name()}) : this.content.type.name());
   }

   public Component contentName(@Nullable MinecraftServer server) {
      return ((TaskType<Object>)this.content.type).contentName(this.content.element, server);
   }

   public void sync(MinecraftServer server, @Nullable ServerPlayer target) {
      ((TaskType<Object>)this.content.type).sync(this.content.element, server, target);
   }

   public <T> Task.CompletionState shouldComplete(TaskType<T> type, ServerPlayer player, T compare) {
      if (this.content.type == type) {
         if (((TaskType<T>)this.content.type).shouldComplete(player, (T)this.content.element, compare)) {
            return this.inverted() ? Task.CompletionState.LOCK : Task.CompletionState.COMPLETE;
         } else {
            return Task.CompletionState.KEEP;
         }
      } else {
         return Task.CompletionState.KEEP;
      }
   }

   public <T> void consume(TaskType<T> type, ServerPlayer player, T found) {
      if (!this.inverted() && this.content.type == type) {
         ((TaskType<T>)this.content.type).consume(player, (T)this.content.element, found);
      }
   }

   public Stream<Highlight<?>> highlight() {
      return this.inverted() ? Stream.empty() : ((TaskType<Object>)this.content.type).highlight(this.content.element);
   }

   public void invalidate() {
      ((TaskType<Object>)this.content.type).invalidate(this.content.element);
   }

   @OnlyIn(Dist.CLIENT)
   public FormattedCharSequence renderDisplayName(Minecraft mc) {
      return ((TaskType<Object>)this.content.type).renderDisplayName(mc, this.content.element);
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlot(Minecraft mc, GuiGraphicsExtractor graphics) {
      if (this.customTexture() == null) {
         this.content.type.renderSlot(mc, graphics);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void renderSlotContent(Minecraft mc, GuiGraphicsExtractor graphics, boolean bigBongo) {
      Identifier tex = this.customTexture();
      if (tex == null) {
         ((TaskType<Object>)this.content.type).renderSlotContent(mc, graphics, this.content.element, bigBongo);
      } else {
         RenderHelper.resetColor();
         ItemRenderUtil.blit(graphics, tex, -1, -1, 0, 0, 18, 18, 18, 18);
      }
   }

   public void validate(MinecraftServer server) {
      try {
         ((TaskType<Object>)this.content.type).validate(this.content.element, server);
      } catch (Exception e) {
         BingoMod.logger.error("Failed to validate task of type {}: {}", this.content.type.id(), e.getMessage());
      }
   }

   public <T> Optional<T> getElement(TaskType<T> type) {
      return this.content.type == type ? Optional.of((T)this.content.element) : Optional.empty();
   }

   @Nullable
   public Identifier customTexture() {
      return this.ext.customTexture.orElse(null);
   }

   public boolean inverted() {
      return this.ext.inverted;
   }

   public boolean isWinningTask(boolean completed, boolean locked) {
      return this.inverted() ? !locked : completed;
   }

   public enum CompletionState {
      KEEP(false, false),
      COMPLETE(true, false),
      LOCK(false, true);

      public final boolean shouldComplete;
      public final boolean shouldLock;

      CompletionState(boolean shouldComplete, boolean shouldLock) {
         this.shouldComplete = shouldComplete;
         this.shouldLock = shouldLock;
      }
   }

   private static class TaskContent {
      public static final Task.TaskContent EMPTY = new Task.TaskContent(TaskTypeEmpty.INSTANCE, Unit.INSTANCE);
      public static final Codec<Task.TaskContent> CODEC = MoreCodecs.mapDispatch(
         TaskTypes.CODEC.fieldOf("type"),
         TaskTypes::getCodec,
         content -> Pair.of(content.type, content.element),
         (type, element) -> CodecHelper.doesNotThrow(() -> new Task.TaskContent((TaskType<Object>)type, element))
      );
      private final TaskType<?> type;
      private final Object element;

      private <T> TaskContent(TaskType<T> type, T element) {
         this.type = type;
         this.element = element;
         this.validateElementType();
      }

      private void validateElementType() {
         if (!this.type.taskClass().isAssignableFrom(this.element.getClass())) {
            throw new IllegalStateException("Can't create task of type " + this.type.id() + " with element of type " + this.element.getClass());
         }
      }
   }

   private record TaskExtension(boolean inverted, Optional<Identifier> customTexture) {
      public static final Task.TaskExtension EMPTY = new Task.TaskExtension(false, Optional.empty());
      public static final MapCodec<Task.TaskExtension> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               Codec.BOOL.optionalFieldOf("inverted", false).forGetter(Task.TaskExtension::inverted),
               Identifier.CODEC.optionalFieldOf("custom_texture").forGetter(Task.TaskExtension::customTexture)
            )
            .apply(instance, Task.TaskExtension::new)
      );
   }
}
