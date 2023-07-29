/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackProvider;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.phase.PhaseData;
import org.quiltmc.qsl.base.api.phase.PhaseSorting;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.api.ResourcePackRegistrationContext;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.mixin.VanillaDataPackProviderAccessor;

/**
 * Represents the implementation of the resource loader.
 */
@ApiStatus.Internal
public final class ResourceLoaderImpl implements ResourceLoader {
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new EnumMap<>(ResourceType.class);
	/**
	 * Represents a cache of the client mod resource packs so resource packs that can cache don't lose their cache.
	 */
	private static final Map<String, List<ModNioResourcePack>> CLIENT_MOD_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	/**
	 * Represents a cache of the server mod data packs so resource packs that can cache don't lose their cache.
	 */
	private static final Map<String, List<ModNioResourcePack>> SERVER_MOD_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	private static final Map<String, ModNioResourcePack> CLIENT_BUILTIN_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	private static final Map<String, ModNioResourcePack> SERVER_BUILTIN_RESOURCE_PACKS = new Object2ObjectOpenHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger("ResourceLoader");

	private static final boolean DEBUG_RELOADERS_IDENTITY = TriState.fromProperty("quilt.resource_loader.debug.reloaders_identity")
			.toBooleanOrElse(QuiltLoader.isDevelopmentEnvironment());
	private static final boolean DEBUG_RELOADERS_ORDER = TriState.fromProperty("quilt.resource_loader.debug.reloaders_order")
			.toBooleanOrElse(false);

	private final ResourceType type;
	private final Set<Identifier> addedReloaderIds = new ObjectOpenHashSet<>();
	private final Set<IdentifiableResourceReloader> addedReloaders = new LinkedHashSet<>();
	private final Set<Pair<Identifier, Identifier>> reloadersOrdering = new LinkedHashSet<>();
	final Set<ResourcePackProvider> resourcePackProfileProviders = new ObjectOpenHashSet<>();

	private final Event<ResourcePackRegistrationContext.Callback> defaultResourcePackRegistrationEvent = createResourcePackRegistrationEvent();
	private final Event<ResourcePackRegistrationContext.Callback> topResourcePackRegistrationEvent = createResourcePackRegistrationEvent();

	private static Event<ResourcePackRegistrationContext.Callback> createResourcePackRegistrationEvent() {
		return Event.create(ResourcePackRegistrationContext.Callback.class, callbacks -> context -> {
			for (var callback : callbacks) {
				callback.onRegisterPack(context);
			}
		});
	}

	public ResourceLoaderImpl(ResourceType type) {
		this.type = type;
	}

	public static ResourceLoaderImpl get(ResourceType type) {
		return IMPL_MAP.computeIfAbsent(type, ResourceLoaderImpl::new);
	}

	public static <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader, ResourcePack pack, InputStream inputStream) {
		JsonObject json;

		try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			json = JsonHelper.deserialize(reader);
		} catch (Exception e) {
			LOGGER.error("Couldn't load {} metadata from pack \"{}\":", metaReader.getKey(), pack.getName(), e);
			return null;
		}

		if (!json.has(metaReader.getKey())) {
			return null;
		} else {
			try {
				return metaReader.fromJson(JsonHelper.getObject(json, metaReader.getKey()));
			} catch (Exception e) {
				LOGGER.error("Couldn't load {} metadata from pack \"{}\":", metaReader.getKey(), pack.getName(), e);
				return null;
			}
		}
	}

	/* Resource reloaders stuff */

	public static void sort(ResourceType type, List<ResourceReloader> reloaders) {
		get(type).sort(reloaders);
	}

	@SuppressWarnings("removal")
	@Override
	public void registerReloader(@NotNull IdentifiableResourceReloader resourceReloader) {
		if (!this.addedReloaderIds.add(resourceReloader.getQuiltId())) {
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

	@Override
	public void addReloaderOrdering(@NotNull Identifier firstReloader, @NotNull Identifier secondReloader) {
		Objects.requireNonNull(firstReloader, "The first reloader identifier should not be null.");
		Objects.requireNonNull(secondReloader, "The second reloader identifier should not be null.");

		if (firstReloader.equals(secondReloader)) {
			throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
		}

		this.reloadersOrdering.add(new Pair<>(firstReloader, secondReloader));
	}

	@Override
	public void registerResourcePackProfileProvider(@NotNull ResourcePackProvider provider) {
		if (!this.resourcePackProfileProviders.add(provider)) {
			throw new IllegalStateException(
					"Tried to register a resource pack profile provider twice!"
			);
		}
	}

	@Override
	public @NotNull Event<ResourcePackRegistrationContext.Callback> getRegisterDefaultResourcePackEvent() {
		return this.defaultResourcePackRegistrationEvent;
	}

	@Override
	public @NotNull Event<ResourcePackRegistrationContext.Callback> getRegisterTopResourcePackEvent() {
		return this.topResourcePackRegistrationEvent;
	}

	@Override
	public @NotNull ResourcePack newFileSystemResourcePack(@NotNull Identifier id, @NotNull ModContainer owner, @NotNull Path rootPath,
			ResourcePackActivationType activationType, @NotNull Text displayName) {
		String name = id.getNamespace() + '/' + id.getPath();
		return new ModNioResourcePack(name, owner.metadata(), displayName, activationType, rootPath, this.type, null);
	}

	/**
	 * Flattens the given resource pack if it's a group resource pack.
	 * <p>
	 * This is useful to flatten the resource pack list once the runtime list is figured out.
	 *
	 * @param pack     the given resource pack
	 * @param consumer the resource pack consumer
	 */
	public static void flattenPacks(ResourcePack pack, Consumer<ResourcePack> consumer) {
		if (pack instanceof GroupResourcePack grouped) {
			grouped.streamPacks().forEach(p -> flattenPacks(p, consumer));
		} else {
			consumer.accept(pack);
		}
	}

	public void appendTopPacks(MultiPackResourceManager resourceManager, Consumer<ResourcePack> resourcePackAdder) {
		this.topResourcePackRegistrationEvent.invoker().onRegisterPack(new ResourcePackRegistrationContextImpl(resourceManager, resourcePackAdder));
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
		// - We *do not* touch the ordering of vanilla reloaders. Ever.
		//   While dependency values are provided where possible, we cannot
		//   trust them 100%. Only code doesn't lie.
		// - We add all custom reloaders after vanilla reloaders if they don't have contrary ordering. Same reasons.

		var runtimePhases = new Object2ObjectOpenHashMap<Identifier, ResourceReloaderPhaseData>();

		Iterator<ResourceReloader> itPhases = reloaders.iterator();
		// Add the virtual before Vanilla phase.
		ResourceReloaderPhaseData last = new ResourceReloaderPhaseData(ResourceReloaderKeys.BEFORE_VANILLA, null);
		last.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
		runtimePhases.put(last.getId(), last);

		// Add all the Vanilla reloaders.
		while (itPhases.hasNext()) {
			var currentReloader = itPhases.next();
			Identifier id;

			if (currentReloader instanceof IdentifiableResourceReloader identifiable) {
				id = identifiable.getQuiltId();
			} else {
				id = new Identifier("unknown",
						"private/"
								+ currentReloader.getClass().getName()
								.replace(".", "/")
								.replace("$", "_")
								.toLowerCase(Locale.ROOT)
				);

				if (DEBUG_RELOADERS_IDENTITY) {
					LOGGER.warn("The resource reloader at {} does not implement IdentifiableResourceReloader " +
							"making ordering support more difficult for other modders.", currentReloader.getClass().getName());
				}
			}

			var current = new ResourceReloaderPhaseData(id, currentReloader);
			current.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
			runtimePhases.put(id, current);

			PhaseData.link(last, current);
			last = current;
		}

		// Add the virtual after Vanilla phase.
		var afterVanilla = new ResourceReloaderPhaseData.AfterVanilla(ResourceReloaderKeys.AFTER_VANILLA);
		runtimePhases.put(afterVanilla.getId(), afterVanilla);
		PhaseData.link(last, afterVanilla);

		// Add the modded reloaders.
		for (var moddedReloader : this.addedReloaders) {
			var phase = new ResourceReloaderPhaseData(moddedReloader.getQuiltId(), moddedReloader);
			runtimePhases.put(phase.getId(), phase);
		}

		// Add the ordering.
		for (var order : this.reloadersOrdering) {
			var first = runtimePhases.get(order.getLeft());

			if (first == null) continue;

			var second = runtimePhases.get(order.getRight());

			if (second == null) continue;

			PhaseData.link(first, second);
		}

		// Attempt to order un-ordered modded reloaders to after Vanilla to respect the rules.
		for (var putAfter : runtimePhases.values()) {
			if (putAfter == afterVanilla) continue;

			if (putAfter.vanillaStatus == ResourceReloaderPhaseData.VanillaStatus.NONE
					|| putAfter.vanillaStatus == ResourceReloaderPhaseData.VanillaStatus.AFTER) {
				PhaseData.link(afterVanilla, putAfter);
			}
		}

		// Sort the phases.
		var phases = new ArrayList<>(runtimePhases.values());
		PhaseSorting.sortPhases(phases);

		// Apply the sorting!
		reloaders.clear();

		for (var phase : phases) {
			if (phase.getData() != null) {
				reloaders.add(phase.getData());
			}
		}

		if (DEBUG_RELOADERS_ORDER) {
			LOGGER.info("Sorted reloaders: " + phases.stream().map(data -> {
				String str = data.getId().toString();

				if (data.getData() == null) {
					str += " (virtual)";
				}

				return str;
			}).collect(Collectors.joining(", ")));
		}
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
		var modResourcePacks = type == ResourceType.CLIENT_RESOURCES
				? CLIENT_MOD_RESOURCE_PACKS : SERVER_MOD_RESOURCE_PACKS;
		var existingList = modResourcePacks.get(subPath);
		var byMod = new Reference2ObjectOpenHashMap<ModMetadata, ModNioResourcePack>();

		if (existingList != null) {
			for (var pack : existingList) {
				byMod.put(pack.modInfo, pack);
			}
		}

		for (var container : QuiltLoader.getAllMods()) {
			if (container.getSourceType() == ModContainer.BasicSourceType.BUILTIN) {
				continue;
			}

			if (byMod.containsKey(container.metadata())) {
				continue;
			}

			Path path = container.rootPath();

			if (subPath != null) {
				Path childPath = container.getPath(subPath).toAbsolutePath().normalize();

				if (!childPath.startsWith(path) || !Files.exists(childPath)) {
					continue;
				}

				path = childPath;
			}

			byMod.put(container.metadata(), ModNioResourcePack.ofMod(container.metadata(), path, type));
		}

		List<ModNioResourcePack> packList = byMod.values().stream()
				.filter(pack -> !pack.getNamespaces(type).isEmpty())
				.toList();

		// Cache the pack list for the next reload.
		modResourcePacks.put(subPath, packList);

		packs.addAll(packList);
	}

	public static GroupResourcePack.Wrapped buildMinecraftResourcePack(ResourceType type, ResourcePack vanillaPack) {
		// Build a list of mod resource packs.
		var packs = new ArrayList<ResourcePack>();
		appendModResourcePacks(packs, type, null);

		var pack = new GroupResourcePack.Wrapped(type, vanillaPack, packs, false);
		int[] lastExtraPackIndex = new int[] {1};

		var context = new ResourcePackRegistrationContextImpl(type, List.of(pack), p -> {
			packs.add(lastExtraPackIndex[0]++, p);
			pack.recompute();
		});

		get(type).getRegisterDefaultResourcePackEvent().invoker().onRegisterPack(context);

		return pack;
	}

	public static GroupResourcePack.Wrapped buildVanillaBuiltinResourcePack(ResourcePack vanillaPack, ResourceType type, String packName) {
		// Build a list of mod resource packs.
		var packs = new ArrayList<ResourcePack>();
		appendModResourcePacks(packs, type, packName);

		return new GroupResourcePack.Wrapped(type, vanillaPack, packs, false);
	}

	/* Built-in resource packs */

	public static Text getBuiltinPackDisplayNameFromId(Identifier id) {
		return Text.of(id.getNamespace() + "/" + id.getPath());
	}

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @param displayName    the display name of the resource pack
	 * @return {@code true} if successfully registered the resource pack, or {@code false} otherwise
	 * @see ResourceLoader#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType, Text)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container,
			ResourcePackActivationType activationType, Text displayName) {
		Path resourcePackPath = container.getPath(subPath).toAbsolutePath().normalize();

		if (!Files.exists(resourcePackPath)) {
			return false;
		}

		var name = id.getNamespace() + "/" + id.getPath();

		boolean result = false;
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			result = registerBuiltinResourcePack(ResourceType.CLIENT_RESOURCES,
					newBuiltinResourcePack(container, name, displayName, resourcePackPath, ResourceType.CLIENT_RESOURCES, activationType)
			);
		}

		result |= registerBuiltinResourcePack(ResourceType.SERVER_DATA,
				newBuiltinResourcePack(container, name, displayName, resourcePackPath, ResourceType.SERVER_DATA, activationType)
		);

		return result;
	}

	private static boolean registerBuiltinResourcePack(ResourceType type, ModNioResourcePack pack) {
		if (QuiltLoader.isDevelopmentEnvironment() || !pack.getNamespaces(type).isEmpty()) {
			var builtinResourcePacks = type == ResourceType.CLIENT_RESOURCES
					? CLIENT_BUILTIN_RESOURCE_PACKS : SERVER_BUILTIN_RESOURCE_PACKS;
			builtinResourcePacks.put(pack.getName(), pack);
			return true;
		}
		return false;
	}

	private static ModNioResourcePack newBuiltinResourcePack(ModContainer container, String name, Text displayName,
			Path resourcePackPath, ResourceType type, ResourcePackActivationType activationType) {
		return new ModNioResourcePack(name, container.metadata(), displayName, activationType, resourcePackPath, type, null);
	}

	public static void registerBuiltinResourcePacks(ResourceType type, Consumer<ResourcePackProfile> profileAdder) {
		var builtinPacks = type == ResourceType.CLIENT_RESOURCES
				? CLIENT_BUILTIN_RESOURCE_PACKS : SERVER_BUILTIN_RESOURCE_PACKS;

		// Loop through each registered built-in resource packs and add them if valid.
		for (var entry : builtinPacks.entrySet()) {
			ModNioResourcePack pack = entry.getValue();

			// Add the built-in pack only if namespaces for the specified resource type are present.
			if (!pack.getNamespaces(type).isEmpty()) {
				// Make the resource pack profile for built-in pack, should never be always enabled.
				var profile = QuiltBuiltinResourcePackProfile.of(pack);

				if (profile != null) {
					profileAdder.accept(profile);
				}
			}
		}
	}

	/* Language stuff */

	/**
	 * Appends to the given map all the default language entries.
	 *
	 * @param map the language map
	 */
	public static void appendLanguageEntries(@NotNull Map<String, String> map) {
		var pack = ResourceLoaderImpl.buildMinecraftResourcePack(ResourceType.CLIENT_RESOURCES,
				VanillaDataPackProviderAccessor.invokeCreateVanillaResourcePack()
		);

		try (var manager = new MultiPackResourceManager(ResourceType.CLIENT_RESOURCES, List.of(pack))) {
			for (var namespace : manager.getAllNamespaces()) {
				var langId = new Identifier(namespace, "lang/" + Language.DEFAULT_LANGUAGE + ".json");

				for (var resource : manager.getAllResources(langId)) {
					try (var stream = resource.open()) {
						Language.load(stream, map::put);
					} catch (IOException e) {
						LOGGER.error("Couldn't load language file {} from pack {}.", langId, resource.getSourceName());
					}
				}
			}
		}
	}
}
