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

package org.quiltmc.qsl.rendering.item.test.client.itembar;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import org.quiltmc.qsl.rendering.item.api.client.ItemBarRenderer;
import org.quiltmc.qsl.rendering.item.api.client.SolidColorItemBarRenderer;

@Environment(EnvType.CLIENT)
public class DiscoItemBarRenderer extends SolidColorItemBarRenderer {
	public static final ItemBarRenderer INSTANCE = new DiscoItemBarRenderer();

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	protected int getItemBarStep(ItemStack stack) {
		return 13;
	}

	@Override
	protected int getItemBarForeground(ItemStack stack) {
		// This doesn't need to be pretty, but it shows that
		//  one can get fancy with item bars by taking
		//  the current time into account when calculating
		//  step or color.
		float c = (Util.getMeasuringTimeMs() % 360) / 360f;
		return 0xFF000000 | MathHelper.hsvToRgb(c, 1.0f, 1.0f);
	}
}
