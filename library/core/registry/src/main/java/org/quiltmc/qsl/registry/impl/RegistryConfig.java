/*
 * Copyright 2022-2023 QuiltMC
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

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.loader.api.config.QuiltConfig;

@ApiStatus.Internal
public class RegistryConfig extends WrappedConfig {
	public static final RegistryConfig INSTANCE = QuiltConfig.create("quilt/qsl", "registry", RegistryConfig.class);

	public final RegistrySync registry_sync = new RegistrySync();

	public static class RegistrySync implements Section {
		@Comment("Message displayed for players joining with clients incompatible with Registry Sync. Supports strings and Minecraft's JSON text format.")
		public final String missing_registry_sync_message = Text.Serializer.toJson(Text.translatable("qsl.registry_sync.unsupported_client", """
				Unsupported (vanilla?) client!
				This server requires modded client to join!"""));

		@Comment("Top part of the message displayed for players joining with incompatible clients. Supports strings and Minecraft's JSON text format.")
		public final String mismatched_entries_top_message = Text.Serializer.toJson(Text.translatable("qsl.registry_sync.failed_sync", """
				Failed to synchronize client with the server!
				This can happen when client's and server's mods don't match.
				"""));

		@Comment("Bottom part of the message displayed for players joining with incompatible clients. Supports strings and Minecraft's JSON text format.")
		public final String mismatched_entries_bottom_message = Text.Serializer.toJson(Text.translatable("qsl.registry_sync.check_logs", """

				Check logs for more details!""").formatted(Formatting.GOLD));

		@Comment("Shows some details about why client couldn't connect.")
		public final boolean mismatched_entries_show_details = true;

		@Comment("Allows players with Fabric API to connect, as long as they have all required mods.")
		public final boolean support_fabric_api_protocol = true;

		@Comment("Forces unknown clients to use the Fabric Registry Sync protocol fallback. Disables preventing Vanilla clients from joining.")
		public final boolean force_fabric_api_protocol_fallback = false;

		@Comment("Disables the Registry Sync requirement. USE AT YOUR OWN RISK!")
		public final boolean disable_registry_sync = false;
	}
}
