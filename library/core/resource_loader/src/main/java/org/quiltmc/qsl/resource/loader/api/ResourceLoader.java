/*
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

package org.quiltmc.qsl.resource.loader.api;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePackProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

/**
 * Represents the resource loader. Contains different register methods.
 */
@ApiStatus.NonExtendable
public interface ResourceLoader {
	/**
	 * Get the resource loader instance for a given resource type.
	 * A resource loader instance may be used to register resource reloaders.
	 *
	 * @param type the given resource type
	 * @return the resource loader instance
	 */
	static ResourceLoader get(ResourceType type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * @param resourceReloader the resource reloader
	 */
	void registerReloader(IdentifiableResourceReloader resourceReloader);

	/**
	 * Registers a resource pack profile provider.
	 * <p>
	 * A resource pack profile means any provided resource packs will show up in the resource pack selection screen.
	 * Always fired <i>after</i> the built-in resource pack providers.
	 *
	 * @param provider the provider
	 */
	void registerResourcePackProfileProvider(ResourcePackProvider provider);

	/**
	 * Registers a built-in resource pack.
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active,
	 * similarly to the "Programmer Art" resource pack.
	 * <p>
	 * A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow providing different styles of your assets.
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 * <p>
	 * This method will automatically fetch the {@linkplain ModContainer} based on the namespace provided in {@code id}.
	 *
	 * @param id             the identifier of the resource pack; its namespace must be the same as the mod id
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if the resource pack was successfully registered, or {@code false} otherwise
	 * @throws IllegalArgumentException if a mod with the corresponding namespace given in id cannot be found
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType, Text)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType, Text)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ResourcePackActivationType activationType) {
		return registerBuiltinResourcePack(id, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
	}

	/**
	 * Registers a built-in resource pack.
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active,
	 * similarly to the "Programmer Art" resource pack.
	 * <p>
	 * A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow providing different styles of your assets.
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 * <p>
	 * This method will automatically fetch the {@linkplain ModContainer} based on the namespace provided in {@code id}.
	 *
	 * @param id             the identifier of the resource pack; its namespace must be the same as the mod id
	 * @param activationType the activation type of the resource pack
	 * @param displayName    the display name of the resource pack
	 * @return {@code true} if the resource pack was successfully registered, or {@code false} otherwise
	 * @throws IllegalArgumentException if a mod with the corresponding namespace given in id cannot be found
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType, Text)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ResourcePackActivationType activationType, Text displayName) {
		var container = QuiltLoader.getModContainer(id.getNamespace())
				.orElseThrow(() ->
						new IllegalArgumentException("No mod with mod id " + id.getNamespace() + " could be found"));
		return registerBuiltinResourcePack(id, container, activationType, displayName);
	}

	/**
	 * Registers a built-in resource pack.
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active,
	 * similarly to the "Programmer Art" resource pack.
	 * <p>
	 * A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow providing different styles of your assets.
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if the resource pack was successfully registered, or {@code false} otherwise
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType)
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType, Text)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType, Text)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container,
			ResourcePackActivationType activationType) {
		return registerBuiltinResourcePack(id, container, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
	}

	/**
	 * Registers a built-in resource pack.
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active,
	 * similarly to the "Programmer Art" resource pack.
	 * <p>
	 * A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow providing different styles of your assets.
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @param displayName    the display name of the resource pack
	 * @return {@code true} if the resource pack was successfully registered, or {@code false} otherwise
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType)
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType, Text)
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container,
			ResourcePackActivationType activationType, Text displayName) {
		return ResourceLoaderImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.getPath(), container,
				activationType, displayName);
	}
}
