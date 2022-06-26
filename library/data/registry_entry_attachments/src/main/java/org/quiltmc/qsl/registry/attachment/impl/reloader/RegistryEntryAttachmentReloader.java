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

package org.quiltmc.qsl.registry.attachment.impl.reloader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.impl.AssetsHolderGuard;
import org.quiltmc.qsl.registry.attachment.impl.Initializer;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentSync;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;

@ApiStatus.Internal
public final class RegistryEntryAttachmentReloader implements SimpleResourceReloader<RegistryEntryAttachmentReloader.LoadedData> {
	public static void register(ResourceType source) {
		ResourceLoader.get(source).registerReloader(new RegistryEntryAttachmentReloader(source));
	}

	static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier ID_DATA = new Identifier(Initializer.NAMESPACE, "data");
	private static final Identifier ID_ASSETS = new Identifier(Initializer.NAMESPACE, "assets");

	private final ResourceType source;
	private final Identifier id;
	private final Collection<Identifier> deps;

	private RegistryEntryAttachmentReloader(ResourceType source) {
		if (source == ResourceType.CLIENT_RESOURCES) {
			AssetsHolderGuard.assertAccessAllowed();
		}

		this.source = source;
		this.id = switch (source) {
			case SERVER_DATA -> ID_DATA;
			case CLIENT_RESOURCES -> ID_ASSETS;
		};
		this.deps = switch (source) {
			case SERVER_DATA -> Set.of(ResourceReloaderKeys.Server.TAGS);
			case CLIENT_RESOURCES -> Set.of();
		};
	}

	@Override
	public Identifier getQuiltId() {
		return this.id;
	}

	@Override
	public Collection<Identifier> getQuiltDependencies() {
		return this.deps;
	}

	@Override
	public CompletableFuture<LoadedData> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			var attachmentMaps = new HashMap<RegistryEntryAttachment<?, ?>, AttachmentDictionary<?, ?>>();

			for (var entry : Registry.REGISTRIES.getEntries()) {
				Identifier registryId = entry.getKey().getValue();
				String path = registryId.getNamespace() + "/" + registryId.getPath();
				profiler.push(this.id + "/finding_resources/" + path);

				Collection<Identifier> jsonIds = manager.findResources("attachments/" + path, s -> s.endsWith(".json"));
				if (jsonIds.isEmpty()) {
					profiler.pop();
					continue;
				}

				Registry<?> registry = entry.getValue();
				processResources(manager, profiler, attachmentMaps, jsonIds, registry);

				profiler.pop();
			}

			return new LoadedData(attachmentMaps);
		}, executor);
	}

	private void processResources(ResourceManager manager, Profiler profiler,
			Map<RegistryEntryAttachment<?, ?>, AttachmentDictionary<?, ?>> attachmentMaps,
			Collection<Identifier> jsonIds, Registry<?> registry) {
		for (var jsonId : jsonIds) {
			Identifier attachmentId = this.getAttachmentId(jsonId);
			RegistryEntryAttachment<?, ?> attachment = RegistryEntryAttachmentHolder.getAttachment(registry, attachmentId);
			if (attachment == null) {
				LOGGER.warn("Unknown attachment {} (from {})", attachmentId, jsonId);
				continue;
			}

			if (!attachment.side().shouldLoad(this.source)) {
				LOGGER.warn("Ignoring attachment {} (from {}) since it shouldn't be loaded from this source ({}, we're loading from {})",
						attachmentId, jsonId, attachment.side().getSource(), this.source);
				continue;
			}

			profiler.swap(this.id + "/getting_resources{" + jsonId + "}");

			List<Resource> resources;
			try {
				resources = manager.getAllResources(jsonId);
			} catch (IOException e) {
				LOGGER.error("Failed to get all resources for {}", jsonId);
				LOGGER.error("", e);
				continue;
			}

			profiler.swap(this.id + "/processing_resources{" + jsonId + "," + attachmentId + "}");

			AttachmentDictionary<?, ?> attachAttachment = attachmentMaps.computeIfAbsent(attachment, this::createAttachmentMap);
			for (var resource : resources) {
				attachAttachment.processResource(resource);
			}
		}
	}

	private <R, V> AttachmentDictionary<R, V> createAttachmentMap(RegistryEntryAttachment<R, V> attachment) {
		return new AttachmentDictionary<>(attachment.registry(), attachment);
	}

	@Override
	public CompletableFuture<Void> apply(LoadedData data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			data.apply(profiler);
			if (this.source == ResourceType.SERVER_DATA) {
				RegistryEntryAttachmentSync.clearEncodedValuesCache();
				RegistryEntryAttachmentSync.syncAttachmentsToAllPlayers();
			}
		}, executor);
	}

	// "<namespace>:attachments/<path>/<file_name>.json" becomes "<namespace>:<file_name>"
	private Identifier getAttachmentId(Identifier jsonId) {
		String path = jsonId.getPath();
		int lastSlash = path.lastIndexOf('/');
		path = path.substring(lastSlash + 1);

		int lastDot = path.lastIndexOf('.');
		path = path.substring(0, lastDot);
		return new Identifier(jsonId.getNamespace(), path);
	}

	private <R> RegistryEntryAttachmentHolder<R> getHolder(Registry<R> registry) {
		return switch (this.source) {
			case CLIENT_RESOURCES -> RegistryEntryAttachmentHolder.getAssets(registry);
			case SERVER_DATA -> RegistryEntryAttachmentHolder.getData(registry);
		};
	}

	protected final class LoadedData {
		private final Map<RegistryEntryAttachment<?, ?>, AttachmentDictionary<?, ?>> attachmentMaps;

		private LoadedData(Map<RegistryEntryAttachment<?, ?>, AttachmentDictionary<?, ?>> attachmentMaps) {
			this.attachmentMaps = attachmentMaps;
		}

		@SuppressWarnings("unchecked")
		public void apply(Profiler profiler) {
			profiler.push(id + "/clear_attachments");

			for (var entry : Registry.REGISTRIES.getEntries()) {
				getHolder(entry.getValue()).clear();
			}

			for (var entry : this.attachmentMaps.entrySet()) {
				profiler.swap(id + "/apply_attachment{" + entry.getKey().id() + "}");
				applyOne((RegistryEntryAttachment<Object, Object>) entry.getKey(), (AttachmentDictionary<Object, Object>) entry.getValue());
			}

			profiler.pop();
		}

		@SuppressWarnings("unchecked")
		private <R, V> void applyOne(RegistryEntryAttachment<R, V> attachment, AttachmentDictionary<R, V> attachAttachment) {
			var registry = attachment.registry();
			Objects.requireNonNull(registry, "registry");

			RegistryEntryAttachmentHolder<R> holder = getHolder(registry);
			for (Map.Entry<AttachmentDictionary.ValueTarget, Object> attachmentEntry : attachAttachment.getMap().entrySet()) {
				V value = (V) attachmentEntry.getValue();
				AttachmentDictionary.ValueTarget target = attachmentEntry.getKey();
				switch (target.type()) {
					case ENTRY -> holder.putValue(attachment, attachment.registry().get(target.id()), value);
					case TAG -> holder.putValue(attachment, TagKey.of(attachment.registry().getKey(), target.id()), value);
					default -> throw new IllegalStateException("Unexpected value: " + target.type());
				}
			}
		}
	}
}
