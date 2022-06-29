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

package org.quiltmc.qsl.component.test.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import org.quiltmc.qsl.component.test.ComponentTestMod;
import org.quiltmc.qsl.component.test.TestBlockEntity;

import java.util.function.Predicate;

public class TestBeRenderer implements BlockEntityRenderer<TestBlockEntity> {
	@Override
	public void render(TestBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		entity.expose(ComponentTestMod.CHUNK_INVENTORY)
				.map(inventoryComponent -> inventoryComponent.getStack(0))
				.filter(Predicate.not(ItemStack::isEmpty))
				.ifPresent(itemStack -> {
					matrices.push();
					matrices.translate(0.5f, 1, 0.5f);
					matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(System.currentTimeMillis() % 360));
					MinecraftClient.getInstance().getItemRenderer().renderItem(
							itemStack,
							ModelTransformation.Mode.GROUND,
							0xffffff,
							OverlayTexture.DEFAULT_UV,
							matrices,
							vertexConsumers,
							0
					);
					matrices.pop();
				});
	}
}
