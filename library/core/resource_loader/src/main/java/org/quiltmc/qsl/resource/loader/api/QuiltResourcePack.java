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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Represents a resource pack with extended metadata, injected into {@link net.minecraft.resource.pack.ResourcePack}.
 */
@InjectedInterface(ResourcePack.class)
public interface QuiltResourcePack {
	/**
	 * {@return a display name for this resource pack}
	 */
	default @NotNull Text getDisplayName() {
		// To avoid javac complaining, the actual default implementation is mixin-ed into ResourcePack.
		return Text.empty();
	}

	/**
	 * Gets the activation type of this resource pack.
	 * <p>
	 * This only serves as a hint as ultimately the {@link net.minecraft.resource.pack.ResourcePackProfile}
	 * has the last word.
	 *
	 * @return the activation type of this resource pack
	 */
	default @NotNull ResourcePackActivationType getActivationType() {
		return ResourcePackActivationType.NORMAL;
	}

	/**
	 * {@return the path inside a resource pack of the given resource path}
	 *
	 * @param type the type of the resource
	 * @param id   the identifier of the resource
	 */
	@Contract(value = "_, _ -> new", pure = true)
	static @NotNull String getResourcePath(@NotNull ResourceType type, @NotNull Identifier id) {
		return type.getDirectory() + '/' + id.getNamespace() + '/' + id.getPath();
	}
}
