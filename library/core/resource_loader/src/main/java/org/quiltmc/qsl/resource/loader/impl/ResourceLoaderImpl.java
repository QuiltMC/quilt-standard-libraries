/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
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
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new EnumMap<>(ResourceType.class);
	private static final Map<String, ModNioResourcePack> CLIENT_BUILTIN_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	private static final Map<String, ModNioResourcePack> SERVER_BUILTIN_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger("ResourceLoader");

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
		// Remove any modded reloaders to sort properly.
		reloaders.removeAll(this.addedReloaders);

		// General rules:
		// - We *do not* touch the ordering of vanilla listeners. Ever.
		//   While dependency values are provided where possible, we cannot
		//   trust them 100%. Only code doesn't lie.
		// - We addReloadListener all custom listeners after vanilla listeners. Same reasons.

		var reloadersToAdd = new ArrayList<>(this.addedReloaders);
		var resolvedIds = new HashSet<Identifier>();

		// Build a list of resolve identifiers from the reloaders that are already registered.
		for (var reloader : reloaders) {
			if (reloader instanceof IdentifiableResourceReloader identifiableResourceReloader) {
				resolvedIds.add(identifiableResourceReloader.getQuiltId());
			}
		}

		int lastSize = -1;

		// Loop as long as the reloader list is changed in the loop.
		while (reloaders.size() != lastSize) {
			lastSize = reloaders.size();

			Iterator<IdentifiableResourceReloader> it = reloadersToAdd.iterator();

			// Loop through all remaining reloaders to add.
			while (it.hasNext()) {
				IdentifiableResourceReloader listener = it.next();

				// If all the dependencies of the reloader are satisfied then
				//  - add the reloader id to the resolved ids.
				//  - add the reloader to the reloader list.
				//  - remove the reloader from the "to add" list.
				if (resolvedIds.containsAll(listener.getQuiltDependencies())) {
					resolvedIds.add(listener.getQuiltId());
					reloaders.add(listener);
					it.remove();
				}
			}
		}

		// Warn about all unsatisfied reloaders.
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
				"Default",
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

			var pack = new ModNioResourcePack(null, container.getMetadata(), path, type, null,
					ResourcePackActivationType.ALWAYS_ENABLED);

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

	/* Built-in resource packs */

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @see ResourceLoader#registerBuiltinResourcePack(Identifier, ResourcePackActivationType)
	 * @see ResourceLoader#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container,
	                                                  ResourcePackActivationType activationType) {
		String separator = container.getRootPath().getFileSystem().getSeparator();
		subPath = subPath.replace("/", separator);

		Path resourcePackPath = container.getRootPath().resolve(subPath).toAbsolutePath().normalize();

		if (!Files.exists(resourcePackPath)) {
			return false;
		}

		var name = id.getNamespace() + "/" + id.getPath();

		boolean result = false;
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			result = registerBuiltinResourcePack(ResourceType.CLIENT_RESOURCES,
					newBuiltinResourcePack(container, name, resourcePackPath, ResourceType.CLIENT_RESOURCES, activationType)
			);
		}

		result |= registerBuiltinResourcePack(ResourceType.SERVER_DATA,
				newBuiltinResourcePack(container, name, resourcePackPath, ResourceType.SERVER_DATA, activationType)
		);

		return result;
	}

	private static boolean registerBuiltinResourcePack(ResourceType type, ModNioResourcePack pack) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment() || !pack.getNamespaces(type).isEmpty()) {
			var builtinResourcePacks = type == ResourceType.CLIENT_RESOURCES
					? CLIENT_BUILTIN_RESOURCE_PACKS : SERVER_BUILTIN_RESOURCE_PACKS;
			builtinResourcePacks.put(pack.getName(), pack);
			return true;
		}
		return false;
	}

	private static ModNioResourcePack newBuiltinResourcePack(ModContainer container, String name, Path resourcePackPath,
	                                                         ResourceType type, ResourcePackActivationType activationType) {
		return new ModNioResourcePack(name, container.getMetadata(), resourcePackPath, type, null, activationType);
	}

	public static void registerBuiltinResourcePacks(ResourceType type, Consumer<ResourcePackProfile> profileAdder,
	                                                ResourcePackProfile.Factory factory) {
		var builtinPacks = type == ResourceType.CLIENT_RESOURCES
				? CLIENT_BUILTIN_RESOURCE_PACKS : SERVER_BUILTIN_RESOURCE_PACKS;

		// Loop through each registered built-in resource packs and add them if valid.
		for (var entry : builtinPacks.entrySet()) {
			ModNioResourcePack pack = entry.getValue();

			// Add the built-in pack only if namespaces for the specified resource type are present.
			if (!pack.getNamespaces(type).isEmpty()) {
				// Make the resource pack profile for built-in pack, should never be always enabled.
				var profile = ResourcePackProfile.of(entry.getKey(),
						pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
						entry::getValue, factory, ResourcePackProfile.InsertionPosition.TOP,
						ModResourcePackProvider.PACK_SOURCE_MOD_BUILTIN);
				if (profile != null) {
					profileAdder.accept(profile);
				}
			}
		}
	}
}
