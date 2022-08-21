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

package org.quiltmc.qsl.tracked_data.api;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;
import org.quiltmc.qsl.tracked_data.impl.QuiltTrackedDataInitializer;

public final class QuiltTrackedDataHandlerRegistry {
	private static boolean markForSync = true;

	/**
	 * This method registers custom TrackedDataHandler in a mod compatible way
	 *
	 * @param identifier Unique identifier
	 * @param handler Handler you want to register
	 * @param <T> Type of handler
	 * @return Handler you provided
	 */
	public static <T> TrackedDataHandler<T> register(Identifier identifier, TrackedDataHandler<T> handler) {
		Registry.register(QuiltTrackedDataInitializer.HANDLER_REGISTRY, identifier, handler);

		if (markForSync) {
			RegistrySynchronization.markForSync(QuiltTrackedDataInitializer.HANDLER_REGISTRY);
			markForSync = false;
		}
		return handler;
	}
}
