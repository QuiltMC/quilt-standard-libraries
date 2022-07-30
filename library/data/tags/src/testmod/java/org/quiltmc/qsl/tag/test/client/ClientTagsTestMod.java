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

package org.quiltmc.qsl.tag.test.client;

import static org.quiltmc.qsl.command.api.client.ClientCommandManager.literal;

import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.test.TagsTestMod;

public final class ClientTagsTestMod implements ClientModInitializer {
	public static final TagKey<Block> TEST_CLIENT_BLOCK_TAG = QuiltTagKey.of(Registry.BLOCK_KEY, TagsTestMod.id("client_block_tag"), TagType.CLIENT_ONLY);
	public static final TagKey<Biome> TEST_CLIENT_BIOME_TAG = QuiltTagKey.of(Registry.BIOME_KEY, TagsTestMod.id("client_biome_tag"), TagType.CLIENT_ONLY);
	public static final TagKey<Item> TEST_DEFAULT_ITEM_TAG = QuiltTagKey.of(Registry.ITEM_KEY, TagsTestMod.id("default_item_tag"), TagType.CLIENT_FALLBACK);

	private static final Consumer<Text> FEEDBACK_CONSUMER = msg -> MinecraftClient.getInstance().player.sendMessage(msg, false);

	@Override
	public void onInitializeClient(ModContainer mod) {
		ResourceLoader.registerBuiltinResourcePack(TagsTestMod.id("defaulted_test_pack"), ResourcePackActivationType.NORMAL);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			dispatcher.register(literal("client_tag_test")
					.then(literal("block")
							.executes(context -> {
								TagsTestMod.displayTag(TEST_CLIENT_BLOCK_TAG, Registry.BLOCK, FEEDBACK_CONSUMER);
								return 0;
							})
					).then(literal("biome")
							.executes(context -> {
								TagsTestMod.displayTag(TEST_CLIENT_BIOME_TAG,
										context.getSource().getRegistryManager().get(Registry.BIOME_KEY),
										FEEDBACK_CONSUMER
								);
								return 0;
							})
					).then(literal("fallback_item")
							.executes(context -> {
								TagsTestMod.displayTag(TEST_DEFAULT_ITEM_TAG, Registry.ITEM, FEEDBACK_CONSUMER);
								return 0;
							})
					)
			);
		});
	}
}
