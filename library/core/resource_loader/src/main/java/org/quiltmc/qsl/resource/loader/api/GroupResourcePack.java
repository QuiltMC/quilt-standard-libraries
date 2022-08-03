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
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceNotFoundException;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents a group resource pack, which holds multiple resource packs as one.
 * <p>
 * The possible use cases are:
 * <ul>
 *   <li>bundling multiple resource packs as one to reduce pollution of the user's UI</li>
 *   <li>replacing the default resource pack with a combination of the default one and all mods' resource packs</li>
 *   <li>etc.</li>
 * </ul>
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

	/**
	 * Gets an unmodifiable list of the resource packs stored in this group resourced pack.
	 *
	 * @return the resource packs
	 */
	public List<? extends ResourcePack> getPacks() {
		return Collections.unmodifiableList(this.packs);
	}

	/**
	 * Gets an unmodifiable list of the resource packs stored in this group resource pack
	 * which contain the given {@code namespace}.
	 *
	 * @param namespace the namespace the packs must contain
	 * @return the list of the matching resource packs
	 */
	public List<? extends ResourcePack> getPacks(String namespace) {
		return Collections.unmodifiableList(this.namespacedPacks.get(namespace));
	}

	/**
	 * Gets a flattened stream of resource packs in this group resource pack.
	 *
	 * @return the flattened stream of resource packs
	 */
	public Stream<? extends ResourcePack> streamPacks() {
		return this.packs.stream().mapMulti((pack, consumer) -> {
			if (pack instanceof GroupResourcePack grouped) {
				grouped.streamPacks().forEach(consumer);
			} else {
				consumer.accept(pack);
			}
		});
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		var packs = this.namespacedPacks.get(id.getNamespace());

		if (packs != null) {
			// Iterating backwards as higher-priority packs are placed at the end.
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
	public Collection<Identifier> findResources(ResourceType type, String namespace, String startingPath,
			Predicate<Identifier> pathFilter) {
		var packs = this.namespacedPacks.get(namespace);

		if (packs == null) {
			return Collections.emptyList();
		}

		var resources = new HashSet<Identifier>();

		// Iterating backwards as higher-priority packs are placed at the end.
		for (int i = packs.size() - 1; i >= 0; i--) {
			ResourcePack pack = packs.get(i);
			Collection<Identifier> modResources = pack.findResources(type, namespace, startingPath, pathFilter);

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

		// Iterating backwards as higher-priority packs are placed at the end.
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

	/**
	 * Represents a group resource pack which wraps a "base" resource pack.
	 */
	public static class Wrapped extends GroupResourcePack {
		private final ResourcePack basePack;

		/**
		 * Constructs a new instance of a group resource pack wrapping a base resource pack.
		 *
		 * @param type         the resource type of this resource pack
		 * @param basePack     the base resource pack
		 * @param packs        the additional packs
		 * @param basePriority {@code true} if the base resource pack has priority over the additional packs, or {@code false} otherwise.
		 *                     Ignored if the base resource pack is already present in the list
		 */
		public Wrapped(ResourceType type, ResourcePack basePack, List<ResourcePack> packs, boolean basePriority) {
			super(type, addToPacksIfNeeded(basePack, packs, basePriority));
			this.basePack = basePack;
		}

		private static List<ResourcePack> addToPacksIfNeeded(ResourcePack basePack, List<ResourcePack> packs,
				boolean basePriority) {
			if (!packs.contains(basePack)) {
				if (basePriority) {
					packs.add(basePack);
				} else {
					packs.add(0, basePack);
				}
			}

			return packs;
		}

		@Override
		public @Nullable InputStream openRoot(String fileName) throws IOException {
			return this.basePack.openRoot(fileName);
		}

		@Override
		public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
			return this.basePack.parseMetadata(metaReader);
		}

		@Override
		public String getName() {
			return this.basePack.getName();
		}

		@Override
		public Text getDisplayName() {
			return this.basePack.getDisplayName();
		}

		@Override
		public String getFullName() {
			return this.getName() + " (" + this.packs.stream().filter(pack -> pack != this.basePack)
					.map(ResourcePack::getName).collect(Collectors.joining(", ")) + ")";
		}
	}
}
