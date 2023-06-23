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

package org.quiltmc.qsl.block.entity.impl.client;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.block.entity.impl.QuiltBlockEntityImpl;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

@ApiStatus.Internal
@ListenerPhase(
		namespace = QuiltBlockEntityImpl.NAMESPACE, path = QuiltBlockEntityImpl.BLOCK_ENTITY_FREEZING_PHASE,
		callbackTarget = ClientLifecycleEvents.Ready.class
)
@ClientOnly
public final class QuiltBlockEntityImplClient implements ClientModInitializer, ClientLifecycleEvents.Ready {
	public static final QuiltBlockEntityImplClient INSTANCE = new QuiltBlockEntityImplClient();

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientLifecycleEvents.READY.addPhaseOrdering(Event.DEFAULT_PHASE, QuiltBlockEntityImpl.BLOCK_ENTITY_FREEZING_PHASE_ID);
	}

	@Override
	public void readyClient(MinecraftClient client) {
		QuiltBlockEntityImpl.INSTANCE.setFrozen(true);
	}
}
