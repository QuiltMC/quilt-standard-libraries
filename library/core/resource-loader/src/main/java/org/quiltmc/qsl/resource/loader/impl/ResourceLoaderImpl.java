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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.mixin.NamespaceResourceManagerAccessor;

/**
 * Represents the implementation of the resource loader.
 */
@ApiStatus.Internal
public final class ResourceLoaderImpl implements ResourceLoader {
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Identifier> addedListenerIds = new HashSet<>();
	private final Set<IdentifiableResourceReloader> addedReloaders = new LinkedHashSet<>();

	public static ResourceLoaderImpl get(ResourceType type) {
		return IMPL_MAP.computeIfAbsent(type, t -> new ResourceLoaderImpl());
	}

	/* Resource reloaders stuff */

	public static void sort(ResourceType type, List<ResourceReloader> reloaders) {
		get(type).sort(reloaders);
	}

	@Override
	public void registerReloader(IdentifiableResourceReloader resourceReloader) {
		if (!this.addedListenerIds.add(resourceReloader.getQuiltId())) {
			throw new IllegalStateException(
					"Tried to register resource reloader " + resourceReloader.getQuiltId() + " twice!"
			);
		}

		if (!this.addedReloaders.add(resourceReloader)) {
			throw new IllegalStateException(
					"Resource reloader with previously unknown ID " + resourceReloader.getQuiltId()
							+ " already in resource reloader set!"
			);
		}
	}

	/**
	 * Sorts the given resource reloaders to satisfy dependencies.
	 *
	 * @param reloaders the resource reloaders to sort
	 */
	private void sort(List<ResourceReloader> reloaders) {
		reloaders.removeAll(this.addedReloaders);

		// General rules:
		// - We *do not* touch the ordering of vanilla listeners. Ever.
		//   While dependency values are provided where possible, we cannot
		//   trust them 100%. Only code doesn't lie.
		// - We addReloadListener all custom listeners after vanilla listeners. Same reasons.

		var reloadersToAdd = new ArrayList<>(this.addedReloaders);
		var resolvedIds = new HashSet<Identifier>();

		for (var reloader : reloaders) {
			if (reloader instanceof IdentifiableResourceReloader identifiableResourceReloader) {
				resolvedIds.add(identifiableResourceReloader.getQuiltId());
			}
		}

		int lastSize = -1;

		while (reloaders.size() != lastSize) {
			lastSize = reloaders.size();

			Iterator<IdentifiableResourceReloader> it = reloadersToAdd.iterator();

			while (it.hasNext()) {
				IdentifiableResourceReloader listener = it.next();

				if (resolvedIds.containsAll(listener.getQuiltDependencies())) {
					resolvedIds.add(listener.getQuiltId());
					reloaders.add(listener);
					it.remove();
				}
			}
		}

		for (var reloader : reloadersToAdd) {
			LOGGER.warn("Could not resolve dependencies for resource reloader: " + reloader.getQuiltId() + "!");
		}
	}

	/* Default resource pack stuff */

	private static Path locateDefaultResourcePack(ResourceType type) {
		try {
			// Locate MC jar by finding the URL that contains the assets root.
			URL assetsRootUrl = DefaultResourcePack.class.getResource("/" + type.getDirectory() + "/.mcassetsroot");

			return Paths.get(assetsRootUrl.toURI()).resolve("../..").toAbsolutePath().normalize();
		} catch (Exception exception) {
			throw new RuntimeException("Quilt: Failed to locate Minecraft assets root!", exception);
		}
	}

	public static ModNioResourcePack locateAndLoadDefaultResourcePack(ResourceType type) {
		return new ModNioResourcePack(
				FabricLoader.getInstance().getModContainer("minecraft").map(ModContainer::getMetadata).orElseThrow(),
				locateDefaultResourcePack(type),
				type,
				() -> {
				},
				ResourcePackActivationType.ALWAYS_ENABLED
		);
	}

	/* Mod resource pack stuff */

	/**
	 * Appends mod resource packs to the given list.
	 *
	 * @param packs   the resource pack list to append
	 * @param type    the type of resource
	 * @param subPath the resource pack sub path directory in mods, may be {@code null}
	 */
	public static void appendModResourcePacks(List<ResourcePack> packs, ResourceType type, @Nullable String subPath) {
		for (var container : FabricLoader.getInstance().getAllMods()) {
			if (container.getMetadata().getType().equals("builtin")) {
				continue;
			}

			Path path = container.getRootPath();

			if (subPath != null) {
				Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).toAbsolutePath().normalize();

				if (!childPath.startsWith(path) || !Files.exists(childPath)) {
					continue;
				}

				path = childPath;
			}

			var pack = new ModNioResourcePack(container.getMetadata(), path, type, null, ResourcePackActivationType.ALWAYS_ENABLED);

			if (!pack.getNamespaces(type).isEmpty()) {
				packs.add(pack);
			}
		}
	}

	public static GroupResourcePack.Wrapped buildMinecraftResourcePack(DefaultResourcePack vanillaPack) {
		var type = vanillaPack.getClass().equals(DefaultResourcePack.class)
				? ResourceType.SERVER_DATA : ResourceType.CLIENT_RESOURCES;

		// Build a list of mod resource packs.
		var packs = new ArrayList<ResourcePack>();
		appendModResourcePacks(packs, type, null);

		return new GroupResourcePack.Wrapped(type, vanillaPack, packs, false);
	}

	public static GroupResourcePack.Wrapped buildProgrammerArtResourcePack(AbstractFileResourcePack vanillaPack) {
		// Build a list of mod resource packs.
		var packs = new ArrayList<ResourcePack>();
		appendModResourcePacks(packs, ResourceType.CLIENT_RESOURCES, "programmer_art");

		return new GroupResourcePack.Wrapped(ResourceType.CLIENT_RESOURCES, vanillaPack, packs, true);
	}

	public static void appendResourcesFromGroup(NamespaceResourceManagerAccessor manager, Identifier id,
												GroupResourcePack groupResourcePack, List<Resource> resources)
			throws IOException {
		var packs = groupResourcePack.getPacks(id.getNamespace());

		if (packs == null) {
			return;
		}

		Identifier metadataId = NamespaceResourceManagerAccessor.accessor_getMetadataPath(id);

		for (var pack : packs) {
			if (pack.contains(manager.getType(), id)) {
				InputStream metadataInputStream = pack.contains(manager.getType(), metadataId)
						? manager.accessor_open(metadataId, pack) : null;
				resources.add(new ResourceImpl(pack.getName(), id, manager.accessor_open(id, pack), metadataInputStream));
			}
		}
	}
}
