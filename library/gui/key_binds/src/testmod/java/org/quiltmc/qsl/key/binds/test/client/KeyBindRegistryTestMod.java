/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.key.binds.test.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;

import org.quiltmc.qsl.key.binds.api.KeyBindRegistry;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class KeyBindRegistryTestMod implements ClientLifecycleEvents.Ready {
	public static final Logger LOGGER = LoggerFactory.getLogger("KeyBindRegistryTest");

	@Override
	public void readyClient(MinecraftClient client) {
		KeyBind enableKeyBindKey = KeyBindRegistry.getKeyBind("key.qsl.enable_key_bind");

		if (enableKeyBindKey != null) {
			LOGGER.info("Successfully got the \"Enable Key Bind\" key!");
			ClientTickEvents.END.register(tickClient -> {
				if (enableKeyBindKey.wasPressed()) {
					LOGGER.info("I can add behavior to other keys!");
				}
			});
		}

		LOGGER.info("The registry has the following keys registered:");
		for (KeyBind key : KeyBindRegistry.getAllKeyBinds()) {
			LOGGER.info(String.format("Found key %s! (Vanilla: %s)", key.getTranslationKey(), key.isVanilla()));
		}
	}
}
