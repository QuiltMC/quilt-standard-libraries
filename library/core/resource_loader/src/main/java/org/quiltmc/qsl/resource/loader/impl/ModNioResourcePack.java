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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.AbstractFileResourcePack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.quiltmc.loader.api.CachedFileSystem;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.QuiltResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.impl.cache.EntryType;
import org.quiltmc.qsl.resource.loader.impl.cache.ResourceAccess;
import org.quiltmc.qsl.resource.loader.impl.cache.ResourceTreeCache;

/**
 * A NIO implementation of a mod resource pack.
 */
@ApiStatus.Internal
public class ModNioResourcePack extends AbstractFileResourcePack implements QuiltResourcePack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FileSystem DEFAULT_FILESYSTEM = FileSystems.getDefault();
	private static final boolean DISABLE_CACHING = TriState.fromProperty("quilt.resource_loader.disable_caching").toBooleanOrElse(false);
	/* Metadata */
	private final String name;
	private final Text displayName;
	final ModMetadata modInfo;
	private final ResourcePackActivationType activationType;
	/* Resource Stuff */
	private final ModIoOps io;
	final ResourceType type;
	private final @Nullable AutoCloseable closer;
	/* Caches */
	private final ResourceAccess cache;

	static ModNioResourcePack ofMod(ModMetadata modInfo, Path path, ResourceType type) {
		return new ModNioResourcePack(
				null, modInfo, null, ResourcePackActivationType.ALWAYS_ENABLED,
				path, type, null
		);
	}

	public ModNioResourcePack(@Nullable String name, ModMetadata modInfo, @Nullable Text displayName, ResourcePackActivationType activationType,
			Path path, ResourceType type, @Nullable AutoCloseable closer) {
		super(null, true);

		/* Metadata */
		this.name = name == null ? ModResourcePackUtil.getName(modInfo) : name;
		this.displayName = displayName == null ? Text.of(name) : displayName;
		this.modInfo = modInfo;
		this.activationType = activationType;

		/* Resource Stuff */
		this.type = type;
		this.closer = closer;

		if (path.getFileSystem() == DEFAULT_FILESYSTEM) {
			this.io = new ModFileOps(path.toAbsolutePath().normalize(), modInfo);
		} else {
			this.io = new ModNioOps(path.toAbsolutePath().normalize(), modInfo);
		}

		/* Cache */
		if (DISABLE_CACHING || path.getFileSystem() == DEFAULT_FILESYSTEM || path.getFileSystem() instanceof CachedFileSystem cached && !cached.isPermanentlyReadOnly()) {
			// The default file system means it's on-disk files that may change
			this.cache = new ResourceAccess(this.io);
		} else {
			// Allows caching for mods that don't have mutable resources.
			this.cache = new ResourceTreeCache(this.io);
		}
	}

	@Override
	public @Nullable ResourceIoSupplier<InputStream> openRoot(String... path) {
		String actualPath = String.join("/", path);

		return this.open(actualPath);
	}

	@Override
	public @Nullable ResourceIoSupplier<InputStream> open(ResourceType type, Identifier id) {
		return this.open(QuiltResourcePack.getResourcePath(type, id));
	}

	protected ResourceIoSupplier<InputStream> open(String filePath) {
		ResourceAccess.Entry entry = this.cache.getEntry(filePath);

		if (entry != null && entry.type() == EntryType.FILE) {
			return ResourceIoSupplier.create(entry.path());
		}

		return ModResourcePackUtil.openDefault(this.modInfo, this.type, filePath);
	}

	@Override
	public void listResources(ResourceType type, String namespace, String startingPath, ResourceConsumer consumer) {
		String namespacePath = type.getDirectory() + '/' + namespace;
		String nioPath = startingPath.replace("/", this.io.getSeparator());

		ResourceAccess.Entry namespaceEntry = this.cache.getEntry(namespacePath);

		if (namespaceEntry != null) {
			ResourceAccess.Entry searchEntry = this.cache.getEntry(namespacePath + '/' + nioPath);

			if (searchEntry != null) {
				try (var stream = Files.walk(searchEntry.path())) {
					stream.filter(p -> Files.isRegularFile(p) && !p.getFileName().endsWith(".mcmeta"))
							.forEach(p -> {
								String idPath = namespaceEntry.path().relativize(p).toString()
										.replace(this.io.getSeparator(), "/");
								Identifier id = Identifier.tryValidate(namespace, idPath);

								if (id == null) {
									Util.logAndPause(String.format(Locale.ROOT, "Invalid path in pack (%s [%s]): %s:%s, ignoring",
											this.getName(), this.modInfo.id(), namespace, idPath
									));
								} else {
									consumer.accept(id, ResourceIoSupplier.create(p));
								}
							});
				} catch (IOException e) {
					LOGGER.warn("findResources at " + startingPath + " in namespace " + namespace
							+ ", mod " + this.modInfo.id() + " failed!", e);
				}
			}
		}
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return this.cache.getNamespaces(type);
	}

	@Override
	public void close() {
		if (this.closer != null) {
			try {
				this.closer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		ResourceIoSupplier<InputStream> resource = this.openRoot(ResourcePack.PACK_METADATA_NAME);

		if (resource == null) {
			return null;
		} else {
			try (InputStream stream = resource.get()) {
				return ResourceLoaderImpl.parseMetadata(metaReader, this, stream);
			}
		}
	}

	//region metadata
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public @NotNull Text getDisplayName() {
		return this.displayName;
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}

	@Override
	public @NotNull ResourcePackActivationType getActivationType() {
		return this.activationType;
	}
	//endregion
}
