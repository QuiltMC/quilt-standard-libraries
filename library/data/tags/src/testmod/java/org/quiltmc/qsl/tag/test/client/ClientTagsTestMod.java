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

package org.quiltmc.qsl.tag.test.client;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.test.TagsTestMod;

public final class ClientTagsTestMod implements ClientModInitializer {
	/**
	 * This tag is only registered on the client:
	 * - worlds opened by the client must have this tag to work, only included in the quilt_tags_testmod:required_test_pack
	 * data-pack so the testing can be exactly sure.
	 * - can connect to servers that don't have the tag (since it's not TagType.CLIENT_SERVER_REQUIRED)
	 */
	public static final Tag<Block> TEST_REQUIRED_BLOCK_TAG = TagRegistry.BLOCK.create(
			TagsTestMod.id("required_block_tag"), TagType.SERVER_REQUIRED
	);
	public static final Tag.Identified<Block> TEST_CLIENT_BLOCK_TAG = TagRegistry.BLOCK.create(
			TagsTestMod.id("client_block_tag"), TagType.CLIENT_ONLY
	);
	public static final Tag.Identified<Biome> TEST_CLIENT_BIOME_TAG = TagRegistry.BIOME.create(
			TagsTestMod.id("client_biome_tag"), TagType.CLIENT_ONLY
	);
	public static final Tag.Identified<Item> TEST_DEFAULT_ITEM_TAG = TagRegistry.ITEM.create(
			TagsTestMod.id("default_item_tag"), TagType.CLIENT_SERVER_SYNC
	);

	private World lastWorld;

	@Override
	public void onInitializeClient() {
		ResourceLoader.registerBuiltinResourcePack(TagsTestMod.id("defaulted_test_pack"), ResourcePackActivationType.NORMAL);
		ResourceLoader.registerBuiltinResourcePack(TagsTestMod.id("required_test_pack"), ResourcePackActivationType.NORMAL);

		ClientWorldTickEvents.START.register((client, world) -> {
			if (this.lastWorld != world) {
				displayTag(client, TEST_CLIENT_BLOCK_TAG, Registry.BLOCK);
				displayTag(client, TEST_CLIENT_BIOME_TAG, client.world.getRegistryManager().get(Registry.BIOME_KEY));
				displayTag(client, TEST_DEFAULT_ITEM_TAG, Registry.ITEM);

				this.lastWorld = world;
			}
		});
	}

	private static <T> void displayTag(MinecraftClient client, Tag.Identified<T> tag, Registry<T> registry) {
		client.player.sendMessage(new LiteralText(tag.getId() + ":").formatted(Formatting.GREEN),
				false);
		for (var value : tag.values()) {
			Identifier id = registry.getId(value);
			client.player.sendMessage(new LiteralText(" - ")
							.append(new LiteralText(id.toString()).formatted(Formatting.GOLD)),
					false);
		}
	}
}
