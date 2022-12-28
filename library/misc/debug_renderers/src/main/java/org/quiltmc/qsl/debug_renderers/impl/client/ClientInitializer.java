/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

package org.quiltmc.qsl.debug_renderers.impl.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.debug_renderers.api.client.DebugRendererRegistrationCallback;
import org.quiltmc.qsl.debug_renderers.impl.DebugFeatureSync;
import org.quiltmc.qsl.debug_renderers.impl.Initializer;
import org.slf4j.Logger;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@ApiStatus.Internal
@ClientOnly
public final class ClientInitializer implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitializeClient(ModContainer mod) {
		if (QuiltLoader.isModLoaded("quilt_networking")) {
			DebugFeatureSync.clientInit();
			LOGGER.info("[Quilt Debug Renderers|Client] Networking support is enabled");
		} else {
			LOGGER.info("[Quilt Debug Renderers|Client] Networking support is disabled");
		}

		if (QuiltLoader.isModLoaded("quilt_client_command")) {
			LOGGER.info("[Quilt Debug Renderers|Client] Client Command support is enabled");
			DebugFeatureClientCommands.init();
		} else {
			LOGGER.info("[Quilt Debug Renderers|Client] Client Command support is disabled");
		}
	}
}
