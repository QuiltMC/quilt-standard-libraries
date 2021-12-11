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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.option.KeyBinding;

import org.quiltmc.qsl.key.bindings.api.KeyBindingRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindingRegistryTestMod implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getFormatterLogger("KeyBindingRegistryTest");

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.READY.register(lifecycleClient -> {
			KeyBinding enableKeyBindKey = KeyBindingRegistry.getKeyBinding("key.qsl.enable_key_bind");

			if (enableKeyBindKey != null) {
				LOGGER.info("Successfully got the \"Enable Key Bind\" key!");

				ClientTickEvents.END.register(tickClient -> {
					if (enableKeyBindKey.wasPressed()) {
						LOGGER.info("I can add behavior to other keys!");
					}
				});
			}

			LOGGER.info("The registry has the following keys registered:");
			KeyBindingRegistry.getAllKeyBindings(true).forEach((key, value) -> {
				LOGGER.info("%s: %s", key.getTranslationKey(), value);
			});
		});
	}
}
