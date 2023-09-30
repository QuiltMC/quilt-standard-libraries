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

package org.quiltmc.qsl.resource.loader.test;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.api.ResourcePackRegistrationContext;

public class VirtualResourcePackTestMod implements ModInitializer, ResourcePackRegistrationContext.Callback, ServerLifecycleEvents.Ready {
	private static final TagKey<Block> TEST_TAG = TagKey.of(RegistryKeys.BLOCK, ResourceLoaderTestMod.id("test_virtual_tag"));
	private static final TagKey<Block> TEST_TAG2 = TagKey.of(RegistryKeys.BLOCK, ResourceLoaderTestMod.id("test_stackable_tag"));
	private static final Identifier TAG_FILE = new Identifier(
			TEST_TAG.id().getNamespace(), "tags/blocks/" + TEST_TAG.id().getPath() + ".json"
	);
	private static final Identifier TAG_FILE2 = new Identifier(
			TEST_TAG2.id().getNamespace(), "tags/blocks/" + TEST_TAG2.id().getPath() + ".json"
	);

	@Override
	public void onInitialize(ModContainer mod) {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).getRegisterDefaultResourcePackEvent().register(this);
		ResourceLoader.get(ResourceType.SERVER_DATA).getRegisterDefaultResourcePackEvent().register(this);

		ResourceLoader.get(ResourceType.SERVER_DATA).getRegisterDefaultResourcePackEvent()
				.register(this.createBasicTagBasedResourcePack("Virtual Tag Default", Blocks.DIAMOND_BLOCK));
		ResourceLoader.get(ResourceType.SERVER_DATA).getRegisterTopResourcePackEvent()
				.register(this.createBasicTagBasedResourcePack("Virtual Tag Top", Blocks.MOSS_BLOCK));

		ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
				.registerResourcePackProfileProvider(profileAdder -> this.providePacks(profileAdder, ResourceType.CLIENT_RESOURCES));
		ResourceLoader.get(ResourceType.SERVER_DATA)
				.registerResourcePackProfileProvider(profileAdder -> this.providePacks(profileAdder, ResourceType.SERVER_DATA));

		ServerLifecycleEvents.READY.register(this);
	}

	private void providePacks(Consumer<ResourcePackProfile> profileAdder, ResourceType type) {
		var pack = new InMemoryResourcePack.Named("activation_test") {
			@Override
			public @NotNull ResourcePackActivationType getActivationType() {
				return ResourcePackActivationType.DEFAULT_ENABLED;
			}
		};

		pack.putText("pack.mcmeta", String.format("""
				{"pack":{"pack_format":%d,"description":"Provided pack activation test."}}
					""", SharedConstants.getGameVersion().getResourceVersion(type)));
		pack.putText(ResourceType.CLIENT_RESOURCES, new Identifier("models/block/dandelion.json"), """
				{
					"parent": "minecraft:block/cube_all",
					"textures": {
						"all": "minecraft:block/dandelion"
					}
				}
				""");
		pack.putText(ResourceType.SERVER_DATA, new Identifier("loot_tables/blocks/dandelion.json"), """
				{
					"type": "minecraft:block",
					"pools": [
						{
							"bonus_rolls": 0.0,
							"conditions": [
								{
									"condition": "minecraft:survives_explosion"
								}
							],
								"entries": [
								{
									"type": "minecraft:item",
									"name": "minecraft:diamond"
								}
							],
							"rolls": 1.0
						}
					]
				}
				""");

		profileAdder.accept(ResourcePackProfile.of("activation_test", Text.literal("Activation Test"), false, name -> pack,
				type, ResourcePackProfile.InsertionPosition.BOTTOM, ResourcePackSource.PACK_SOURCE_BUILTIN));
	}

	@Override
	public void onRegisterPack(@NotNull ResourcePackRegistrationContext context) {
		var pack = new InMemoryResourcePack.Named("Test Virtual Resource Pack");
		pack.putText(ResourceType.CLIENT_RESOURCES, new Identifier("models/block/poppy.json"), """
				{
				  "parent": "minecraft:block/cube_all",
				  "textures": {
				    "all": "minecraft:block/poppy"
				  }
				}
				""");
		pack.putText(ResourceType.SERVER_DATA, new Identifier("loot_tables/blocks/poppy.json"), """
				{
					"type": "minecraft:block",
					"pools": [
						{
							"bonus_rolls": 0.0,
							"conditions": [
								{
									"condition": "minecraft:survives_explosion"
								}
						],
						"entries": [
							{
								"type": "minecraft:item",
								"name": "minecraft:diamond"
							}
						],
						"rolls": 1.0
						}
					]
				}
				""");
		context.addResourcePack(pack);
	}

	@Override
	public void readyServer(MinecraftServer server) {
		assert !Blocks.DIAMOND_BLOCK.getDefaultState().isIn(TEST_TAG);
		assert Blocks.MOSS_BLOCK.getDefaultState().isIn(TEST_TAG);
	}

	private ResourcePackRegistrationContext.Callback createBasicTagBasedResourcePack(String name, Block block) {
		return context -> {
			var pack = new InMemoryResourcePack.Named(name);
			pack.putTextAsync(ResourceType.SERVER_DATA, TAG_FILE, file -> """
					{
						"replace": true,
						"values": [
							"%s"
						]
					}
					""".formatted(Registries.BLOCK.getId(block)));
			pack.putTextAsync(ResourceType.SERVER_DATA, TAG_FILE2, file -> """
					{
						"replace": false,
						"values": [
							"%s"
						]
					}
					""".formatted(Registries.BLOCK.getId(block)));
			context.addResourcePack(pack);
		};
	}
}
