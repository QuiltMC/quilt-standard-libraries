/*
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

package org.quiltmc.qsl.resource.loader.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;

/**
 * Represents a resource pack whose resources are mutable.
 */
public interface MutableResourcePack extends ResourcePack {
	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the resource content
	 */
	void putResource(@NotNull String fileName, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the resource content
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 */
	void putResource(@NotNull String fileName, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a resource into the resource pack's root asynchronously.
	 *
	 * @param fileName        the name of the file
	 * @param resourceFactory the factory of the resource content
	 * @return the future
	 */
	@NotNull Future<byte[]> putResourceAsync(@NotNull String fileName, @NotNull Function<@NotNull String, byte @NotNull []> resourceFactory);

	/**
	 * Puts a resource into the resource pack for the given side and path asynchronously.
	 *
	 * @param type            the resource type
	 * @param id              the path of the resource
	 * @param resourceFactory the factory of the resource content
	 * @return the future
	 */
	@NotNull Future<byte[]> putResourceAsync(@NotNull ResourceType type, @NotNull Identifier id,
			@NotNull Function<@NotNull Identifier, byte @NotNull []> resourceFactory);

	default void putText(String fileName, String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	default void putText(ResourceType type, Identifier id, String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	default void putText(String fileName, Supplier<String> textSupplier) {
		this.putResource(fileName, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	default void putText(ResourceType type, Identifier id, Supplier<String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	@Environment(EnvType.CLIENT)
	default void putImage(String fileName, NativeImage image) throws IOException {
		this.putResource(fileName, image.getBytes());
	}

	@Environment(EnvType.CLIENT)
	default void putImage(Identifier id, NativeImage image) throws IOException {
		this.putResource(ResourceType.CLIENT_RESOURCES, id, image.getBytes());
	}

	@Environment(EnvType.CLIENT)
	default void putImage(String fileName, Supplier<NativeImage> imageSupplier) {
		this.putResource(fileName, () -> {
			try (var image = imageSupplier.get()) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Environment(EnvType.CLIENT)
	default void putImage(Identifier id, Supplier<NativeImage> imageSupplier) {
		this.putResource(ResourceType.CLIENT_RESOURCES, id, () -> {
			try (var image = imageSupplier.get()) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Clears the resource of a specific resource type.
	 *
	 * @param type the resource type
	 */
	void clearResources(ResourceType type);

	/**
	 * Clears all the resources from memory.
	 */
	void clearResources();
}
