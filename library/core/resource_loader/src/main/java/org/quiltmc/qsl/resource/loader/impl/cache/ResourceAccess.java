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

package org.quiltmc.qsl.resource.loader.impl.cache;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resource.ResourceType;

import org.quiltmc.qsl.resource.loader.impl.ModIoOps;
import org.quiltmc.qsl.resource.loader.mixin.IdentifierAccessor;

/**
 * Represents a simple way to access resources, no caching is applied.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
public class ResourceAccess {
	protected static final Logger LOGGER = LogUtils.getLogger();
	protected final ModIoOps io;

	public ResourceAccess(ModIoOps io) {
		this.io = io;
	}

	/**
	 * Gets a resource entry from the given path.
	 *
	 * @param pathName the path of the resource
	 * @return the resource entry if it exists, or {@code null} otherwise
	 */
	public @Nullable Entry getEntry(String pathName) {
		var path = this.io.getPath(pathName);

		if (path == null) {
			return null;
		}

		try {
			var attributes = Files.readAttributes(path, BasicFileAttributes.class);

			if (attributes.isRegularFile()) {
				return new Entry(path, EntryType.FILE);
			} else if (attributes.isDirectory()) {
				return new Entry(path, EntryType.DIRECTORY);
			}
		} catch (IOException e) {
			// ignored
		}

		return null;
	}

	/**
	 * Gets the resource entry type of the given path.
	 *
	 * @param pathName the path of the resource
	 * @return the resource entry type
	 */
	public EntryType getEntryType(String pathName) {
		var entry = this.getEntry(pathName);

		return entry != null ? entry.type() : EntryType.EMPTY;
	}

	/**
	 * {@return the available namespaces for a given resource type}
	 *
	 * @param type the type of resources
	 */
	public Set<String> getNamespaces(ResourceType type) {
		try {
			var entry = this.getEntry(type.getDirectory());

			if (entry == null || entry.type() != EntryType.DIRECTORY) {
				return Collections.emptySet();
			}

			var namespaces = new HashSet<String>();

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(entry.path(), Files::isDirectory)) {
				for (Path path : stream) {
					String s = path.getFileName().toString();
					// s may contain trailing slashes, remove them
					s = s.replace(this.io.getSeparator(), "");

					// Empty file names are disallowed anyway so no need to check for length.
					if (IdentifierAccessor.callIsNamespaceValid(s)) {
						namespaces.add(s);
					} else {
						this.warnInvalidNamespace(s);
					}
				}
			}

			return namespaces;
		} catch (IOException e) {
			LOGGER.warn("getNamespaces in mod " + this.io.getModMetadata().id() + " failed!", e);
			return Collections.emptySet();
		}
	}

	protected void warnInvalidNamespace(String s) {
		LOGGER.warn("Quilt NioResourcePack: ignored invalid namespace: {} in mod ID {}", s, this.io.getModMetadata().id());
	}

	public record Entry(Path path, EntryType type) {
	}
}
