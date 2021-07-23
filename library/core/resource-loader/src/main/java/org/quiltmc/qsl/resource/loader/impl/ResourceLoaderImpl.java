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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

/**
 * Represents the implementation of the resource loader.
 */
public final class ResourceLoaderImpl implements ResourceLoader {
	private static final Map<ResourceType, ResourceLoaderImpl> IMPL_MAP = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Identifier> addedListenerIds = new HashSet<>();
	private final Set<IdentifiableResourceReloader> addedReloaders = new LinkedHashSet<>();

	public static ResourceLoaderImpl get(ResourceType type) {
		return IMPL_MAP.computeIfAbsent(type, t -> new ResourceLoaderImpl());
	}

	public static void sort(ResourceType type, List<ResourceReloader> reloaders) {
		get(type).sort(reloaders);
	}

	@Override
	public void registerReloader(IdentifiableResourceReloader resourceReloader) {
		if (!this.addedListenerIds.add(resourceReloader.getQuiltId())) {
			throw new RuntimeException("Tried to register resource reloader " + resourceReloader.getQuiltId() + " twice!");
		}

		if (!this.addedReloaders.add(resourceReloader)) {
			throw new RuntimeException("Resource reloader with previously unknown ID " + resourceReloader.getQuiltId()
					+ " already in resource reloader set!");
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
}
