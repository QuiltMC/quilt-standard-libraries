/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.block.entity.test.client;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.RenderLayer;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.entity.test.BlockEntityTypeTest;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

@ClientOnly
public class BlockEntityTypeTestClient implements ClientModInitializer {
	public static BlockColorProvider ANGY_BLOCK_COLOR_PROVIDER = (state, world, pos, tintIndex) -> {
		if (tintIndex == 0) {
			var blockEntity = BlockEntityTypeTest.COLORFUL_BLOCK_ENTITY_TYPE.get(world, pos);

			if (blockEntity != null) {
				return 0xff000000 | blockEntity.getColor();
			}
		}

		return -1;
	};

	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockRenderLayerMap.put(RenderLayer.getCutout(), BlockEntityTypeTest.ANGY_BLOCKS.toArray(Block[]::new));
	}
}
