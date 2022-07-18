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

package org.quiltmc.qsl.component.impl.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.SyncChannel;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

public class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientSyncHandler.getInstance().registerPackets();

		ClientLifecycleEvents.READY.register(
				CommonInitializer.id("freeze_component_registies"),
				client -> ComponentsImpl.REGISTRY.freeze()
		);
	}
}
