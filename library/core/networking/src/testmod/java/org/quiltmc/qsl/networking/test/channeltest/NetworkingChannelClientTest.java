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

package org.quiltmc.qsl.networking.test.channeltest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.C2SPlayChannelEvents;
import org.quiltmc.qsl.networking.api.client.ClientLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

public final class NetworkingChannelClientTest implements ClientTickEvents.End,
		C2SPlayChannelEvents.Register, C2SPlayChannelEvents.Unregister,
		ClientLoginConnectionEvents.Disconnect, ClientPlayConnectionEvents.Disconnect {
	// public static final KeyBinding OPEN = KeyBindingHelper.registerKeyBinding(new KeyBinding("networking-v1-test", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MENU, "fabric-networking-api-v1-testmod\""));
	static final Set<Identifier> SUPPORTED_C2S_CHANNELS = new HashSet<>();

	@Override
	public void endClientTick(MinecraftClient client) {
		// TODO: Pending other APIs and testmod dependencies getting setup.
		/*
		if (client.player != null) {
			if (OPEN.wasPressed()) {
				client.setScreen(new ChannelScreen(this));
			}
		}

		*/
	}

	@Override
	public void onChannelRegister(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, List<Identifier> channels) {
		SUPPORTED_C2S_CHANNELS.addAll(channels);

		if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen channelScreen) {
			channelScreen.refresh();
		}
	}

	@Override
	public void onChannelUnregister(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, List<Identifier> channels) {
		SUPPORTED_C2S_CHANNELS.removeAll(channels);

		if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen channelScreen) {
			channelScreen.refresh();
		}
	}

	// State destruction on disconnection:

	@Override
	public void onLoginDisconnect(ClientLoginNetworkHandler handler, MinecraftClient client) {
		SUPPORTED_C2S_CHANNELS.clear();
	}

	@Override
	public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
		SUPPORTED_C2S_CHANNELS.clear();
	}
}
