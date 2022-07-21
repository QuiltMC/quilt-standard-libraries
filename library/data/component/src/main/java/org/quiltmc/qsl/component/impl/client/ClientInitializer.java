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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.event.ClientEventListener;
import org.quiltmc.qsl.component.impl.event.ComponentEventPhases;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientSyncHandler.getInstance().registerPackets();

		ClientLifecycleEvents.READY.register(
				ComponentEventPhases.FREEZE_COMPONENT_REGISTRIES,
				ClientEventListener::onClientReady
		);

		ClientPlayConnectionEvents.JOIN.register(
				ComponentEventPhases.UNFREEZE_COMPONENT_NETWORK,
				ClientEventListener::onServerJoin
		);

		ClientPlayConnectionEvents.DISCONNECT.register(
				ComponentEventPhases.FREEZE_COMPONENT_NETWORK,
				ClientEventListener::onServerDisconnect
		);

		ClientTickEvents.END.register(
				ComponentEventPhases.CLIENT_REQUEST_POLL,
				ClientEventListener::onClientTick
		);
	}
}
