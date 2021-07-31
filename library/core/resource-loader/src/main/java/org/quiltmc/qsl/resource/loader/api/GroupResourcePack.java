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

package org.quiltmc.qsl.resource.loader.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.resource.ResourceNotFoundException;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Represents a group resource pack, which holds multiple resource packs as one.
 */
public abstract class GroupResourcePack implements ResourcePack {
	protected final ResourceType type;
	protected final List<? extends ResourcePack> packs;
	protected final Map<String, List<ResourcePack>> namespacedPacks = new Object2ObjectOpenHashMap<>();

	public GroupResourcePack(ResourceType type, List<? extends ResourcePack> packs) {
		this.type = type;
		this.packs = packs;
		this.packs.forEach(pack -> pack.getNamespaces(this.type)
				.forEach(namespace -> this.namespacedPacks.computeIfAbsent(namespace, value -> new ArrayList<>())
						.add(pack)));
	}

	public List<? extends ResourcePack> getPacks(String namespace) {
		return this.namespacedPacks.get(namespace);
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		var packs = this.namespacedPacks.get(id.getNamespace());

		if (packs != null) {
			// Iterating backwards as higher-priority packs are placed at the beginning.
			for (int i = packs.size() - 1; i >= 0; i--) {
				ResourcePack pack = packs.get(i);

				if (pack.contains(type, id)) {
					return pack.open(type, id);
				}
			}
		}

		throw new ResourceNotFoundException(null,
				String.format("%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath()));
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
		var packs = this.namespacedPacks.get(namespace);

		if (packs == null) {
			return Collections.emptyList();
		}

		var resources = new HashSet<Identifier>();

		// Iterating backwards as higher-priority packs are placed at the beginning.
		for (int i = packs.size() - 1; i >= 0; i--) {
			ResourcePack pack = packs.get(i);
			Collection<Identifier> modResources = pack.findResources(type, namespace, prefix, maxDepth, pathFilter);

			resources.addAll(modResources);
		}

		return resources;
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		var packs = this.namespacedPacks.get(id.getNamespace());

		if (packs == null) {
			return false;
		}

		// Iterating backwards as higher-priority packs are placed at the beginning.
		for (int i = packs.size() - 1; i >= 0; i--) {
			ResourcePack pack = packs.get(i);

			if (pack.contains(type, id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return this.namespacedPacks.keySet();
	}

	public String getFullName() {
		return this.getName() + " (" + this.packs.stream().map(ResourcePack::getName).collect(Collectors.joining(", ")) + ")";
	}

	@Override
	public void close() {
		this.packs.forEach(ResourcePack::close);
	}
}
