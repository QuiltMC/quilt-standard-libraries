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

package org.quiltmc.qsl.item.group.impl;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemGroup;

import org.quiltmc.qsl.item.group.api.client.ItemGroupIconRenderer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ItemGroupIconRendererRegistry {
	private static final Map<ItemGroup, ItemGroupIconRenderer<? extends ItemGroup>> RENDERERS = new Object2ObjectOpenHashMap<>();

	@SuppressWarnings("unchecked")
	public static <IG extends ItemGroup> ItemGroupIconRenderer<IG> register(IG itemGroup, ItemGroupIconRenderer<IG> renderer) {
		return (ItemGroupIconRenderer<IG>) RENDERERS.put(itemGroup, renderer);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <IG extends ItemGroup> ItemGroupIconRenderer<IG> get(IG itemGroup) {
		return (ItemGroupIconRenderer<IG>) RENDERERS.get(itemGroup);
	}
}
