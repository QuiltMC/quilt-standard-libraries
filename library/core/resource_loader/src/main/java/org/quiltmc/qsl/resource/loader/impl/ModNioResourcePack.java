/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.AbstractFileResourcePack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.impl.filesystem.QuiltJoinedFileSystem;
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

	static ModNioResourcePack ofMod(ModMetadata modInfo, Path path, ResourceType type, @Nullable String name) {
		return new ModNioResourcePack(
				name, modInfo, null, ResourcePackActivationType.ALWAYS_ENABLED,
				path, type, null
		);
	}

	public ModNioResourcePack(@Nullable String name, ModMetadata modInfo, @Nullable Text displayName, ResourcePackActivationType activationType,
			Path path, ResourceType type, @Nullable AutoCloseable closer) {
		super(null);

		/* Metadata */
		this.name = name == null ? ModResourcePackUtil.getName(modInfo) : name;
		this.displayName = displayName == null ? new LiteralText(name) : displayName;
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
		if (DISABLE_CACHING || path.getFileSystem() == DEFAULT_FILESYSTEM || path.getFileSystem() instanceof QuiltJoinedFileSystem) {
			// The default file system means it's on-disk files that may change, and QuiltJoinedFileSystem is usually used for dev envs too.
			this.cache = new ResourceAccess(this.io);
		} else {
			// Allows caching for mods that don't have mutable resources.
			this.cache = new ResourceTreeCache(this.io);
		}
	}

	@Override
	protected InputStream openFile(String filePath) throws IOException {
		InputStream stream;

		ResourceAccess.Entry entry = this.cache.getEntry(filePath);

		if (entry != null && entry.type() == EntryType.FILE) {
			return Files.newInputStream(entry.path());
		}

		stream = ModResourcePackUtil.openDefault(this.modInfo, this.type, filePath);

		if (stream != null) {
			return stream;
		}

		// FileNotFoundException is an IOException, which is properly handled by the Vanilla resource loader and
		// prints to the logs.
		throw new FileNotFoundException("\"" + filePath + "\" in Quilt mod \"" + modInfo.id() + "\"");
	}

	@Override
	protected boolean containsFile(String filePath) {
		if (ModResourcePackUtil.containsDefault(modInfo, filePath)) {
			return true;
		}

		return this.cache.getEntryType(filePath) == EntryType.FILE;
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String startingPath, int depth,
			Predicate<String> pathFilter) {
		var ids = new ArrayList<Identifier>();
		String namespacePath = type.getDirectory() + '/' + namespace;
		String nioPath = startingPath.replace("/", this.io.getSeparator());

		ResourceAccess.Entry namespaceEntry = this.cache.getEntry(namespacePath);

		if (namespaceEntry != null) {
			ResourceAccess.Entry searchEntry = this.cache.getEntry(namespacePath + '/' + nioPath);

			if (searchEntry != null) {
				try {
					Files.walk(searchEntry.path(), depth)
							.filter(Files::isRegularFile)
							.filter((p) -> {
								String filename = p.getFileName().toString();
								return !filename.endsWith(".mcmeta") && pathFilter.test(filename);
							})
							.map(namespaceEntry.path()::relativize)
							.map((p) -> p.toString().replace(this.io.getSeparator(), "/"))
							.forEach((s) -> {
								try {
									ids.add(new Identifier(namespace, s));
								} catch (InvalidIdentifierException e) {
									LOGGER.error(e.getMessage());
								}
							});
				} catch (IOException e) {
					LOGGER.warn("findResources at " + startingPath + " in namespace " + namespace
							+ ", mod " + this.modInfo.id() + " failed!", e);
				}
			}
		}

		return ids;
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

	//region metadata
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Text getDisplayName() {
		return this.displayName;
	}

	@Override
	public ResourcePackActivationType getActivationType() {
		return this.activationType;
	}
	//endregion
}
