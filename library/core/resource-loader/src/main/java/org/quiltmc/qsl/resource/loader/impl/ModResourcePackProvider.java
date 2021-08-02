/*
 * Copyright 2021 QuiltMC
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

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Represents a resource pack provider for built-in mods resource packs and low-priority virtual resource packs.
 */
@ApiStatus.Internal
public final class ModResourcePackProvider implements ResourcePackProvider {
	/**
	 * Equivalent to {@link ResourcePackSource#PACK_SOURCE_BUILTIN} but allows a different reference equality.
	 */
	public static final ResourcePackSource PACK_SOURCE_MOD_BUILTIN = nameAndSource("pack.source.builtin");
	public static final ModResourcePackProvider CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackProvider(ResourceType.CLIENT_RESOURCES);
	public static final ModResourcePackProvider SERVER_RESOURCE_PACK_PROVIDER = new ModResourcePackProvider(ResourceType.SERVER_DATA);

	private final ResourceType type;
	private final ResourcePackProfile.Factory factory;

	public ModResourcePackProvider(ResourceType type) {
		this.type = type;
		this.factory = (name, text, bl, supplier, metadata, initialPosition, source) ->
				new ResourcePackProfile(name, text, bl, supplier, metadata, type, initialPosition, source);
	}

	/**
	 * Registers the resource packs.
	 *
	 * @param consumer The resource pack profile consumer.
	 */
	public void register(Consumer<ResourcePackProfile> consumer) {
		this.register(consumer, this.factory);
	}

	@Override
	public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
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

		ResourceLoaderImpl.registerBuiltinResourcePacks(this.type, profileAdder, factory);
	}

	private static ResourcePackSource nameAndSource(String source) {
		Text text = new TranslatableText(source);
		return name -> (new TranslatableText("pack.nameAndSource", name, text)).formatted(Formatting.GRAY);
	}
}
