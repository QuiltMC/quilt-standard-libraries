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

package org.quiltmc.qsl.tag.test;

import java.util.stream.Collectors;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;

public final class TagsTestMod implements ModInitializer {
	public static final String NAMESPACE = "quilt_tags_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(TagsTestMod.class);

	/**
	 * This tag is only registered on the client:
	 * - worlds opened by the client must have this tag to work, only included in the quilt_tags_testmod:required_test_pack
	 * data-pack so the testing can be exactly sure.
	 * - can connect to servers that don't have the tag (since it's not TagType.CLIENT_SERVER_REQUIRED)
	 */
	public static final Tag.Identified<Block> TEST_REQUIRED_BLOCK_TAG = TagRegistry.BLOCK.create(
			id("required_block_tag"), TagType.SERVER_REQUIRED
	);

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.READY.register(server -> {
			// Asserts the existence of the tag.
			Tag<Block> tag = server.getTagManager().getTag(Registry.BLOCK_KEY, TEST_REQUIRED_BLOCK_TAG.getId(),
					identifier -> new IllegalStateException("Could not find tag " + identifier));
			LOGGER.info("Tag content: {}", tag.values().stream()
					.map(Registry.BLOCK::getId)
					.map(Identifier::toString)
					.collect(Collectors.joining(", "))
			);
		});
	}
}
