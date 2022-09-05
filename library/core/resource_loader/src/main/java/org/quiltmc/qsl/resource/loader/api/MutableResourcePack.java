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
	void putResource(@NotNull String path, byte @NotNull [] resource);

	void putResource(@NotNull ResourceType type, @NotNull Identifier id, byte @NotNull [] resource);

	void putResource(@NotNull String path, @NotNull Supplier<byte @NotNull []> resource);

	void putResource(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<byte @NotNull []> resource);

	default void putText(String path, String text) {
		this.putResource(path, text.getBytes(StandardCharsets.UTF_8));
	}

	default void putText(ResourceType type, Identifier id, String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	default void putText(String path, Supplier<String> textSupplier) {
		this.putResource(path, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	default void putText(ResourceType type, Identifier id, Supplier<String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	@Environment(EnvType.CLIENT)
	default void putImage(String path, NativeImage image) throws IOException {
		this.putResource(path, image.getBytes());
	}

	@Environment(EnvType.CLIENT)
	default void putImage(Identifier id, NativeImage image) throws IOException {
		this.putResource(ResourceType.CLIENT_RESOURCES, id, image.getBytes());
	}

	@Environment(EnvType.CLIENT)
	default void putImage(String path, Supplier<NativeImage> imageSupplier) {
		this.putResource(path, () -> {
			try (var image = imageSupplier.get()) {
				return image.getBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Environment(EnvType.CLIENT)
	default void putImage(Identifier id, Supplier<NativeImage> imageSupplier) {
		this.putImage(QuiltResourcePack.getResourcePath(ResourceType.CLIENT_RESOURCES, id), imageSupplier);
	}
}
