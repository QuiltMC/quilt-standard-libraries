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

package org.quiltmc.qsl.resource.loader.test;

import static org.quiltmc.qsl.resource.loader.test.ResourceLoaderTestMod.id;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;

public class BuiltinResourcePackTestMod implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		if (!ResourceLoader.registerBuiltinResourcePack(id("test"), mod, ResourcePackActivationType.DEFAULT_ENABLED,
				Text.literal("Test built-in resource pack").formatted(Formatting.GOLD))) {
			throw new RuntimeException("Could not register built-in resource pack.");
		}

		this.testPackMetaGenerations();
	}

	/**
	 * Tests {@link ModResourcePackUtil#getPackMeta(String, ResourceType)} so it generates a perfectly valid JSON.
	 */
	private void testPackMetaGenerations() {
		this.testPackMetaGeneration(null);
		this.testPackMetaGeneration("");
		this.testPackMetaGeneration("Test");
		this.testPackMetaGeneration("\"Test\"");
		this.testPackMetaGeneration("\"Test\\");
		this.testPackMetaGeneration("\"Test\\\\\"");
	}

	private void testPackMetaGeneration(String name) {
		String pack = ModResourcePackUtil.getPackMeta(name, ResourceType.CLIENT_RESOURCES);
		JsonObject obj;

		try {
			obj = (JsonObject) JsonParser.parseString(pack);
		} catch (JsonParseException e) {
			throw new AssertionError("Pack metadata parsing test for description \"" + name + "\".", e);
		}

		String desc = obj.getAsJsonObject("pack").get("description").getAsString();

		if (!desc.equals(name == null ? "" : name)) {
			throw new AssertionError("Escaped name is different from name after parsing. Got \"" + desc + "\", expected \"" + name + "\".");
		}
	}
}
