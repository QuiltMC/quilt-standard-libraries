/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.key.bindings.test.client;

import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;

import org.quiltmc.qsl.key.bindings.api.KeyBindingRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindingsTestMod implements ClientModInitializer {
	// A conflicting key test
	public static final KeyBinding CONFLICT_TEST_KEY_BIND = KeyBindingRegistry.registerKeyBinding(
		new KeyBinding("key.qsl.conflict_test", GLFW.GLFW_KEY_H, "key.qsl.category")
	);

	public static final KeyBinding DISABLE_KEY_BIND = KeyBindingRegistry.registerKeyBinding(
		new KeyBinding("key.qsl.disable_key_bind", GLFW.GLFW_KEY_H, "key.qsl.category"), true
	);

	public static final KeyBinding ENABLE_KEY_BIND = KeyBindingRegistry.registerKeyBinding(
		new KeyBinding("key.qsl.enable_key_bind", GLFW.GLFW_KEY_I, "key.qsl.category"), true
	);

	public static final KeyBinding DISABLED_CONFLICT_TEST_KEY_BIND = KeyBindingRegistry.registerKeyBinding(
		new KeyBinding("key.qsl.disabled_conflict_test", GLFW.GLFW_KEY_H, "key.qsl.category"), false
	);

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START.register(client -> {
			if (DISABLE_KEY_BIND.isPressed()) {
				if (client.player != null) {
					client.player.sendMessage(new LiteralText("The key has disappeared! Bye bye, key!"), true);
				}

				KeyBindingRegistry.setEnabled(DISABLE_KEY_BIND, false);
			}

			if (ENABLE_KEY_BIND.isPressed()) {
				if (client.player != null) {
					client.player.sendMessage(new LiteralText("The key is back!"), true);
				}

				KeyBindingRegistry.setEnabled(DISABLE_KEY_BIND, true);
			}

			if (CONFLICT_TEST_KEY_BIND.isPressed()) {
				if (client.player != null) {
					client.player.sendMessage(new LiteralText("This is the conflict key being pressed"), false);
				}
			}

			if (DISABLED_CONFLICT_TEST_KEY_BIND.isPressed()) {
				if (client.player != null) {
					client.player.sendMessage(new LiteralText("I'm not supposed to do things! Why am I enabled?"), false);
				}
			}
		});
	}
}
