/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.impl.client;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceType;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.registry.attachment.impl.ClientSideGuard;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentSync;
import org.quiltmc.qsl.registry.attachment.impl.reloader.RegistryEntryAttachmentReloader;

@ApiStatus.Internal
@ClientOnly
public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientSideGuard.setAccessAllowed();
		RegistryEntryAttachmentReloader.register(ResourceType.CLIENT_RESOURCES);
		RegistryEntryAttachmentSync.registerClient();
	}
}
