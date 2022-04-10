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

package org.quiltmc.qsl.registry.test;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;


public class RegistryLibSyncTest implements ModInitializer {
	private static final String NAMESPACE = "quilt_registry_test_sync";

	@Override
	public void onInitialize(ModContainer mod) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			for (int i = 0; i < 10; i++) {
				register(i);
			}
			ClientLifecycleEvents.READY.register((x) -> printReg());
		} else {
			for (int i = 9; i >= 0; i--) {
				register(i);
			}
			ServerLifecycleEvents.READY.register((x) -> printReg());
		}
	}

	private void printReg() {
		try {
			var writer = Files.newBufferedWriter(
					FabricLoader.getInstance().getGameDir().resolve("reg-" + FabricLoader.getInstance().getEnvironmentType() + ".txt"),
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
					);

			for (var reg : Registry.REGISTRIES) {
				writer.write("\n");
				writer.write("=== Registry: " + ((Registry<Registry<?>>) Registry.REGISTRIES).getId(reg));
				writer.write("\n");

				for (var entry : reg) {
					writer.write("" + ((Registry<Object>)reg).getRawId(entry) + ": " + ((Registry<Object>)reg).getId(entry));
					writer.write("\n");
				}
			}

			writer.write("\n");
			writer.write("=== BlockStates");
			writer.write("\n");

			for (var entry : Block.STATE_IDS) {
				writer.write("" + Block.STATE_IDS.getRawId(entry) + ": " + Registry.BLOCK.getId(entry.getBlock()));
				writer.write("\n");
			}

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	static void register(int i) {
		var id = new Identifier(NAMESPACE, "entry_" + i);
		var block = new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK));

		Registry.register(Registry.BLOCK, id, block);
		Registry.register(Registry.ITEM, id, new BlockItem(block, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
	}
}
