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

package org.quiltmc.qsl.registry.attachment.impl;

import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.impl.reloader.RegistryEntryAttachmentReloader;

@ApiStatus.Internal
public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		AssetsHolderGuard.setAccessAllowed();
		RegistryEntryAttachmentReloader.register(ResourceType.CLIENT_RESOURCES);

		ClientPlayNetworking.registerGlobalReceiver(RegistryEntryAttachmentSync.PACKET_ID, ClientInitializer::handleAttachmentSyncPacket);
	}

	@SuppressWarnings("unchecked")
	private static void handleAttachmentSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		var registryId = buf.readIdentifier();
		var attachmentId = buf.readIdentifier();
		var namespace = buf.readString();
		var valueMap = buf.readNbt();
		client.execute(() -> {
			var registry = (Registry<Object>) Registry.REGISTRIES.get(registryId);
			if (registry == null) {
				throw new IllegalStateException("Unknown registry %s".formatted(registryId));
			}
			var attachment = (RegistryEntryAttachment<Object, Object>) RegistryEntryAttachmentHolder.getAttachment(registry, attachmentId);
			if (attachment == null) {
				throw new IllegalStateException("Unknown attachment %s for registry %s".formatted(attachmentId, registryId));
			}
			var holder = RegistryEntryAttachmentHolder.getData(registry);
			holder.valueTable.row(attachment).clear();
			for (var entryKey : valueMap.getKeys()) {
				var entryId = new Identifier(namespace, entryKey);
				var registryObject = registry.get(entryId);
				if (registryObject == null) {
					throw new IllegalStateException("Foreign ID %s".formatted(entryId));
				}
				var parsedValue = attachment.codec()
						.parse(NbtOps.INSTANCE, valueMap.get(entryKey))
						.getOrThrow(false, msg -> {
							throw new IllegalStateException("Failed to decode value for attachment %s of registry entry %s: %s"
									.formatted(attachment.id(), entryId, msg));
						});
				holder.putValue(attachment, registryObject, parsedValue);
			}
		});
		// TODO send "OK" response packet?
	}
}
