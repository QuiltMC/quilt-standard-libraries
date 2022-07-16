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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.rendering.impl.client.VanillaCooldownOverlayProvider;

@Environment(EnvType.CLIENT)
public interface CooldownOverlayProvider {
	CooldownOverlayProvider VANILLA = VanillaCooldownOverlayProvider.INSTANCE;

	boolean isCooldownOverlayVisible(ItemStack stack);

	/**
	 * {@return the height of the cooldown overlay in pixels (out of 16)}
	 */
	int getCooldownOverlayStep(ItemStack stack);
	int getCooldownOverlayColor(ItemStack stack);
}
