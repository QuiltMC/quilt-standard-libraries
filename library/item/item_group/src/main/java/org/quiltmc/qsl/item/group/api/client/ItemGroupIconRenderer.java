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

package org.quiltmc.qsl.item.group.api.client;

import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.item.group.impl.ItemGroupIconRendererRegistry;
import org.quiltmc.qsl.item.group.impl.ItemGroupTextureIconRenderer;

@Environment(EnvType.CLIENT)
public interface ItemGroupIconRenderer<IG extends ItemGroup> {
	static <IG extends ItemGroup> ItemGroupIconRenderer<IG> register(@NotNull IG itemGroup, @NotNull Function<IG, ItemGroupIconRenderer<IG>> renderer) {
		Objects.requireNonNull(itemGroup, "itemGroup may not be null");
		Objects.requireNonNull(renderer, "renderer may not be null");
		return ItemGroupIconRendererRegistry.register(itemGroup, renderer.apply(itemGroup));
	}

	static <IG extends ItemGroup> ItemGroupIconRenderer<IG> texture(@NotNull IG itemGroup, @NotNull Identifier textureId) {
		Objects.requireNonNull(itemGroup, "itemGroup may not be null");
		Objects.requireNonNull(textureId, "textureId may not be null");
		return new ItemGroupTextureIconRenderer<IG>(textureId);
	}

	void render(IG itemGroup, MatrixStack matrices, int x, int y, float tickDelta);
}
