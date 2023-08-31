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

import java.io.File;
import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.impl.cache.EntryType;

/**
 * Represents a {@link File} implementation of {@link ModIoOps}.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
final class ModFileOps extends ModIoOps {
	ModFileOps(Path basePath, ModMetadata modInfo) {
		super(basePath, modInfo);
	}

	@Override
	public Path getPath(String path) {
		var p = this.getNormalizedPath(path);

		if (p.toFile().exists()) {
			return p;
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
		File file = path.toFile();

		if (file.isFile()) {
			return EntryType.FILE;
		} else if (file.isDirectory()) {
			return EntryType.DIRECTORY;
		} else {
			return EntryType.EMPTY;
		}
	}
}
