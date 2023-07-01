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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Allows registering a mapping from {@link TooltipData} to {@link TooltipComponent}.
 * This allows custom tooltips for items: first, override {@link Item#getTooltipData} and return a custom {@link TooltipData}.
 * Second, register a listener to this event and convert the data to your component implementation if it's an instance of your data class.
 * <p>
 * Note that failure to map some data to a component will throw an exception,
 * so make sure that any data you return in {@link Item#getTooltipData} will be handled by one of the callbacks.
 *
 * @see org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData ConvertibleTooltipData: for custom tooltip data implementations, avoid the event for those
 */
@FunctionalInterface
@ClientOnly
public interface TooltipComponentCallback extends ClientEventAwareListener {
	Event<TooltipComponentCallback> EVENT = Event.create(TooltipComponentCallback.class, callbacks -> data -> {
		for (var callback : callbacks) {
			var component = callback.getComponent(data);

			if (component != null) {
				return component;
			}
		}

		return null;
	});

	/**
	 * {@return the tooltip component for the passed data, or {@code null} if none is available}
	 *
	 * @param data the tooltip data for which a tooltip component equivalent is being searched for
	 */
	@Nullable TooltipComponent getComponent(TooltipData data);
}
