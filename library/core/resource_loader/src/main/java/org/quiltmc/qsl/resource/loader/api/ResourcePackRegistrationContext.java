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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.pack.ResourcePack;

/**
 * Represents a context to register resource packs at will invisibly from the user.
 *
 * @see ResourceLoader#getRegisterDefaultResourcePackEvent()
 * @see ResourceLoader#getRegisterTopResourcePackEvent()
 */
public interface ResourcePackRegistrationContext {
	/**
	 * {@return the resource manager that contains the currently existing resource packs}
	 */
	@Contract(pure = true)
	@NotNull ResourceManager resourceManager();

	/**
	 * Adds a new resource pack.
	 * <p>
	 * The resource pack will not be visible to users.
	 *
	 * @param pack the pack to add
	 */
	void addResourcePack(@NotNull ResourcePack pack);

	/**
	 * Functional interface to be implemented on callbacks for {@link ResourceLoader#getRegisterDefaultResourcePackEvent()}
	 * and {@link ResourceLoader#getRegisterTopResourcePackEvent()}.
	 */
	@FunctionalInterface
	interface Callback {
		/**
		 * Called when resource packs are being registered, giving the option to register a resource pack.
		 *
		 * @param context the context
		 */
		void onRegisterPack(@NotNull ResourcePackRegistrationContext context);
	}
}
