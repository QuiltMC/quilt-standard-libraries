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

package org.quiltmc.qsl.registry.attachment.impl;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.impl.reloader.RegistryEntryAttachmentReloader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@ApiStatus.Internal
public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "quilt_registry_entry_attachments";

	public static final String ENABLE_DUMP_BUILTIN_COMMAND_PROPERTY =
			"quilt.data.registry_entry_attachments.enable_dump_builtin_command";

	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltRegistryEntryAttachments");

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryEntryAttachmentReloader.register(ResourceType.SERVER_DATA);
		RegistryEntryAttachmentSync.register();

		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, error) -> {
			if (server != null && error != null) {
				RegistryEntryAttachmentSync.clearEncodedValuesCache();
				RegistryEntryAttachmentSync.syncAttachmentsToAllPlayers(server);
			}
		});

		if (Boolean.getBoolean(ENABLE_DUMP_BUILTIN_COMMAND_PROPERTY)) {
			if (QuiltLoader.isModLoaded("quilt_command")) {
				DumpBuiltinAttachmentsCommand.register();
			} else {
				LOGGER.warn("Property \"{}\" was set to true, but required module \"quilt_command\" is missing!",
						ENABLE_DUMP_BUILTIN_COMMAND_PROPERTY);
			}
		}
	}
}
