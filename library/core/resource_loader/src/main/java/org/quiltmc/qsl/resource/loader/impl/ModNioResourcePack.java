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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

/**
 * A NIO implementation of a mod resource pack.
 */
@ApiStatus.Internal
public class ModNioResourcePack extends AbstractFileResourcePack {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModNioResourcePack.class);
	private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_]+");
	private final String name;
	private final ModMetadata modInfo;
	private final Path basePath;
	private final ResourceType type;
	private final boolean cacheable;
	private final AutoCloseable closer;
	private final String separator;
	private final ResourcePackActivationType activationType;
	private final Map<ResourceType, Set<String>> namespaces = new EnumMap<>(ResourceType.class);

	public ModNioResourcePack(@Nullable String name, ModMetadata modInfo, Path path,
	                          ResourceType type, AutoCloseable closer,
	                          ResourcePackActivationType activationType) {
		super(null);
		this.name = name == null ? ModResourcePackUtil.getName(modInfo) : name;
		this.modInfo = modInfo;
		this.basePath = path.toAbsolutePath().normalize();
		this.type = type;
		// Cache ModNioResourcePacks if not in dev environment because it's not supposed to be mutable in production.
		this.cacheable = modInfo.getId().equals("minecraft") || !FabricLoader.getInstance().isDevelopmentEnvironment();
		this.closer = closer;
		this.separator = basePath.getFileSystem().getSeparator();
		this.activationType = activationType;
	}

	private Path getPath(String filename) {
		Path childPath = this.basePath.resolve(filename.replace("/", separator)).toAbsolutePath().normalize();

		if (childPath.startsWith(basePath) && Files.exists(childPath)) {
			return childPath;
		} else {
			return null;
		}
	}

	@Override
	protected InputStream openFile(String filename) throws IOException {
		InputStream stream;

		Path path = this.getPath(filename);

		if (path != null && Files.isRegularFile(path)) {
			return Files.newInputStream(path);
		}

		stream = ModResourcePackUtil.openDefault(this.modInfo, this.type, filename);

		if (stream != null) {
			return stream;
		}

		// FileNotFoundException is an IOException, which is properly handled by the Vanilla resource loader and
		// prints to the logs.
		throw new FileNotFoundException("\"" + filename + "\" in Quilt mod \"" + modInfo.getId() + "\"");
	}

	@Override
	protected boolean containsFile(String filename) {
		if (ModResourcePackUtil.containsDefault(modInfo, filename)) {
			return true;
		}

		Path path = this.getPath(filename);
		return path != null && Files.isRegularFile(path);
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String path, int depth,
	                                            Predicate<String> pathFilter) {
		var ids = new ArrayList<Identifier>();
		String nioPath = path.replace("/", separator);

		Path namespacePath = this.getPath(type.getDirectory() + "/" + namespace);

		if (namespacePath != null) {
			Path searchPath = namespacePath.resolve(nioPath).toAbsolutePath().normalize();

			if (Files.exists(searchPath)) {
				try {
					Files.walk(searchPath, depth)
							.filter(Files::isRegularFile)
							.filter((p) -> {
								String filename = p.getFileName().toString();
								return !filename.endsWith(".mcmeta") && pathFilter.test(filename);
							})
							.map(namespacePath::relativize)
							.map((p) -> p.toString().replace(separator, "/"))
							.forEach((s) -> {
								try {
									ids.add(new Identifier(namespace, s));
								} catch (InvalidIdentifierException e) {
									LOGGER.error(e.getMessage());
								}
							});
				} catch (IOException e) {
					LOGGER.warn("findResources at " + path + " in namespace " + namespace
							+ ", mod " + this.modInfo.getId() + " failed!", e);
				}
			}
		}

		return ids;
	}

	protected void warnInvalidNamespace(String s) {
		LOGGER.warn("Quilt NioResourcePack: ignored invalid namespace: {} in mod ID {}",
				s, this.modInfo.getId());
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		if (this.cacheable) {
			var namespaces = this.namespaces.get(type);

			if (namespaces != null) {
				return namespaces;
			}
		}

		try {
			Path typePath = this.getPath(type.getDirectory());

			if (typePath == null || !(Files.isDirectory(typePath))) {
				return Collections.emptySet();
			}

			var namespaces = new HashSet<String>();

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(typePath, Files::isDirectory)) {
				for (Path path : stream) {
					String s = path.getFileName().toString();
					// s may contain trailing slashes, remove them
					s = s.replace(separator, "");

					if (RESOURCE_PACK_PATH.matcher(s).matches()) {
						namespaces.add(s);
					} else {
						this.warnInvalidNamespace(s);
					}
				}
			}

			if (this.cacheable) {
				this.namespaces.put(type, namespaces);
			}

			return namespaces;
		} catch (IOException e) {
			LOGGER.warn("getNamespaces in mod " + modInfo.getId() + " failed!", e);
			return Collections.emptySet();
		}
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

	public ResourcePackActivationType getActivationType() {
		return this.activationType;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
