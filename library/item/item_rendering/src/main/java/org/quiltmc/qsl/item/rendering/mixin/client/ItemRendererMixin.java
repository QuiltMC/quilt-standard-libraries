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

package org.quiltmc.qsl.item.rendering.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.rendering.api.client.QuiltItemRenderingExtensions;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Unique private final ThreadLocal<MatrixStack> quilt$matrices = ThreadLocal.withInitial(MatrixStack::new);

	@Shadow public float zOffset;

	/**
	 * @author QuiltMC
	 * @reason Completely refactoring overlay rendering logic to use {@link QuiltItemRenderingExtensions}
	 */
	@Overwrite
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
		if (stack.isEmpty()) {
			return;
		}

		var item = (QuiltItemRenderingExtensions) stack.getItem();

		var matrices = quilt$matrices.get();
		matrices.push();

		var countLabelProvider = item.getCountLabelProvider();
		if (countLabelProvider.isCountLabelVisible(stack, countLabel)) {
			var text = countLabelProvider.getCountLabelText(stack, countLabel).asOrderedText();

			matrices.push();
			matrices.translate(x + 19 - 2 - renderer.getWidth(text), y + 6 + 3, zOffset + 200);
			// TODO figure out if we can NOT render immediately here
			var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBufferBuilder());
			renderer.draw(
					text,
					0,
					0,
					0xFFFFFF,
					true,
					matrices.peek().getPosition(),
					immediate,
					false,
					0x000000,
					0xF000F0
			);
			immediate.draw();
			matrices.pop();
		}

		var itemBarProviders = item.getItemBarProviders();
		int itemBarY = y + 13;
		boolean anyItemBarsVisible = false;
		for (var provider : itemBarProviders) {
			if (provider.isItemBarVisible(stack)) {
				itemBarY -= 2;
				anyItemBarsVisible = true;
			}
		}

		if (anyItemBarsVisible) {
			// TODO figure out if we can NOT render immediately here
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBufferBuilder();
			for (var provider : itemBarProviders) {
				if (provider.isItemBarVisible(stack)) {
					matrices.push();
					matrices.translate(x, itemBarY, 0);
					provider.renderItemBar(matrices, buffer, stack);
					itemBarY += 2;
					matrices.pop();
				}
			}
		}

		var cooldownProvider = item.getCooldownOverlayProvider();
		if (cooldownProvider.isCooldownOverlayVisible(stack)) {
			int step = cooldownProvider.getCooldownOverlayStep(stack);
			int color = cooldownProvider.getCooldownOverlayColor(stack);

			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBufferBuilder();
			this.renderGuiQuad(matrices, buffer, x, y + (16 - step), 16, step, color);
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}

		matrices.pop();
	}

	// TODO figure out if we can NOT render immediately here
	@Unique
	private void renderGuiQuad(MatrixStack matrices,
							   BufferBuilder buffer, int x, int y, int width, int height, int color) {
		var mat = matrices.peek().getPosition();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = (color >> 24) & 0xFF;

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		buffer.vertex(mat, x, y, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y + height, 0.0f).color(r, g, b, a).next();
		buffer.vertex(mat, x + width, y, 0.0f).color(r, g, b, a).next();
		BufferRenderer.drawWithShader(buffer.end());
	}
}
