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

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.loader.api.config.QuiltConfig;

@ApiStatus.Internal
public class RegistryConfig extends WrappedConfig {
	public static final RegistryConfig INSTANCE = QuiltConfig.create("quilt/qsl", "registry", RegistryConfig.class);

	public final RegistrySync registry_sync = new RegistrySync();

	public static class RegistrySync implements Section {
		@Comment("Message displayed for players joining with incompatible clients. Support strings and Minecraft's json text format")
		public final String missing_registry_sync_message = "Unsupported (vanilla?) client!\nThis server requires modded client to join!";
		@Comment("Allows players with Fabric API to connect, as long as they have all required mods")
		public final boolean support_fabric_api_protocol = true;
	}
}
