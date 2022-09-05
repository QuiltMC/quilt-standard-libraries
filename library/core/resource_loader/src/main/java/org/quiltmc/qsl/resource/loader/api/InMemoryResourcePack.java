/*
 * Copyright 2022 QuiltMC
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.AbstractFileResourcePack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

public abstract class InMemoryResourcePack implements ResourcePack {
	private final Set<String> namespaces = new HashSet<>();
	private final Map<String, byte[]> resources = new Object2ObjectOpenHashMap<>();
	private final Map<String, Supplier<byte[]>> resourcesToCompute = new Object2ObjectOpenHashMap<>();

	@Override
	public @Nullable InputStream openRoot(String fileName) throws IOException {
		if (!fileName.contains("/") && !fileName.contains("\\")) {
			byte[] bytes = this.resources.get(fileName);

			if (bytes == null) {
				throw new FileNotFoundException("Could not find root resource \"" + fileName + "\" in pack " + this.getName() + ".");
			}

			return new ByteArrayInputStream(bytes);
		} else {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		String path = this.getPath(type, id);

		byte[] bytes = this.resources.get(path);

		if (bytes == null) {
			var supplier = this.resourcesToCompute.remove(path);

			if (supplier == null) {
				throw new FileNotFoundException("Could not find resource \"" + path + "\" in pack " + this.getName() + ".");
			}

			bytes = supplier.get();
			this.resources.put(path, bytes);
		}

		return new ByteArrayInputStream(bytes);
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String startingPath, Predicate<Identifier> pathFilter) {
		return null;
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		String path = this.getPath(type, id);

		return this.resources.containsKey(path) || this.resourcesToCompute.containsKey(path);
	}

	@Override
	public @UnmodifiableView Set<String> getNamespaces(ResourceType type) {
		return Collections.unmodifiableSet(this.namespaces);
	}

	@Override
	public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		try (var stream = this.openRoot(ResourcePack.PACK_METADATA_NAME)) {
			return AbstractFileResourcePack.parseMetadata(metaReader, stream);
		}
	}

	@Override
	public void close() {
	}

	protected String getPath(ResourceType type, Identifier id) {
		return type.getDirectory() + '/' + id.getNamespace() + '/' + id.getPath();
	}
}
