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

package org.quiltmc.qsl.item.group.test;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.item.group.api.client.ItemGroupIconRenderer;

public class ItemGroupClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ItemGroupIconRenderer.register(ItemGroup.FOOD, itemGroup -> ItemGroupIconRenderer.texture(itemGroup, new Identifier("textures/item/carrot.png")));
		ItemGroupIconRenderer.register(ItemGroupTest.ITEM_GROUP_WITH_TEXTURE_ICON, itemGroup -> ItemGroupIconRenderer.texture(itemGroup, new Identifier("textures/block/stone.png")));
	}
}
