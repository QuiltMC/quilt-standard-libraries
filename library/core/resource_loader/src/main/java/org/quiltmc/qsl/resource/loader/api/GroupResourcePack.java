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

package org.quiltmc.qsl.resource.loader.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.resource.ResourceIoSupplier;
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
	private boolean builtin;

	public GroupResourcePack(@NotNull ResourceType type, @NotNull List<? extends ResourcePack> packs) {
		this.type = type;
		this.packs = packs;
		this.recompute();
	}

	/**
	 * Gets an unmodifiable list of the resource packs stored in this group resourced pack.
	 *
	 * @return the resource packs
	 */
	public @UnmodifiableView List<? extends ResourcePack> getPacks() {
		return Collections.unmodifiableList(this.packs);
	}

	/**
	 * Gets an unmodifiable list of the resource packs stored in this group resource pack
	 * which contain the given {@code namespace}.
	 *
	 * @param namespace the namespace the packs must contain
	 * @return the list of the matching resource packs
	 */
	public @UnmodifiableView List<? extends ResourcePack> getPacks(String namespace) {
		return Collections.unmodifiableList(this.namespacedPacks.get(namespace));
	}

	/**
	 * Gets a flattened stream of resource packs in this group resource pack.
	 *
	 * @return the flattened stream of resource packs
	 */
	public @NotNull Stream<? extends ResourcePack> streamPacks() {
		return this.packs.stream().mapMulti((pack, consumer) -> {
			if (pack instanceof GroupResourcePack grouped) {
				grouped.streamPacks().forEach(consumer);
			} else {
				consumer.accept(pack);
			}
		});
	}

	/**
	 * Recomputes some cached data in case the resource pack list changes.
	 */
	public void recompute() {
		this.namespacedPacks.clear();
		this.packs.forEach(pack -> pack.getNamespaces(this.type)
				.forEach(namespace -> this.namespacedPacks.computeIfAbsent(namespace, value -> new ArrayList<>())
						.add(pack)));

		this.builtin = this.packs.stream().allMatch(ResourcePack::isBuiltin);
	}

	@Override
	public @Nullable ResourceIoSupplier<InputStream> open(ResourceType type, Identifier id) {
		var packs = this.namespacedPacks.get(id.getNamespace());

		if (packs != null) {
			// Iterating backwards as higher-priority packs are placed at the end.
			for (int i = packs.size() - 1; i >= 0; i--) {
				ResourcePack pack = packs.get(i);
				var supplier = pack.open(type, id);

				if (supplier != null) {
					return supplier;
				}
			}
		}

		return null;
	}

	@Override
	public void listResources(ResourceType type, String namespace, String startingPath,
			ResourcePack.ResourceConsumer consumer) {
		var packs = this.namespacedPacks.get(namespace);

		// Iterating backwards as higher-priority packs are placed at the end.
		for (var pack : packs) {
			pack.listResources(type, namespace, startingPath, consumer);
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return this.namespacedPacks.keySet();
	}

	public @NotNull String getFullName() {
		return this.getName() + " (" + this.packs.stream().map(ResourcePack::getName).collect(Collectors.joining(", ")) + ")";
	}

	@Override
	public boolean isBuiltin() {
		return this.builtin;
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
		public Wrapped(@NotNull ResourceType type, @NotNull ResourcePack basePack, @NotNull List<ResourcePack> packs, boolean basePriority) {
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
		public @Nullable ResourceIoSupplier<InputStream> openRoot(String... path) {
			return this.basePack.openRoot(path);
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
		public @NotNull Text getDisplayName() {
			return this.basePack.getDisplayName();
		}

		@Override
		public @NotNull String getFullName() {
			return this.getName() + " (" + this.packs.stream().filter(pack -> pack != this.basePack)
					.map(ResourcePack::getName).collect(Collectors.joining(", ")) + ")";
		}
	}
}
