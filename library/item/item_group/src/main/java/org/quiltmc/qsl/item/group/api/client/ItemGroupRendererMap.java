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

package org.quiltmc.qsl.item.group.api.client;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class ItemGroupRendererMap {
	private ItemGroupRendererMap() {
	}

	private static final Map<ItemGroup, ItemGroupRenderer> rendererMap = new Reference2ReferenceOpenHashMap<>();

	public static void put(ItemGroup group, ItemGroupRenderer renderer) {
		rendererMap.put(group, renderer);
	}

	@Nullable
	public static ItemGroupRenderer get(ItemGroup group) {
		return rendererMap.get(group);
	}
}
