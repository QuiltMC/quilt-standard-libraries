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

package org.quiltmc.qsl.item.rendering.api.client;

import com.mojang.blaze3d.vertex.Tessellator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

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
