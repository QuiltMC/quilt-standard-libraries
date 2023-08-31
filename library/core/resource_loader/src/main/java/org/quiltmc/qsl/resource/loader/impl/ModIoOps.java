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

import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.impl.cache.EntryType;

/**
 * Defines some I/O operations that may be needed to access a mod's resources.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
public abstract class ModIoOps {
	protected final Path basePath;
	private final String separator;
	private final ModMetadata modMetadata;

	public ModIoOps(Path basePath, ModMetadata modMetadata) {
		this.basePath = basePath;
		this.separator = basePath.getFileSystem().getSeparator();
		this.modMetadata = modMetadata;
	}

	/**
	 * {@return the normalized path to access this mod's resources}
	 *
	 * @param path the Minecraft-formatted resource path
	 */
	public Path getNormalizedPath(String path) {
		return this.basePath.resolve(path.replace("/", this.getSeparator())).toAbsolutePath().normalize();
	}

	/**
	 * {@return the normalized path to access this mod's resources if it exists, or {@code null} otherwise}
	 *
	 * @param path the Minecraft-formatted path
	 */
	public abstract Path getPath(String path);

	public abstract EntryType getEntryType(String path);

	public abstract EntryType getEntryType(Path path);

	/**
	 * {@return the path separator used for this mod}
	 */
	public String getSeparator() {
		return this.separator;
	}

	/**
	 * {@return the mod metadata}
	 */
	public ModMetadata getModMetadata() {
		return this.modMetadata;
	}
}
