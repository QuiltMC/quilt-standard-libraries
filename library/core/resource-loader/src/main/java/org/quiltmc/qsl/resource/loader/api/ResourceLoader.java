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

package org.quiltmc.qsl.resource.loader.api;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

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
	 * Registers a built-in resource pack.
	 *
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>
	 * Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 *
	 * <p>
	 * This method will fetch automatically the {@linkplain ModContainer} based on the namespace provided in {@code id}.
	 *
	 * @param id             the identifier of the resource pack, its namespace must be the same as the mod id
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 *
	 * @see #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ResourcePackActivationType activationType) {
		return FabricLoader.getInstance().getModContainer(id.getNamespace())
				.map(container -> registerBuiltinResourcePack(id, container, activationType))
				.orElse(false);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>
	 * A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>
	 * Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>
	 * The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory.
	 * {@code id path} being the path specified in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 *
	 * @see #registerBuiltinResourcePack(Identifier, ResourcePackActivationType)
	 */
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, ResourcePackActivationType activationType) {
		return false;
	}
}
