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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackProvider;

/**
 * Represents a resource pack provider for built-in mods resource packs and low-priority virtual resource packs.
 */
@ApiStatus.Internal
public final class ModPackProvider implements PackProvider {
	public static final ModPackProvider CLIENT_RESOURCE_PACK_PROVIDER = new ModPackProvider(ResourceType.CLIENT_RESOURCES);
	public static final ModPackProvider SERVER_RESOURCE_PACK_PROVIDER = new ModPackProvider(ResourceType.SERVER_DATA);

	private final ResourceType type;

	public ModPackProvider(ResourceType type) {
		this.type = type;
	}

	@Override
	public void register(Consumer<PackProfile> profileAdder) {
		/*
			Register order rule in this provider:
			1. Mod built-in resource packs
			Register order rule globally:
			1. Default (with mod resource packs bundled) and Vanilla built-in resource packs
			2. Mod built-in resource packs
			3. Low priority virtual resource packs
			4. User resource packs
			5. (Invisible) High-priority virtual resource packs
		 */

		ResourceLoaderImpl.registerBuiltinPacks(this.type, profileAdder);

		for (var provider : ResourceLoaderImpl.get(this.type).resourcePackProfileProviders) {
			provider.register(profileAdder);
		}
	}
}
