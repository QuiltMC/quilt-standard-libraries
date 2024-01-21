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

package org.quiltmc.qsl.registry.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.EnvType;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

/**
 * Items/Blocks are registered in different order on client/server to make sure sync works correctly.
 * Server also gets its own entry that shouldn't block client from joining.
 */
public class RegistryLibSyncTest implements ModInitializer {
	private static final String NAMESPACE = "quilt_registry_test_sync";

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize(ModContainer mod) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			for (int i = 0; i < 10; i++) {
				register(i);
			}

			ClientLifecycleEvents.READY.register((x) -> this.printReg());
		} else {
			for (int i = 9; i >= 0; i--) {
				register(i);
			}

			var opt = register(10);
			RegistrySynchronization.setEntryOptional((SimpleRegistry<Item>) Registries.ITEM, opt);
			RegistrySynchronization.setEntryOptional((SimpleRegistry<Block>) Registries.BLOCK, opt);

			ServerLifecycleEvents.READY.register((x) -> this.printReg());
		}

		var customRequiredRegistry = Registry.register((Registry<Registry<Path>>) Registries.REGISTRY,
				new Identifier(NAMESPACE, "synced_registry"),
				new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier(NAMESPACE, "synced_registry")), Lifecycle.stable()));

		Registry.register(customRequiredRegistry, new Identifier("quilt:game_dir"), QuiltLoader.getGameDir());
		RegistrySynchronization.markForSync(customRequiredRegistry);
	}

	@SuppressWarnings({"unchecked", "RedundantCast"})
	private void printReg() {
		try {
			var writer = Files.newBufferedWriter(
					QuiltLoader.getGameDir().resolve("reg-" + MinecraftQuiltLoader.getEnvironmentType() + ".txt"),
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
			);

			for (var reg : Registries.REGISTRY) {
				writer.write("\n=== Registry: " + ((Registry<Registry<?>>) Registries.REGISTRY).getId(reg) + "\n");
				if (reg instanceof SynchronizedRegistry<?> sync) {
					writer.write("== Requires Sync: " + sync.quilt$requiresSyncing() + "\n");
					writer.write("== Status: " + sync.quilt$getContentStatus() + "\n");
				}

				for (var entry : reg) {
					writer.write("" + ((Registry<Object>) reg).getRawId(entry) + ": " + ((Registry<Object>) reg).getId(entry));
					writer.write("\n");
				}
			}

			writer.write("\n");
			writer.write("=== BlockStates");
			writer.write("\n");

			for (var entry : Block.STATE_IDS) {
				writer.write("" + Block.STATE_IDS.getRawId(entry) + ": " + Registries.BLOCK.getId(entry.getBlock()));
				writer.write("\n");
			}

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	static Identifier register(int i) {
		var id = new Identifier(NAMESPACE, "entry_" + i);
		var block = new Block(AbstractBlock.Settings.method_9630(Blocks.STONE).mapColor(MapColor.BLACK));

		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		RegistrySynchronization.setEntryOptional((SimpleRegistry<Item>) Registries.ITEM, id);
		return id;
	}
}
