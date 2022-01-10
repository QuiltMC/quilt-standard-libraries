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

package org.quiltmc.qsl.tag.impl;

import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.Main;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;
import org.quiltmc.qsl.tag.mixin.DynamicRegistryManagerAccessor;
import org.quiltmc.qsl.tag.mixin.RequiredTagListRegistryAccessor;

@SuppressWarnings("ClassCanBeRecord")
@ApiStatus.Internal
public final class TagRegistryImpl<T> implements TagRegistry<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadLocal<Boolean> MISSING_TAGS_CLIENT_FETCH = new ThreadLocal<>();

	private final RequiredTagList<T> tagList;
	private final Supplier<TagGroup<T>> tagGroupSupplier;

	private TagRegistryImpl(RequiredTagList<T> tagList, Supplier<TagGroup<T>> tagGroupSupplier) {
		this.tagList = tagList;
		this.tagGroupSupplier = tagGroupSupplier;
	}

	@SuppressWarnings("unchecked")
	public static <T> TagRegistry<T> of(Supplier<TagGroup<T>> tagGroupSupplier) {
		var tagGroup = tagGroupSupplier.get();
		RequiredTagList<T> tagList = null;

		for (var requiredTagList : RequiredTagListRegistryAccessor.getAll()) {
			if (requiredTagList.getGroup() == tagGroup) {
				tagList = (RequiredTagList<T>) requiredTagList;
				break;
			}
		}

		if (tagList == null) {
			throw new IllegalStateException("Could not find the associated RequiredTagList for " + tagGroup + ".");
		}

		return new TagRegistryImpl<>(tagList, tagGroupSupplier);
	}

	@SuppressWarnings("unchecked")
	public static <T> TagRegistry<T> of(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		RequiredTagList<T> tagList = null;

		// Use already registered tag list for the registry if it has the same data type, in case multiple mods tried to do it.
		for (var value : RequiredTagListRegistryAccessor.getAll()) {
			if (value.getRegistryKey() == registryKey) {
				tagList = (RequiredTagList<T>) value;
				// Throw an exception if the tag list has a different data type.
				Preconditions.checkArgument(tagList.getDataType().equals(dataType),
						"Tag list for registry %s already exists with data type %s.",
						registryKey.getValue(), tagList.getDataType()
				);
				break;
			}
		}

		if (tagList == null) {
			tagList = RequiredTagListRegistry.register(registryKey, dataType);
		}

		return new TagRegistryImpl<>(tagList, tagList::getGroup);
	}

	private RegistryKey<? extends Registry<T>> getRegistryKey() {
		return this.tagList.getRegistryKey();
	}

	@Override
	public Tag.Identified<T> create(Identifier id, TagType type) {
		return switch (type) {
			case SERVER_REQUIRED, REQUIRED -> //noinspection unchecked
					((QuiltRequiredTagListHooks<T>) this.tagList).quilt$addTypedTag(id, type);
			case CLIENT_FALLBACK, CLIENT_ONLY -> ClientTagRegistryManager.create(id, type, this.getRegistryKey(), this.tagGroupSupplier);
			case OPTIONAL -> new TagDelegate<>(id, type, this.tagGroupSupplier);
		};
	}

	/**
	 * Manually load tags for dynamic registries and add the resulting tag group to the tag list.
	 * <p>
	 * Minecraft loads the resource manager before dynamic registries, making tags for them fail to load
	 * if it mentions data pack entries. The solution is to manually load tags after the registry is loaded.
	 * <p>
	 * Look at server's {@link Main#main} function calls for {@link ServerResourceManager#reload} and
	 * {@link RegistryOps#ofLoaded} for the relevant code.
	 */
	public static void loadDynamicRegistryTags(DynamicRegistryManager registryManager, ResourceManager resourceManager) {
		var stopwatch = Stopwatch.createStarted();
		int loadedTags = 0;

		for (var tagList : RequiredTagListRegistryAccessor.getAll()) {
			if (isRegistryDynamic(tagList.getRegistryKey())) {
				var registryKey = tagList.getRegistryKey();
				Registry<?> registry = registryManager.get(registryKey);

				var tagGroupLoader = new TagGroupLoader<>(registry::getOrEmpty, tagList.getDataType());
				TagGroup<?> tagGroup = tagGroupLoader.load(resourceManager);
				((QuiltTagManagerHooks) ServerTagManagerHolder.getTagManager()).quilt$putTagGroup(registryKey, tagGroup);
				tagList.updateTagManager(ServerTagManagerHolder.getTagManager());
				loadedTags += tagGroup.getTags().size();
			}
		}

		if (loadedTags > 0) {
			LOGGER.info("Loaded {} dynamic registry tags in {}.", loadedTags, stopwatch);
		}
	}

	/**
	 * Returns whether the given registry key is the key of a dynamic registry.
	 *
	 * @param registryKey the key of the registry
	 * @return {@code true} if the registry is dynamic, otherwise {@code false}
	 */
	public static boolean isRegistryDynamic(RegistryKey<? extends Registry<?>> registryKey) {
		return DynamicRegistryManagerAccessor.getInfos().containsKey(registryKey);
	}

	public static void forceInit() {
		// noop. this only forces to run the static initializer.
	}

	@Environment(EnvType.CLIENT)
	public static void startClientMissingTagsFetching() {
		MISSING_TAGS_CLIENT_FETCH.set(true);
	}

	@Environment(EnvType.CLIENT)
	public static void endClientMissingTagsFetching() {
		MISSING_TAGS_CLIENT_FETCH.remove();
	}

	public static boolean isClientFetchingMissingTags() {
		var value = MISSING_TAGS_CLIENT_FETCH.get();
		if (value == null) {
			return false;
		} else {
			return value;
		}
	}
}
