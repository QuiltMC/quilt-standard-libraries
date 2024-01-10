/*
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

package org.quiltmc.qsl.resource.loader.api;

import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.PackProvider;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.event.Event;
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
	static @NotNull ResourceLoader get(@NotNull ResourceType type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * @param resourceReloader the resource reloader
	 * @see #addReloaderOrdering(Identifier, Identifier) add an ordering rule for resource reloaders
	 */
	void registerReloader(@NotNull IdentifiableResourceReloader resourceReloader);

	/**
	 * Requests that resource reloaders registered as the first identifier is applied before the other referenced resource reloader.
	 * <p>
	 * Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 * <p>
	 * Please keep in mind that this only takes effect during the application stage!
	 *
	 * @param firstReloader  the identifier of the resource reloader that should run before the other
	 * @param secondReloader the identifier of the resource reloader that should run after the other
	 * @see org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys identifiers of Vanilla resource reloaders
	 * @see #registerReloader(IdentifiableResourceReloader) register a new resource reloader
	 */
	void addReloaderOrdering(@NotNull Identifier firstReloader, @NotNull Identifier secondReloader);

	/**
	 * Registers a pack profile provider.
	 * <p>
	 * A pack profile means any provided resource packs will show up in the resource pack selection screen.
	 * Always fired <i>after</i> the built-in pack providers.
	 *
	 * @param provider the provider
	 */
	void registerPackProfileProvider(@NotNull PackProvider provider);

	/**
	 * {@return the registration of default packs event}
	 * <p>
	 * This event is triggered when the default resources are created and allow to register additional packs
	 * that are displayed to the user as the {@code "Default"} pack.
	 * Added packs are added before mod packs, meaning mod resources will override added packs' resources.
	 */
	@Contract(pure = true)
	@NotNull Event<PackRegistrationContext.Callback> getRegisterDefaultPackEvent();

	/**
	 * {@return the registration of top packs event}
	 * <p>
	 * This event is triggered once all default and user packs are added.
	 * It allows to add additional packs that can override any other packs.
	 */
	@Contract(pure = true)
	@NotNull Event<PackRegistrationContext.Callback> getRegisterTopPackEvent();

	/**
	 * Creates a new resource pack based on a {@link Path} as its root.
	 * <p>
	 * If the file system of the given {@link Path} is not the operating system's file system then the pack resources will be cached.
	 *
	 * @param id             the identifier of the resource pack; its namespace must be the same as the mod ID
	 * @param rootPath       the root path of this resource pack
	 * @param activationType the activation type hint of this resource pack
	 * @return a new resource pack instance
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType, Text)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType, Text)
	 */
	default @NotNull ResourcePack newFileSystemPack(@NotNull Identifier id, @NotNull Path rootPath,
			PackActivationType activationType) {
		return this.newFileSystemPack(id, rootPath, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
	}

	/**
	 * Creates a new resource pack based on a {@link Path} as its root.
	 * <p>
	 * If the file system of the given {@link Path} is not the operating system's file system then the pack resources will be cached.
	 *
	 * @param id             the identifier of the resource pack; its namespace must be the same as the mod ID
	 * @param rootPath       the root path of this resource pack
	 * @param activationType the activation type hint of this resource pack
	 * @param displayName    the display name of the resource pack
	 * @return a new resource pack instance
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType, Text)
	 */
	default @NotNull ResourcePack newFileSystemPack(@NotNull Identifier id, @NotNull Path rootPath,
													PackActivationType activationType, @NotNull Text displayName) {
		var container = QuiltLoader.getModContainer(id.getNamespace())
				.orElseThrow(() ->
						new IllegalArgumentException("No mod with ID '" + id.getNamespace() + "' could be found"));
		return this.newFileSystemPack(id, container, rootPath, activationType, displayName);
	}

	/**
	 * Creates a new resource pack based on a {@link Path} as its root.
	 * <p>
	 * If the file system of the given {@link Path} is not the operating system's file system then the pack resources will be cached.
	 *
	 * @param id             the identifier of the resource pack
	 * @param owner          the mod which owns this resource pack
	 * @param rootPath       the root path of this resource pack
	 * @param activationType the activation type hint of this resource pack
	 * @return a new resource pack instance
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType)
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType, Text)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType, Text)
	 */
	default @NotNull ResourcePack newFileSystemPack(@NotNull Identifier id, @NotNull ModContainer owner, @NotNull Path rootPath,
			PackActivationType activationType) {
		return this.newFileSystemPack(id, owner, rootPath, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
	}

	/**
	 * Creates a new resource pack based on a {@link Path} as its root.
	 * <p>
	 * If the file system of the given {@link Path} is not the operating system's file system then the pack resources will be cached.
	 *
	 * @param id             the identifier of the resource pack
	 * @param owner          the mod which owns this resource pack
	 * @param rootPath       the root path of this resource pack
	 * @param activationType the activation type hint of this resource pack
	 * @param displayName    the display name of the resource pack
	 * @return a new resource pack instance
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType)
	 * @see #newFileSystemPack(Identifier, Path, PackActivationType, Text)
	 * @see #newFileSystemPack(Identifier, ModContainer, Path, PackActivationType)
	 */
	@NotNull ResourcePack newFileSystemPack(@NotNull Identifier id, @NotNull ModContainer owner, @NotNull Path rootPath,
											PackActivationType activationType, @NotNull Text displayName);

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
	 * @see #registerBuiltinPack(Identifier, PackActivationType, Text)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType, Text)
	 */
	static boolean registerBuiltinPack(@NotNull Identifier id, @NotNull PackActivationType activationType) {
		return registerBuiltinPack(id, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
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
	 * @see #registerBuiltinPack(Identifier, PackActivationType)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType, Text)
	 */
	static boolean registerBuiltinPack(@NotNull Identifier id, @NotNull PackActivationType activationType, Text displayName) {
		var container = QuiltLoader.getModContainer(id.getNamespace())
				.orElseThrow(() ->
						new IllegalArgumentException("No mod with mod id " + id.getNamespace() + " could be found"));
		return registerBuiltinPack(id, container, activationType, displayName);
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
	 * @see #registerBuiltinPack(Identifier, PackActivationType)
	 * @see #registerBuiltinPack(Identifier, PackActivationType, Text)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType, Text)
	 */
	static boolean registerBuiltinPack(@NotNull Identifier id, @NotNull ModContainer container,
			@NotNull PackActivationType activationType) {
		return registerBuiltinPack(id, container, activationType, ResourceLoaderImpl.getBuiltinPackDisplayNameFromId(id));
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
	 * @see #registerBuiltinPack(Identifier, PackActivationType)
	 * @see #registerBuiltinPack(Identifier, PackActivationType, Text)
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType)
	 */
	static boolean registerBuiltinPack(@NotNull Identifier id, @NotNull ModContainer container,
									   @NotNull PackActivationType activationType, Text displayName) {
		return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.getPath(), container,
				activationType, displayName);
	}
}
