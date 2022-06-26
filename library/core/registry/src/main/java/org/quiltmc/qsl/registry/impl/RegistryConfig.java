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

package org.quiltmc.qsl.registry.impl;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.QuiltConfig;

public class RegistryConfig {
	public static final String REGISTRY_SYNC = "registry_sync";
	public static final String NO_REGISTRY_SYNC_MESSAGE = "no_registry_sync_message";
	public static final String SUPPORT_FABRIC_API_SYNC = "support_fabric_api_sync";
	private static Config config = null;
	public static Config getConfig() {
		if (config == null) {
			config = QuiltConfig.create("quilt", "registry", (builder) -> {
				builder.section("registry_sync", s -> {
					s.field(TrackedValue.create("Unsupported (vanilla?) client!\n This server requires modded client to join!", NO_REGISTRY_SYNC_MESSAGE));
					s.field(TrackedValue.create(true, SUPPORT_FABRIC_API_SYNC));
				});
			});
		}
		return config;
	}
}
