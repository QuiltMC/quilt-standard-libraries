/*
 * Copyright 2022 The Quilt Project
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

/**
 * Represents an in-memory resource pack.
 * <p>
 * The resources of this pack are stored in memory instead of it being on-disk.
 */
public abstract class InMemoryResourcePack implements MutableResourcePack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ExecutorService EXECUTOR_SERVICE;
	private static final boolean DUMP = TriState.fromProperty("quilt.resource_loader.debug.pack.dump_from_in_memory")
			.toBooleanOrElse(QuiltLoader.isDevelopmentEnvironment());
	private static final String VIRTUAL_ASYNC_THREADS_PROPERTY = "quilt.resource_loader.pack.virtual_async_threads";
	private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
	private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
	private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();

	@Override
	public @Nullable ResourceIoSupplier<InputStream> openRoot(String... path) {
		String actualPath = String.join("/", path);

		return this.openResource(this.root, actualPath);
	}

	@Override
	public @Nullable ResourceIoSupplier<InputStream> open(ResourceType type, Identifier id) {
		return this.openResource(this.getResourceMap(type), id);
	}

	protected <T> @Nullable ResourceIoSupplier<InputStream> openResource(Map<T, Supplier<byte[]>> map, @NotNull T key) {
		var supplier = map.get(key);

		if (supplier == null) {
			return null;
		}

		byte[] bytes = supplier.get();

		if (bytes == null) {
			return null;
		}

		return () -> new ByteArrayInputStream(bytes);
	}

	@Override
	public void listResources(ResourceType type, String namespace, String startingPath, ResourceConsumer consumer) {
		this.getResourceMap(type).entrySet().stream()
				.filter(entry -> entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(startingPath))
				.forEach(entry -> {
					byte[] bytes = entry.getValue().get();

					if (bytes != null) {
						consumer.accept(entry.getKey(), () -> new ByteArrayInputStream(bytes));
					}
				});
	}

	@Override
	public @Unmodifiable Set<String> getNamespaces(ResourceType type) {
		return this.getResourceMap(type).keySet().stream()
				.map(Identifier::getNamespace)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		if (!this.root.containsKey(ResourcePack.PACK_METADATA_NAME)) {
			var json = new JsonObject();
			var packJson = new JsonObject();
			packJson.addProperty("description", "A virtual resource pack.");
			packJson.addProperty("pack_format", 5); // This is like, not read by any significant system when invisible to users.
			json.add("pack", packJson);

			if (!json.has(metaReader.getKey())) {
				return null;
			} else {
				try {
					return metaReader.fromJson(JsonHelper.getObject(json, metaReader.getKey()));
				} catch (Exception e) {
					LOGGER.error("Couldn't load {} metadata from pack \"{}\":", metaReader.getKey(), this.getName(), e);
					return null;
				}
			}
		}

		var resource = this.openRoot(ResourcePack.PACK_METADATA_NAME);
		if (resource == null) return null;

		try (var stream = resource.get()) {
			return ResourceLoaderImpl.parseMetadata(metaReader, this, stream);
		}
	}

	@Override
	public void close() {
		if (DUMP) {
			this.dumpAll();
		}
	}

	@Override
	public void putResource(@NotNull String fileName, byte @NotNull [] resource) {
		this.root.put(fileName, () -> resource);
	}

	@Override
	public void putResource(@NotNull ResourceType type, @NotNull Identifier id, byte @NotNull [] resource) {
		this.getResourceMap(type).put(id, () -> resource);
	}

	@Override
	public void putResource(@NotNull String fileName, @NotNull Supplier<byte[]> resource) {
		this.root.put(fileName, Suppliers.memoize(resource::get));
	}

	@Override
	public void putResource(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<byte @NotNull []> resource) {
		this.getResourceMap(type).put(id, Suppliers.memoize(resource::get));
	}

	@Override
	public @NotNull Future<byte[]> putResourceAsync(@NotNull String fileName,
			@NotNull Function<@NotNull String, byte @NotNull []> resourceFactory) {
		Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> resourceFactory.apply(fileName));
		this.putResource(fileName, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		return future;
	}

	@Override
	public @NotNull Future<byte[]> putResourceAsync(@NotNull ResourceType type, @NotNull Identifier id,
			@NotNull Function<@NotNull Identifier, byte @NotNull []> resourceFactory) {
		Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> resourceFactory.apply(id));
		this.putResource(type, id, () -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		return future;
	}

	@Override
	public void clearResources(ResourceType type) {
		this.getResourceMap(type).clear();
	}

	@Override
	public void clearResources() {
		this.root.clear();
		this.clearResources(ResourceType.CLIENT_RESOURCES);
		this.clearResources(ResourceType.SERVER_DATA);
	}

	/**
	 * Dumps the content of this resource pack into the given path.
	 *
	 * @param path the path to dump the resources into
	 */
	public void dumpTo(@NotNull Path path) {
		try {
			Files.createDirectories(path);

			this.root.forEach((p, resource) -> this.dumpResource(path, p, resource.get()));
			this.assets.forEach((p, resource) ->
					this.dumpResource(path, QuiltResourcePack.getResourcePath(ResourceType.CLIENT_RESOURCES, p), resource.get()));
			this.data.forEach((p, resource) ->
					this.dumpResource(path, QuiltResourcePack.getResourcePath(ResourceType.SERVER_DATA, p), resource.get()));
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {} to {}.", this.getName(), path, e);
		}
	}

	protected void dumpAll() {
		this.dumpTo(Paths.get("debug", "packs", this.getName()));
	}

	protected void dumpResource(Path parentPath, String resourcePath, byte[] resource) {
		try {
			var p = parentPath.resolve(resourcePath);
			Files.createDirectories(p.getParent());
			Files.write(p, resource, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {}.", this.getName(), e);
		}
	}

	private Map<Identifier, Supplier<byte[]>> getResourceMap(ResourceType type) {
		return switch (type) {
			case CLIENT_RESOURCES -> this.assets;
			case SERVER_DATA -> this.data;
		};
	}

	static {
		int threads = Math.max(Runtime.getRuntime().availableProcessors() / 2 - 1, 1);
		String threadsOverride = System.getProperty(VIRTUAL_ASYNC_THREADS_PROPERTY);

		if (threadsOverride != null) {
			try {
				threads = Integer.parseInt(threadsOverride);
			} catch (NumberFormatException e) {
				LOGGER.error("Could not use the number provided by the property \"{}\": ", VIRTUAL_ASYNC_THREADS_PROPERTY, e);
			}
		}

		EXECUTOR_SERVICE = Executors.newFixedThreadPool(
				threads,
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Quilt-Resource-Loader-Virtual-Pack-Worker-%s").build()
		);
	}

	/**
	 * Represents an in-memory resource pack with a static name.
	 */
	public static class Named extends InMemoryResourcePack {
		private final String name;

		public Named(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
