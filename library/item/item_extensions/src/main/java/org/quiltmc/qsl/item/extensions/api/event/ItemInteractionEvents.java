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

package org.quiltmc.qsl.item.extensions.api.event;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

public final class ItemInteractionEvents {
	private ItemInteractionEvents() {
		throw new UnsupportedOperationException("BlockInteractionEvents only contains static declarations.");
	}

	public static final Event<IgniteBlock> IGNITE_BLOCK = Event.create(IgniteBlock.class, callbacks -> context -> {
		var result = ActionResult.PASS;
		for (var callback : callbacks) {
			result = callback.onIgniteBlock(context);
			if (result != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	@FunctionalInterface
	public interface IgniteBlock extends EventAwareListener {
		@NotNull ActionResult onIgniteBlock(@NotNull ItemUsageContext context);
	}
}
