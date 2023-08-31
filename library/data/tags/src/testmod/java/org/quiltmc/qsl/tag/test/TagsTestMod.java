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

package org.quiltmc.qsl.tag.test;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.tag.api.TagRegistry;

public final class TagsTestMod implements ServerLifecycleEvents.Ready, CommandRegistrationCallback {
	public static final String NAMESPACE = "quilt_tags_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(TagsTestMod.class);

	public static final TagKey<Block> TEST_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, id("block_tag"));
	public static final TagKey<Biome> TEST_BIOME_TAG = TagKey.of(RegistryKeys.BIOME, id("registry_test"));

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(literal("biome_tag_test")
				.then(literal("registry").executes(context -> {
					displayTag(TEST_BIOME_TAG, context.getSource().getRegistryManager().get(RegistryKeys.BIOME),
							context.getSource());

					return 1;
				}))
				.then(literal("list_all").executes(context -> {
					TagRegistry.stream(RegistryKeys.BIOME).forEach((entry) -> {
						displayTag(
								entry.key(), entry.values(),
								context.getSource().getRegistryManager().get(RegistryKeys.BIOME),
								msg -> context.getSource().sendSystemMessage(msg)
						);
					});

					return 1;
				}))
		);
	}

	@Override
	public void readyServer(MinecraftServer server) {
		// Asserts the existence of the tag.
		LOGGER.info("Tag content: {}", TagRegistry.getTag(TEST_BLOCK_TAG).stream()
				.map(Holder::value)
				.map(Registries.BLOCK::getId)
				.map(Identifier::toString)
				.collect(Collectors.joining(", "))
		);
	}

	public static <T> void displayTag(TagKey<T> tag, Registry<T> registry, ServerCommandSource source) {
		displayTag(tag, registry, source::sendSystemMessage);
	}

	public static <T> void displayTag(TagKey<T> tag, Registry<T> registry, Consumer<Text> feedbackConsumer) {
		displayTag(tag, TagRegistry.getTag(tag), registry, feedbackConsumer);
	}

	private static <T> void displayTag(TagKey<T> tagKey, Collection<Holder<T>> tag, Registry<T> registry, Consumer<Text> feedbackConsumer) {
		feedbackConsumer.accept(Text.literal(tagKey.id() + ":").formatted(Formatting.GREEN));

		for (var value : tag) {
			Identifier id = registry.getId(value.value());
			feedbackConsumer.accept(Text.literal(" - ")
					.append(Text.literal(id.toString()).formatted(Formatting.GOLD)));
		}
	}
}
