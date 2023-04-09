/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.feature.flag.test;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.feature.flag.api.QuiltFeatureFlags;
import org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public class FeatureFlagTest implements ModInitializer {
	public static final String MODID = "quilt_feature_flag_testmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final FeatureFlag TEST;

	static {
		// Registers enough flags to take up more than the remaining bits in the long used by vanilla.
		int counter;
		for (counter = 0; counter < 100; counter++) {
			QuiltFeatureFlags.registerFlag(id("flood_test_" + counter));
		}

		TEST = QuiltFeatureFlags.registerFlag(id("test"));
		counter++;

		LOGGER.info("{} Testmod flags registered", counter);

		Block block = Registry.register(Registries.BLOCK,
				id("test"),
				new Block(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).requiredFlags(TEST)));

		Registry.register(Registries.ITEM, id("test"), new BlockItem(block, new Item.Settings().requiredFlags(TEST)));
	}

	private static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
				.registerResourcePackProfileProvider(profileAdder -> this.test(profileAdder, ResourceType.CLIENT_RESOURCES));
		ResourceLoader.get(ResourceType.SERVER_DATA)
				.registerResourcePackProfileProvider(profileAdder -> this.test(profileAdder, ResourceType.SERVER_DATA));
	}

	private void test(Consumer<ResourcePackProfile> profileAdder, ResourceType type) {
		var pack = new InMemoryResourcePack.Named("flag_test");

		pack.putText("pack.mcmeta", String.format("""
						{
							"features": {
								"enabled": [
									"quilt_feature_flag_testmod:test"
								]
							},
							"pack": {
								"description": "It's over 64!!!",
								"pack_format": %d
							}
						}
					""",
				type.getPackVersion(SharedConstants.getGameVersion())));

		profileAdder.accept(ResourcePackProfile.of("flag_test", Text.literal("Test Feature"), false, name -> pack,
				type, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_FEATURE));
	}
}
