/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.component.test.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;

import org.quiltmc.qsl.component.test.ComponentTestMod;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
	@Shadow
	@Final
	private ItemRenderer itemRenderer;
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "render", at = @At("TAIL"))
	private void renderCustom(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		this.client.world
				.ifPresent(ComponentTestMod.SAVE_FLOAT, saveFloatSerializable ->
						this.getTextRenderer().draw(
								matrices,
								String.valueOf(saveFloatSerializable.get()),
								10, 10,
								0xfafafa
						)
				);

		Entity entity = this.client.targetedEntity;
		if (entity != null) {
			entity.ifPresent(
					ComponentTestMod.HOSTILE_EXPLODE_TIME,
					defaultIntegerSerializable -> this.getTextRenderer().draw(
							matrices,
							String.valueOf(defaultIntegerSerializable.get()),
							10, 10,
							0xfafafa
					)
			);
		}

		ChunkPos chunkPos = this.client.player.getChunkPos();
		this.client.world.getChunk(chunkPos.x, chunkPos.z)
						 .ifPresent(ComponentTestMod.CHUNK_INVENTORY, chunkInventorySerializable -> {
							 var stack = chunkInventorySerializable.getStack(0);
							 this.itemRenderer.renderInGui(stack, 10, 20);
						 });

		this.client.player.ifPresent(ComponentTestMod.UUID_THING, uuidField -> {
			if (uuidField.getValue() == null) {
				return;
			}
			var uuidString = uuidField.getValue().toString();
			this.client.textRenderer.draw(matrices, uuidString, 10, 30, 0xFAFAFA);
		});
	}
}
