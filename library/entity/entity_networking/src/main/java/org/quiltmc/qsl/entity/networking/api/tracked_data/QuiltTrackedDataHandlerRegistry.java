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

package org.quiltmc.qsl.entity.networking.api.tracked_data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.entity.networking.impl.QuiltEntityNetworkingInitializer;

public final class QuiltTrackedDataHandlerRegistry {
	/**
	 * Registers custom {@linkplain TrackedDataHandler} in a mod compatible way.
	 *
	 * @param identifier the identifier of the tracked data handler
	 * @param handler    the handler to register
	 * @param <T>        the type the tracked data handler holds
	 * @return the registered tracked data
	 */
	@Contract("null, _ -> fail; _, null -> fail; _, _ -> param2")
	public static <T> TrackedDataHandler<T> register(@NotNull Identifier identifier, @NotNull TrackedDataHandler<T> handler) {
		return QuiltEntityNetworkingInitializer.register(identifier, handler);
	}
}
