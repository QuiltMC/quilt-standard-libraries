/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tooltip.api.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

@ClientOnly
public interface ItemTooltipCallback extends ClientEventAwareListener {
	/**
	 * Fired after the game has appended all base tooltip lines to the list.
	 */
	Event<ItemTooltipCallback> EVENT = Event.create(ItemTooltipCallback.class, callbacks -> (stack, player, context, lines) -> {
		for (var callback : callbacks) {
			callback.onTooltipRequest(stack, player, context, lines);
		}
	});

	/**
	 * Called when an item stack's tooltip is rendered.
	 * Text added to {@code lines} will be rendered with the tooltip.
	 *
	 * @param stack   the item for which the tooltip is requested
	 * @param player  the player who requested the tooltip, may be {@code null}
	 * @param context the tooltip context
	 * @param lines   the list containing the lines of text displayed on the stack's tooltip
	 */
	void onTooltipRequest(ItemStack stack, @Nullable PlayerEntity player, TooltipContext context, List<Text> lines);
}
