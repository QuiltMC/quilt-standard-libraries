/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.mining_levels.impl;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@ApiStatus.Internal
public class MiningLevelCacheInvalidator implements SimpleSynchronousResourceReloader {
	private static final Identifier ID = new Identifier("quilt-mining-levels", "cache_invalidator");
	private static final Set<Identifier> DEPENDENCIES = Collections.singleton(ResourceReloaderKeys.Server.TAGS);

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	@Override
	public Collection<Identifier> getQuiltDependencies() {
		return DEPENDENCIES;
	}

	@Override
	public void reload(ResourceManager manager) {
		MiningLevelManagerImpl.clearCache();
	}
}
