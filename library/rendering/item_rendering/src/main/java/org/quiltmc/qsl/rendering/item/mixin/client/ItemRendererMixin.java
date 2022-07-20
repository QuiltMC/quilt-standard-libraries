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

package org.quiltmc.qsl.rendering.item.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.item.api.client.QuiltItemRenderingExtensions;

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

		var item = stack.getItem();

		var matrices = quilt$matrices.get();
		matrices.push();
		matrices.translate(x, y, 0);

		if (item.preRenderOverlay(matrices, renderer, this.zOffset, stack)) {
			item.getCountLabelRenderer().renderCountLabel(matrices, renderer, this.zOffset, stack, countLabel);

			var itemBarRenderers = item.getItemBarRenderers();

			int itemBarY = 13;
			if (itemBarRenderers.length == 1) {
				var itemBar = itemBarRenderers[0];
				if (itemBar.isItemBarVisible(stack)) {
					matrices.push();
					matrices.translate(0, itemBarY, 0);
					itemBar.renderItemBar(matrices, renderer, this.zOffset, stack);
					matrices.pop();
				}
			} else if (itemBarRenderers.length > 0) {
				boolean anyItemBarsVisible = false;
				for (var itemBar : itemBarRenderers) {
					if (itemBar.isItemBarVisible(stack)) {
						itemBarY -= 2;
						anyItemBarsVisible = true;
					}
				}

				if (anyItemBarsVisible) {
					// TODO figure out if we can NOT render immediately here
					for (var itemBar : itemBarRenderers) {
						if (itemBar.isItemBarVisible(stack)) {
							itemBarY += 2;

							matrices.push();
							matrices.translate(0, itemBarY, 0);
							itemBar.renderItemBar(matrices, renderer, this.zOffset, stack);
							matrices.pop();
						}
					}
				}
			}

			item.getCooldownOverlayRenderer().renderCooldownOverlay(matrices, renderer, this.zOffset, stack);

			item.postRenderOverlay(matrices, renderer, this.zOffset, stack);
		}

		matrices.pop();
	}
}
