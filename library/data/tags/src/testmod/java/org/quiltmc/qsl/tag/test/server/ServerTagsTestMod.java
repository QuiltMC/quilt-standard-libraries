/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.test.server;

import net.fabricmc.api.DedicatedServerModInitializer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.tag.test.TagsTestMod;

public class ServerTagsTestMod implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		// We enable this one by default, else the automatic server testing will fail.
		ResourceLoader.registerBuiltinResourcePack(TagsTestMod.id("required_test_pack"), ResourcePackActivationType.DEFAULT_ENABLED);
	}
}
