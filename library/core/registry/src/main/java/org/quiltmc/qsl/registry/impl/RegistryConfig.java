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

package org.quiltmc.qsl.registry.impl;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.text.Text;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.loader.api.config.QuiltConfig;

@ApiStatus.Internal
public class RegistryConfig extends WrappedConfig {
	public static final RegistryConfig INSTANCE = QuiltConfig.create("quilt/qsl", "registry", RegistryConfig.class);

	public final RegistrySync registry_sync = new RegistrySync();

	// This is just workaround for direct values from INSTANCE not being correct.
	@Deprecated
	public static Object getSync(String key) {
		return RegistryConfig.INSTANCE.getValue(List.of("registry_sync", key)).value();
	}

	public static class RegistrySync implements Section {
		@Comment("Mod protocol is a feature allowing you to prevent clients with mismatched settings to join.")
		@Comment("Client with mismatched values won't be able to connect to servers having this enabled.")
		@Comment("It should be used only for non-vanilla compatible modpacks!")
		@Comment("Protocol version. Needs to be the same on client and server. If it has value of -1, it won't be required by servers.")
		public final int mod_protocol_version = -1;
		@Comment("Protocol id. It should be different for every modpack, to prevent joining with mismatched mods.")
		public final String mod_protocol_id = "my_quilt_modpack";
		@Comment("A mod protocol name. Used for easier identification. Doesn't effect functionality")
		public final String mod_protocol_name = "My Quilt Modpack";


		@Comment("Message displayed for players joining with clients incompatible with Registry Sync. Supports strings and Minecraft's JSON text format.")
		public final String missing_registry_sync_message = Text.Serializer.toJson(Text.translatableWithFallback("qsl.registry_sync.unsupported_client", """
				Unsupported (vanilla?) client!
				This server requires modded client to join!"""));

		@Comment("Top part of the message displayed for players joining with incompatible clients. Supports strings and Minecraft's JSON text format.")
		public final String mismatched_entries_top_message = Text.Serializer.toJson(Text.translatableWithFallback("qsl.registry_sync.failed_sync", """
				Failed to synchronize client with the server!
				This can happen when client's and server's mods don't match.
				"""));

		@Comment("Bottom part of the message displayed for players joining with incompatible clients. Supports strings and Minecraft's JSON text format.")
		public final String mismatched_entries_bottom_message = "";

		@Comment("Shows some details about why client couldn't connect.")
		public final boolean mismatched_entries_show_details = true;

		@Comment("Allows players with Fabric API to connect, as long as they have all required mods.")
		public final boolean support_fabric_api_protocol = true;

		@Comment("Forces unknown clients to use the Fabric Registry Sync protocol fallback. Disables preventing Vanilla clients from joining.")
		public final boolean force_fabric_api_protocol_fallback = false;

		@Comment("Disables the Mod Protocol sync on server list/initial query.")
		public final boolean disable_mod_protocol_ping = false;

		@Comment("Disables the Registry Sync requirement. USE AT YOUR OWN RISK!")
		public final boolean disable_registry_sync = false;

		@Comment("Disables validation of (block/fluid) states. USE AT YOUR OWN RISK!")
		public final boolean disable_state_validation = false;
	}
}
