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

package org.quiltmc.qsl.item.rendering.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.rendering.api.client.ItemBarRenderer;
import org.quiltmc.qsl.item.rendering.api.client.SolidColorItemBarRenderer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class VanillaItemBarRenderer extends SolidColorItemBarRenderer {
	public static final ItemBarRenderer INSTANCE = new VanillaItemBarRenderer();

	private VanillaItemBarRenderer() { }

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return stack.isItemBarVisible();
	}

	@Override
	protected int getItemBarStep(ItemStack stack) {
		return stack.getItemBarStep();
	}

	@Override
	protected int getItemBarForeground(ItemStack stack) {
		return 0xFF000000 | stack.getItemBarColor();
	}
}
