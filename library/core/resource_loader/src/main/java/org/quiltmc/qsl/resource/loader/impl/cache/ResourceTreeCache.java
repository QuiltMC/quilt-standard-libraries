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

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceType;

import org.quiltmc.qsl.resource.loader.impl.ModIoOps;

/**
 * A variant of {@link ResourceAccess} that also caches I/O queries into a tree.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
public final class ResourceTreeCache extends ResourceAccess {
	private final CacheTree.Branch tree = CacheTree.newTree();
	private final Map<ResourceType, Set<String>> namespaces = new EnumMap<>(ResourceType.class);

	public ResourceTreeCache(ModIoOps io) {
		super(io);
	}

	@Override
	public @Nullable Entry getEntry(String pathName) {
		var node = this.tree.resolveOrCompute(this.io, pathName);

		return node != null ? node.toEntry(this.io) : null;
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		var namespaces = this.namespaces.get(type);

		if (namespaces != null) {
			return namespaces;
		}

		namespaces = super.getNamespaces(type);
		this.namespaces.put(type, namespaces);

		return namespaces;
	}
}
