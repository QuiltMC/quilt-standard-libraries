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

package org.quiltmc.qsl.tracked_data.impl;

import com.mojang.serialization.Lifecycle;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

@ApiStatus.Internal
public class QuiltTrackedDataInitializer implements ModInitializer {
	public static final SimpleRegistry<TrackedDataHandler<?>> HANDLER_REGISTRY = new SimpleRegistry<>(
			RegistryKey.ofRegistry(new Identifier("quilt", "tracked_data_handlers")), Lifecycle.stable(), null
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(((Registry<Registry<TrackedDataHandler<?>>>) Registry.REGISTRIES), HANDLER_REGISTRY.getKey().getValue(), HANDLER_REGISTRY);
	}
}
