/*
 * Copyright 2016, 2017 QuiltMC
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
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.quiltmc.qsl.networking.api.client.ClientLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.client.C2SPlayChannelEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

public final class NetworkingChannelClientTest implements ClientModInitializer {
//	public static final KeyBinding OPEN = KeyBindingHelper.registerKeyBinding(new KeyBinding("networking-v1-test", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MENU, "fabric-networking-api-v1-testmod\""));
	static final Set<Identifier> SUPPORTED_C2S_CHANNELS = new HashSet<>();

	@Override
	public void onInitializeClient() {
		// TODO: Pending other APIs and testmod dependencies getting setup.
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			if (client.player != null) {
//				if (OPEN.wasPressed()) {
//					client.setScreen(new ChannelScreen(this));
//				}
//			}
//		});

		C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.addAll(channels);

			if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen) {
				((ChannelScreen) MinecraftClient.getInstance().currentScreen).refresh();
			}
		});

		C2SPlayChannelEvents.UNREGISTER.register((handler, sender, client, channels) -> {
			SUPPORTED_C2S_CHANNELS.removeAll(channels);

			if (MinecraftClient.getInstance().currentScreen instanceof ChannelScreen) {
				((ChannelScreen) MinecraftClient.getInstance().currentScreen).refresh();
			}
		});

		// State destruction on disconnection:
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
			SUPPORTED_C2S_CHANNELS.clear();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			SUPPORTED_C2S_CHANNELS.clear();
		});
	}
}
