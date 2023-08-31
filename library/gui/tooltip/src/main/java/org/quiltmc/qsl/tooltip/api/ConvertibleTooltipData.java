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

package org.quiltmc.qsl.tooltip.api;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tooltip.impl.client.QuiltClientTooltipMod;

/**
 * Represents a {@link TooltipData} which is convertible to a {@link TooltipComponent} on the client.
 * <p>
 * When converting a {@link ConvertibleTooltipData} to a {@link TooltipComponent},
 * it will use the {@link org.quiltmc.qsl.tooltip.api.client.TooltipComponentCallback} event with the phase {@link #EVENT_PHASE}.
 * <p>
 * <b>WARNING:</b> when implementing this interface,
 * make sure to annotate the implemented method {@link #toComponent()} with {@code @ClientOnly}.
 */
public interface ConvertibleTooltipData extends TooltipData {
	/**
	 * Represents the event phase {@code quilt_tooltip:convertible_tooltip_data}
	 * which is used when converting this tooltip data to a {@link TooltipComponent}.
	 * It runs after the {@link org.quiltmc.qsl.base.api.event.Event#DEFAULT_PHASE}
	 * in {@link org.quiltmc.qsl.tooltip.api.client.TooltipComponentCallback}, allowing listeners of the default phase of the event to
	 * override behavior from this interface if needed.
	 */
	Identifier EVENT_PHASE = new Identifier(QuiltClientTooltipMod.NAMESPACE, QuiltClientTooltipMod.CONVERTIBLE_TOOLTIP_DATA_PHASE);

	/**
	 * {@return the associated component}
	 * <p>
	 * <b>WARNING:</b> when implementing this interface,
	 * make sure to annotate the implemented method with {@code @ClientOnly}.
	 */
	@ClientOnly
	TooltipComponent toComponent();
}
