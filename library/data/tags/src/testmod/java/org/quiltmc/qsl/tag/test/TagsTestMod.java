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

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.literal;


public final class TagsTestMod implements ServerLifecycleEvents.Ready, CommandRegistrationCallback {
	public static final String NAMESPACE = "quilt_tags_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(TagsTestMod.class);

	/**
	 * This tag means:
	 * - worlds opened by the client must have this tag to work, only included in the quilt_tags_testmod:required_test_pack
	 * data-pack so the testing can be exactly sure.
	 * - can connect to servers that don't have the tag (since it's not TagType.CLIENT_SERVER_REQUIRED)
	 */
	public static final Tag.Identified<Block> TEST_REQUIRED_BLOCK_TAG = TagRegistry.BLOCK.create(
			id("required_block_tag"), TagType.SERVER_REQUIRED
	);
	public static final Tag.Identified<Biome> TEST_BIOME_TAG = TagRegistry.BIOME.create(
			id("registry_test")
	);

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean integrated, boolean dedicated) {
		dispatcher.register(literal("biome_tag_test")
				.then(literal("registry").executes(context -> {
					TEST_BIOME_TAG.values().forEach(biome -> {
						Identifier id = context.getSource().getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
						context.getSource().sendFeedback(new LiteralText(id.toString()), false);
					});

					return 1;
				}))
				.then(literal("list_all").executes(context -> {
					context.getSource().getServer().getTagManager().getOrCreateTagGroup(Registry.BIOME_KEY).getTags()
							.forEach((tagId, tag) -> {
								displayTag(
										tagId, tag, context.getSource().getRegistryManager().get(Registry.BIOME_KEY),
										msg -> context.getSource().sendFeedback(msg, false)
								);
							});

					return 1;
				}))
		);
	}

	@Override
	public void readyServer(MinecraftServer server) {
		// Asserts the existence of the tag.
		Tag<Block> tag = server.getTagManager().getTag(Registry.BLOCK_KEY, TEST_REQUIRED_BLOCK_TAG.getId(),
				identifier -> new IllegalStateException("Could not find tag " + identifier));
		LOGGER.info("Tag content: {}", tag.values().stream()
				.map(Registry.BLOCK::getId)
				.map(Identifier::toString)
				.collect(Collectors.joining(", "))
		);
	}

	public static <T> void displayTag(Tag.Identified<T> tag, Registry<T> registry, Consumer<Text> feedbackConsumer) {
		displayTag(tag.getId(), tag, registry, feedbackConsumer);
	}

	public static <T> void displayTag(Identifier tagId, Tag<T> tag, Registry<T> registry, Consumer<Text> feedbackConsumer) {
		feedbackConsumer.accept(new LiteralText(tagId + ":").formatted(Formatting.GREEN));

		for (var value : tag.values()) {
			Identifier id = registry.getId(value);
			feedbackConsumer.accept(new LiteralText(" - ")
					.append(new LiteralText(id.toString()).formatted(Formatting.GOLD)));
		}
	}
}
