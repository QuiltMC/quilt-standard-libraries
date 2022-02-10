/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.resource.loader.test;

import static org.quiltmc.qsl.resource.loader.test.ResourceLoaderTestMod.id;

import net.fabricmc.api.ModInitializer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

public class BuiltinResourcePackTestMod implements ModInitializer {
	@Override
	public void onInitialize() {
		if (!ResourceLoader.registerBuiltinResourcePack(id("test"), ResourcePackActivationType.DEFAULT_ENABLED)) {
			throw new RuntimeException("Could not register built-in resource pack.");
		}
	}
}
