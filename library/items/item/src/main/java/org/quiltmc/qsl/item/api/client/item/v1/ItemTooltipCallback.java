/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.item.api.client.item.v1;

import java.util.List;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A callback for modifying the lines of text for the rendered tooltip displayed when the mouse hovers over an item stack.
 */
@Environment(EnvType.CLIENT)
public interface ItemTooltipCallback {
	/**
	 * Fired after the game has appended all base tooltip lines to the list.
	 */
	ArrayEvent<ItemTooltipCallback> EVENT = ArrayEvent.create(ItemTooltipCallback.class, callbacks -> (stack, context, lines) -> {
		for (ItemTooltipCallback callback : callbacks) {
			callback.tooltipCallback(stack, context, lines);
		}
	});

	/**
	 * Called when an item stack's tooltip is rendered. Text added to {@code lines} will be
	 * rendered with the tooltip. Values from {@code lines} can also be modified or removed.
	 *
	 * @param stack The {@link ItemStack} to build the tooltip for
	 * @param context The {@link TooltipContext} for the current Tooltip being built
	 * @param lines the list containing the lines of text displayed on the stack's tooltip
	 */
	void tooltipCallback(ItemStack stack, TooltipContext context, List<Text> lines);
}
