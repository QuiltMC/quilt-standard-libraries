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

package org.quiltmc.qsl.resource.loader.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents a resource pack whose resources are mutable.
 */
public interface MutableResourcePack extends ResourcePack {
	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the resource content
	 * @see #putResource(ResourceType, Identifier, byte[])
	 * @see #putResource(String, Supplier)
	 * @see #putResourceAsync(String, Function)
	 */
	void putResource(@NotNull String fileName, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the resource content
	 * @see #putResource(String, byte[])
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 * @see #putResourceAsync(ResourceType, Identifier, Function)
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 * @see #putResource(String, byte[])
	 * @see #putResourceAsync(String, Function)
	 */
	void putResource(@NotNull String fileName, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(String, Supplier)
	 * @see #putResource(ResourceType, Identifier, byte[])
	 * @see #putResourceAsync(ResourceType, Identifier, Function)
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a resource into the resource pack's root asynchronously.
	 *
	 * @param fileName        the name of the file
	 * @param resourceFactory the factory of the resource content
	 * @return the future
	 * @see #putResourceAsync(ResourceType, Identifier, Function)
	 * @see #putResource(String, byte[])
	 * @see #putResource(String, Supplier)
	 */
	@NotNull Future<byte[]> putResourceAsync(@NotNull String fileName, @NotNull Function<@NotNull String, byte @NotNull []> resourceFactory);

	/**
	 * Puts a resource into the resource pack for the given side and path asynchronously.
	 *
	 * @param type            the resource type
	 * @param id              the path of the resource
	 * @param resourceFactory the factory of the resource content
	 * @return the future
	 * @see #putResourceAsync(String, Function)
	 * @see #putResource(ResourceType, Identifier, byte[])
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 */
	@NotNull Future<byte[]> putResourceAsync(@NotNull ResourceType type, @NotNull Identifier id,
			@NotNull Function<@NotNull Identifier, byte @NotNull []> resourceFactory);

	/**
	 * Puts a text resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param text     the resource content
	 * @see #putResource(String, byte[])
	 */
	default void putText(@NotNull String fileName, @NotNull String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path.
	 *
	 * @param type the resource type
	 * @param id   the path of the resource
	 * @param text the resource content
	 * @see #putResource(ResourceType, Identifier, byte[])
	 */
	default void putText(@NotNull ResourceType type, @NotNull Identifier id, @NotNull String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack's root.
	 *
	 * @param fileName     the name of the file
	 * @param textSupplier the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(String, Supplier)
	 */
	default void putText(@NotNull String fileName, @NotNull Supplier<@NotNull String> textSupplier) {
		this.putResource(fileName, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path.
	 *
	 * @param type         the resource type
	 * @param id           the path of the resource
	 * @param textSupplier the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 */
	default void putText(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<@NotNull String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a resource into the resource pack's root asynchronously.
	 *
	 * @param fileName    the name of the file
	 * @param textFactory the factory of the resource content
	 * @return the future
	 * @see #putResourceAsync(String, Function)
	 */
	default @NotNull Future<byte[]> putTextAsync(@NotNull String fileName, @NotNull Function<@NotNull String, @NotNull String> textFactory) {
		return this.putResourceAsync(fileName, textFactory.andThen(text -> text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path asynchronously.
	 *
	 * @param type        the resource type
	 * @param id          the path of the resource
	 * @param textFactory the factory of the resource content
	 * @return the future
	 * @see #putResourceAsync(ResourceType, Identifier, Function)
	 */
	default @NotNull Future<byte[]> putTextAsync(@NotNull ResourceType type, @NotNull Identifier id,
			@NotNull Function<@NotNull Identifier, @NotNull String> textFactory) {
		return this.putResourceAsync(type, id, textFactory.andThen(text -> text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Puts an image resource into the resource pack's root.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param fileName the name of the file
	 * @param image    the resource content
	 * @see #putResource(String, byte[])
	 */
	@ClientOnly
	default void putImage(String fileName, NativeImage image) throws IOException {
		this.putResource(fileName, image.getBytes());
	}

	/**
	 * Puts an image resource into the resource pack for the given path in the {@code assets} directory.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param id    the path of the resource
	 * @param image the resource content
	 * @see #putResource(ResourceType, Identifier, byte[])
	 */
	@ClientOnly
	default void putImage(Identifier id, NativeImage image) throws IOException {
		this.putResource(ResourceType.CLIENT_RESOURCES, id, image.getBytes());
	}

	/**
	 * Puts an image resource into the resource pack's root.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param fileName      the name of the file
	 * @param imageSupplier the supplier of the resource content
	 * @see #putResource(String, Supplier)
	 */
	@ClientOnly
	default void putImage(String fileName, Supplier<NativeImage> imageSupplier) {
		this.putResource(fileName, () -> {
			try (var image = imageSupplier.get()) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Puts an image resource into the resource pack for the given path in the {@code assets} directory.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param id            the path of the resource
	 * @param imageSupplier the supplier of the resource content
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 */
	@ClientOnly
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
	 * Puts an image resource into the resource pack's root asynchronously.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param fileName     the name of the file
	 * @param imageFactory the factory of the resource content
	 * @see #putResourceAsync(String, Function)
	 */
	@ClientOnly
	default @NotNull Future<byte[]> putImageAsync(@NotNull String fileName, @NotNull Function<@NotNull String, @NotNull NativeImage> imageFactory) {
		return this.putResourceAsync(fileName, imageFactory.andThen(image -> {
			try (image) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	/**
	 * Puts an image resource into the resource pack for the given path in the {@code assets} directory asynchronously.
	 * <p>
	 * <b>Note:</b> this method is only available on the client.
	 *
	 * @param id           the path of the resource
	 * @param imageFactory the factory of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResourceAsync(ResourceType, Identifier, Function)
	 */
	@ClientOnly
	default @NotNull Future<byte[]> putImageAsync(@NotNull Identifier id, @NotNull Function<@NotNull Identifier, @NotNull NativeImage> imageFactory) {
		return this.putResourceAsync(ResourceType.CLIENT_RESOURCES, id, imageFactory.andThen(image -> {
			try (image) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}));
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
