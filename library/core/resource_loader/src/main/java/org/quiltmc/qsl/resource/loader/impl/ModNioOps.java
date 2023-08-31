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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.impl.cache.EntryType;

/**
 * Represents a NIO implementation of {@link ModIoOps}.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
final class ModNioOps extends ModIoOps {
	ModNioOps(Path basePath, ModMetadata modInfo) {
		super(basePath, modInfo);
	}

	@Override
	public Path getPath(String path) {
		Path childPath = this.getNormalizedPath(path);

		if (childPath.startsWith(basePath) && Files.exists(childPath)) {
			return childPath;
		} else {
			return null;
		}
	}

	@Override
	public EntryType getEntryType(String path) {
		return this.getEntryType(this.getNormalizedPath(path));
	}

	@Override
	public EntryType getEntryType(Path path) {
		if (path.startsWith(basePath)) {
			try {
				var attr = Files.readAttributes(path, BasicFileAttributes.class);

				if (attr.isRegularFile()) {
					return EntryType.FILE;
				} else if (attr.isDirectory()) {
					return EntryType.DIRECTORY;
				}
			} catch (IOException e) {
				// Ignored
			}
		}

		return EntryType.EMPTY;
	}
}
