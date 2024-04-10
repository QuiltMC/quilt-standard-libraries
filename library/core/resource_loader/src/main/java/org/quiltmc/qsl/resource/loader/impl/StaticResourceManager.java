/*
 * Copyright 2023 The Quilt Project
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

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;

/**
 * Exists primarily for determining whether a manager is involved in static resource loading or regular resource loading,
 * and also prevents the MultiPackResourceManager from being closed.
 * <p>
 * This is used to avoid activating certain mixins when they are not necessary.
 */
@ApiStatus.Internal
public class StaticResourceManager extends MultiPackResourceManager {
	public StaticResourceManager(ResourceType type, List<ResourcePack> packs) {
		super(type, packs);
	}

	/**
	 * Forbids the StaticResourceManager instance from being closed (by throwing a RuntimeException) as a sanity check of sorts.
	 * <p>
	 * Ultimately, this method should never be called by consumers of the API,
	 * as the two StaticResourceManager instances are cast to {@link net.minecraft.resource.ResourceManager} when provided,
	 * and ResourceManager does <i>not</i> extend {@link AutoCloseable}.
	 */
	@Override
	public void close() {
		throw new RuntimeException("StaticResourceManager instances cannot be closed!");
	}
}
