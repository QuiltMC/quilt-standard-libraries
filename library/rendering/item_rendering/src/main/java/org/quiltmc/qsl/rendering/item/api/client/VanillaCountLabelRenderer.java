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

package org.quiltmc.qsl.rendering.item.api.client;

import com.mojang.blaze3d.vertex.Tessellator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * A {@link CountLabelRenderer} implementation that replicates vanilla behavior.
 */
@Environment(EnvType.CLIENT)
public class VanillaCountLabelRenderer implements CountLabelRenderer {
	@Override
	public void renderCountLabel(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack, @Nullable String override) {
		String label = getCountLabel(stack, override);
		if (label == null) {
			return;
		}

		matrices.push();
		matrices.translate(0, 0, zOffset + 200);
		// TODO figure out if we can NOT render immediately here
		var immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBufferBuilder());
		renderer.draw(
				label,
				19 - 2 - renderer.getWidth(label),
				6 + 3,
				0xFFFFFF,
				true,
				matrices.peek().getPosition(),
				immediate,
				false,
				0x000000,
				LightmapTextureManager.MAX_LIGHT_COORDINATE
		);
		immediate.draw();
		matrices.pop();
	}

	/**
	 * Gets the item stack's count label's contents.
	 * Subclasses may override this method to decorate the count label.
	 * <p>
	 * If {@code override} is not null, this returns {@code override}.<br>
	 * Otherwise, if the stack has more than 1 item, this returns the size of the stack.<br>
	 * Otherwise, this returns {@code null}.
	 *
	 * @param stack the item stack
	 * @param override the label contents, or {@code null} to use the default contents
	 * @return the label contents to draw, or {@code null} to not draw a label
	 */
	protected @Nullable String getCountLabel(ItemStack stack, @Nullable String override) {
		if (override != null) {
			return override;
		} else if (stack.getCount() > 1) {
			return Integer.toString(stack.getCount());
		} else {
			return null;
		}
	}
}
