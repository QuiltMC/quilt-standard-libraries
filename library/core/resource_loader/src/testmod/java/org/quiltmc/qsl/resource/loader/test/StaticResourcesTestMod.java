/*
 * Copyright 2023 The Quilt Project
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

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public class StaticResourcesTestMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltStaticResourcesTest");
	@Override
	public void onInitialize(ModContainer mod) {
		try {
			ResourceManager clientManager = ResourceLoader.getStaticResourceManager(ResourceType.CLIENT_RESOURCES);
			Resource cronch = clientManager.getResource(new Identifier("cronch", "test_client")).orElseThrow();
			BufferedReader readerCronch = cronch.openBufferedReader();
			LOGGER.error("{} (Reading this line should be impossible!)", readerCronch.readLine());
			readerCronch.close();
		} catch (Exception e) {
			LOGGER.info("As anticipated, clientside resource fetch failed on logical server. Exception: {}", e.toString());
		}

		ResourceManager staticManager = ResourceLoader.getStaticResourceManager(ResourceType.SERVER_DATA);
		LOGGER.info("Loaded static namespaces: {}", staticManager.getAllNamespaces());
		Map<Identifier, Resource> blockRes = staticManager.findResources("add_block", identifier -> true);
		Map<Identifier, JsonElement> blockElements = new HashMap<>();
		for (Map.Entry<Identifier, Resource> r : blockRes.entrySet()) {
			try {
				JsonElement element = JsonParser.parseReader(r.getValue().openBufferedReader());
				blockElements.put(r.getKey(), element);
			} catch (Exception e) {
				LOGGER.error(e.toString());
			}
		}
		for (Map.Entry<Identifier, JsonElement> r : blockElements.entrySet()) {
			String blockName = r.getValue().getAsJsonObject().get("block").getAsString();
			Registry.register(Registries.BLOCK,
				new Identifier(r.getKey().getNamespace(), blockName),
				new Block(AbstractBlock.Settings.copy(Blocks.RED_WOOL)));
			LOGGER.info("Registered block: {} via data!", new Identifier(r.getKey().getNamespace(), blockName));
		}
	}
}
