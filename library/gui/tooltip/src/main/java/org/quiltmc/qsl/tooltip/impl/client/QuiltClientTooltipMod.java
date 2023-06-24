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

package org.quiltmc.qsl.tooltip.impl.client;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData;
import org.quiltmc.qsl.tooltip.api.client.TooltipComponentCallback;

@ListenerPhase(
		callbackTarget = TooltipComponentCallback.class,
		namespace = QuiltClientTooltipMod.NAMESPACE, path = QuiltClientTooltipMod.CONVERTIBLE_TOOLTIP_DATA_PHASE
)
@ApiStatus.Internal
@ClientOnly
public final class QuiltClientTooltipMod implements ClientModInitializer, TooltipComponentCallback {
	public static final String NAMESPACE = "quilt_tooltip";
	public static final String CONVERTIBLE_TOOLTIP_DATA_PHASE = "convertible_tooltip_data";
	public static final QuiltClientTooltipMod INSTANCE = new QuiltClientTooltipMod();

	@Override
	public void onInitializeClient(ModContainer mod) {
		// We make the convertible tooltip data run after the usual tooltip data conversion listeners to allow overriding through events.
		// (Whoever returns a tooltip component first wins.)
		TooltipComponentCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, ConvertibleTooltipData.EVENT_PHASE);
	}

	@Override
	public @Nullable TooltipComponent getComponent(TooltipData data) {
		if (data instanceof ConvertibleTooltipData convertible) {
			return convertible.toComponent();
		}

		return null;
	}
}
