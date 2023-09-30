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

package org.quiltmc.qsl.resource.loader.test.server;

import net.minecraft.util.Language;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.DedicatedServerOnly;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;

@DedicatedServerOnly
public class ServerLanguageTestMod implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer(ModContainer mod) {
		// Check whether the dedicated server properly loaded the default language with modded entries.
		assert Language.getInstance().get("menu.singleplayer").equals("Let's test!");
	}
}
